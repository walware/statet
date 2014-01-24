/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.List;

import com.ibm.icu.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceContentLines;
import de.walware.ecommons.text.FixInterningStringCache;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.LineInformationCreator;
import de.walware.ecommons.text.PartialStringParseInput;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RChunkElement;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RProblemReporter;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.model.SpecialParseContent;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.core.rsource.ast.RoxygenScanner;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * Worker for r model manager
 */
public class RReconciler {
	
	
	private static final boolean LOG_TIME = false;
	
	protected static class Data {
		
		public final RSuModelContainer adapter;
		public final SourceContent content;
		
		public SourceParseInput parseInput;
		public int parseOffset;
		
		private SourceContentLines contentLines;
		
		public AstInfo ast;
		
		public IRModelInfo oldModel;
		public IRModelInfo newModel;
		
		public Data(final RSuModelContainer adapter, final IProgressMonitor monitor) {
			this.adapter = adapter;
			this.content = adapter.getParseContent(monitor);
		}
		
	}
	
	
	private final RModelManager fManager;
	protected boolean fStop = false;
	
	private final LineInformationCreator fLineInformationCreator = new LineInformationCreator();
	
	private final Object f1AstLock = new Object();
	private final IStringCache f1AstStringCache;
	private final RoxygenScanner f1RoxygenScanner;
	
	private final Object f2ModelLock = new Object();
	private final SourceAnalyzer f2ScopeAnalyzer;
	
	private final Object f3ReportLock = new Object();
	private final RProblemReporter f3ProblemReporter;
	
	
	public RReconciler(final RModelManager manager) {
		fManager = manager;
		f1AstStringCache = new FixInterningStringCache(24);
		f1RoxygenScanner = new RoxygenScanner(f1AstStringCache);
		f2ScopeAnalyzer = new SourceAnalyzer();
		f3ProblemReporter = new RProblemReporter();
	}
	
	
	protected SourceContentLines getContentLines(final Data data) {
		if (data.contentLines == null) {
			synchronized (fLineInformationCreator) {
				data.contentLines = new SourceContentLines(data.content,
						fLineInformationCreator.create(data.content.text) );
			}
		}
		return data.contentLines;
	}
	
	/** for editor reconciling */
	public IRModelInfo reconcile(final RSuModelContainer adapter,
			final int level, final IProgressMonitor monitor) {
		final IRSourceUnit su = adapter.getSourceUnit();
		final int type = (su.getModelTypeId().equals(RModel.TYPE_ID) ? su.getElementType() : 0);
		if (type == 0) {
			return null;
		}
		final Data data = new Data(adapter, monitor);
		if (data.content == null) {
			return null;
		}
		synchronized (f1AstLock) {
			if (fStop || monitor.isCanceled()) {
				return null;
			}
			updateAst(data, monitor);
		}
		
		if (fStop || monitor.isCanceled()
				|| (level & 0xf) < IModelManager.MODEL_FILE) {
			return null;
		}
		
		synchronized (f2ModelLock) {
			if (fStop || monitor.isCanceled()) {
				return null;
			}
			final boolean updated = updateModel(data);
			
			if (fStop) {
				return null;
			}
			
			// Throw event
			if (updated) {
				fManager.getEventJob().addUpdate(su, data.oldModel, data.newModel);
			}
		}
		
		if ((level & IModelManager.RECONCILER) != 0 && data.newModel != null) {
			if (fStop || monitor.isCanceled()) {
				return null;
			}
			
			IProblemRequestor problemRequestor = null;
			synchronized (f3ReportLock) {
				if (!fStop && !monitor.isCanceled()
						&& data.newModel == adapter.getCurrentModel() ) {
					problemRequestor = adapter.createProblemRequestor(data.ast.stamp);
					if (problemRequestor != null) {
						f3ProblemReporter.run(su, getContentLines(data),
								(RAstNode) data.ast.root, problemRequestor );
					}
				}
			}
			if (problemRequestor != null) {
				problemRequestor.finish();
			}
		}
		
		return data.newModel;
	}
	
	public IRModelInfo reconcile(final IRSourceUnit su, final ISourceUnitModelInfo modelInfo,
			final List<? extends RChunkElement> chunks,
			final int level, final IProgressMonitor monitor) {
		synchronized (f2ModelLock) {
			return updateModel(su, modelInfo, chunks);
		}
	}
	
	protected void initParseInput(final Data data) {
		if (data.parseInput == null) {
			if (data.content instanceof SpecialParseContent) {
				final SpecialParseContent parseContent = (SpecialParseContent) data.content;
				data.parseInput = new PartialStringParseInput(data.content.text, parseContent.offset);
				data.parseOffset = parseContent.offset;
			}
			else {
				data.parseInput = new StringParseInput(data.content.text);
				data.parseOffset = 0;
			}
		}
	}
	
	protected final void updateAst(final Data data, final IProgressMonitor monitor) {
		data.ast = data.adapter.getCurrentAst(data.content.stamp);
		
		if (data.ast == null) {
			final long startAst;
			final long stopAst;
			startAst = System.nanoTime();
			
			initParseInput(data);
			final RScanner scanner = new RScanner(data.parseInput, AstInfo.LEVEL_MODEL_DEFAULT,
					f1AstStringCache );
			scanner.setCommentLevel(100);
			final SourceComponent sourceComponent = scanner.scanSourceRange(null,
					data.parseOffset, data.content.text.length() );
			final AstInfo ast = new AstInfo(scanner.getAstLevel(), data.content.stamp, sourceComponent);
			
			stopAst = System.nanoTime();
			
			f1RoxygenScanner.init(data.parseInput);
			f1RoxygenScanner.update(sourceComponent);
			
			if (LOG_TIME) {
				System.out.println(f1AstStringCache.toString());
				System.out.println("RReconciler/createAST   : " + DecimalFormat.getInstance().format(stopAst-startAst)); //$NON-NLS-1$
			}
			
			synchronized (data.adapter) {
				data.adapter.setAst(ast);
			}
			data.ast = ast;
		}
	}
	
	protected final boolean updateModel(final Data data) {
		// Update Model
		data.newModel = data.adapter.getCurrentModel(data.ast.stamp);
		if (data.newModel == null) {
			final long startModel;
			final long stopModel;
			startModel = System.nanoTime();
			
			final IRModelInfo model = f2ScopeAnalyzer.createModel(data.adapter.getSourceUnit(), data.ast);
			final boolean isOK = (model != null);
			
			stopModel = System.nanoTime();
			
			if (LOG_TIME) {
				System.out.println("RReconciler/createMODEL : " + DecimalFormat.getInstance().format(stopModel-startModel)); 
			}
			
			if (isOK) {
				synchronized (data.adapter) {
					data.oldModel = data.adapter.getCurrentModel();
					data.adapter.setModel(model);
				}
				data.newModel = model;
				return true;
			}
		}
		return false;
	}
	
	private IRModelInfo updateModel(final IRSourceUnit su, final ISourceUnitModelInfo modelInfo,
			final List<? extends RChunkElement> chunks) {
		IRModelInfo model;
		try {
			final AstInfo ast = modelInfo.getAst();
			f2ScopeAnalyzer.beginChunkSession(su, ast);
			for (int i = 0; i < chunks.size(); i++) {
				final RChunkElement element = chunks.get(i);
				final SourceComponent[] rootNodes;
				{	final Object source = element.getAdapter(SourceComponent.class);
					if (source instanceof SourceComponent) {
						rootNodes = new SourceComponent[] { (SourceComponent) source };
					}
					else if (source instanceof SourceComponent[]) {
						rootNodes = (SourceComponent[]) source;
					}
					else {
						continue;
					}
				}
				f2ScopeAnalyzer.processChunk(element, rootNodes);
			}
		}
		finally {
			model = f2ScopeAnalyzer.stopChunkSession();
		}
		return model;
	}
	
	void stop() {
		fStop = true;
	}
	
}
