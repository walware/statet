/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.text.FixInterningStringCache;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.PartialStringParseInput;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RChunkElement;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RProblemReporter;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.core.rsource.ast.RoxygenScanner;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * Worker for r model manager
 */
public class RReconciler {
	
	
	private static final boolean LOG_TIME= false;
	
	protected static class Data {
		
		public final RSuModelContainer adapter;
		public final SourceContent content;
		
		public SourceParseInput parseInput;
		
		public AstInfo ast;
		
		public IRModelInfo oldModel;
		public IRModelInfo newModel;
		
		public Data(final RSuModelContainer adapter, final IProgressMonitor monitor) {
			this.adapter= adapter;
			this.content= adapter.getParseContent(monitor);
		}
		
	}
	
	
	private final RModelManager rManager;
	protected boolean stop= false;
	
	private final Object f1AstLock= new Object();
	private final IStringCache f1AstStringCache;
	private final RoxygenScanner f1RoxygenScanner;
	
	private final Object f2ModelLock= new Object();
	private final SourceAnalyzer f2ScopeAnalyzer;
	
	private final Object f3ReportLock= new Object();
	private final RProblemReporter f3ProblemReporter;
	
	
	public RReconciler(final RModelManager manager) {
		this.rManager= manager;
		this.f1AstStringCache= new FixInterningStringCache(24);
		this.f1RoxygenScanner= new RoxygenScanner(this.f1AstStringCache);
		this.f2ScopeAnalyzer= new SourceAnalyzer();
		this.f3ProblemReporter= new RProblemReporter();
	}
	
	
	/** for editor reconciling */
	public void reconcile(final RSuModelContainer adapter, final int flags,
			final IProgressMonitor monitor) {
		final IRSourceUnit su= adapter.getSourceUnit();
		final int type= (su.getModelTypeId().equals(RModel.TYPE_ID) ? su.getElementType() : 0);
		if (type == 0) {
			return;
		}
		
		final Data data= new Data(adapter, monitor);
		if (data.content == null) {
			return;
		}
		
		synchronized (this.f1AstLock) {
			if (this.stop || monitor.isCanceled()) {
				return;
			}
			updateAst(data, monitor);
		}
		
		if (this.stop || monitor.isCanceled()
				|| (flags & 0xf) < IModelManager.MODEL_FILE) {
			return;
		}
		
		synchronized (this.f2ModelLock) {
			if (this.stop || monitor.isCanceled()) {
				return;
			}
			final boolean updated= updateModel(data);
			
			if (this.stop) {
				return;
			}
			
			if (updated) {
				this.rManager.getEventJob().addUpdate(su, data.oldModel, data.newModel);
			}
		}
		
		if ((flags & IModelManager.RECONCILER) != 0 && data.newModel != null) {
			if (this.stop || monitor.isCanceled()) {
				return;
			}
			
			IProblemRequestor problemRequestor= null;
			synchronized (this.f3ReportLock) {
				if (!this.stop && !monitor.isCanceled()
						&& data.newModel == adapter.getCurrentModel() ) {
					problemRequestor= adapter.createProblemRequestor(data.ast.stamp);
					if (problemRequestor != null) {
						this.f3ProblemReporter.run(su, data.content,
								(RAstNode) data.ast.root, problemRequestor );
					}
				}
			}
			if (problemRequestor != null) {
				problemRequestor.finish();
			}
		}
	}
	
	public IRModelInfo reconcile(final IRSourceUnit su, final ISourceUnitModelInfo modelInfo,
			final List<? extends RChunkElement> chunkElements, final List<? extends SourceComponent> inlineNodes,
			final int level, final IProgressMonitor monitor) {
		synchronized (this.f2ModelLock) {
			return updateModel(su, modelInfo, chunkElements, inlineNodes);
		}
	}
	
	protected void initParseInput(final Data data) {
		if (data.parseInput == null) {
			if (data.content.getOffset() != 0) {
				data.parseInput= new PartialStringParseInput(data.content.text, data.content.getOffset());
			}
			else {
				data.parseInput= new StringParseInput(data.content.text);
			}
		}
	}
	
	protected final void updateAst(final Data data, final IProgressMonitor monitor) {
		data.ast= data.adapter.getCurrentAst(data.content.stamp);
		
		if (data.ast == null) {
			final long startAst;
			final long stopAst;
			startAst= System.nanoTime();
			
			initParseInput(data);
			final RScanner scanner= new RScanner(data.parseInput, AstInfo.LEVEL_MODEL_DEFAULT,
					this.f1AstStringCache );
			scanner.setCommentLevel(100);
			final SourceComponent sourceComponent= scanner.scanSourceRange(null,
					data.content.getOffset(), data.content.text.length() );
			final AstInfo ast= new AstInfo(scanner.getAstLevel(), data.content.stamp, sourceComponent);
			
			stopAst= System.nanoTime();
			
			this.f1RoxygenScanner.init(data.parseInput);
			this.f1RoxygenScanner.update(sourceComponent);
			
			if (LOG_TIME) {
				System.out.println(this.f1AstStringCache.toString());
				System.out.println("RReconciler/createAST   : " + DecimalFormat.getInstance().format(stopAst-startAst)); //$NON-NLS-1$
			}
			
			synchronized (data.adapter) {
				data.adapter.setAst(ast);
			}
			data.ast= ast;
		}
	}
	
	protected final boolean updateModel(final Data data) {
		// Update Model
		data.newModel= data.adapter.getCurrentModel(data.ast.stamp);
		if (data.newModel == null) {
			final long startModel;
			final long stopModel;
			startModel= System.nanoTime();
			
			final IRModelInfo model= this.f2ScopeAnalyzer.createModel(data.adapter.getSourceUnit(), data.ast);
			final boolean isOK= (model != null);
			
			stopModel= System.nanoTime();
			
			if (LOG_TIME) {
				System.out.println("RReconciler/createMODEL : " + DecimalFormat.getInstance().format(stopModel-startModel)); 
			}
			
			if (isOK) {
				synchronized (data.adapter) {
					data.oldModel= data.adapter.getCurrentModel();
					data.adapter.setModel(model);
				}
				data.newModel= model;
				return true;
			}
		}
		return false;
	}
	
	private IRModelInfo updateModel(final IRSourceUnit su, final ISourceUnitModelInfo modelInfo,
			final List<? extends RChunkElement> chunkElements,
			final List<? extends SourceComponent> inlineNodes) {
		IRModelInfo model;
		try {
			final AstInfo ast= modelInfo.getAst();
			this.f2ScopeAnalyzer.beginChunkSession(su, ast);
			for (final RChunkElement chunkElement : chunkElements) {
				final List<SourceComponent> rootNodes;
				{	final Object source= chunkElement.getAdapter(SourceComponent.class);
					if (source instanceof SourceComponent) {
						rootNodes= ImCollections.newList((SourceComponent) source);
					}
					else if (source instanceof List<?>) {
						rootNodes= (List<SourceComponent>) source;
					}
					else {
						continue;
					}
				}
				this.f2ScopeAnalyzer.processChunk(chunkElement, rootNodes);
			}
			for (final SourceComponent inlineNode : inlineNodes) {
				this.f2ScopeAnalyzer.processInlineNode(inlineNode);
			}
		}
		finally {
			model= this.f2ScopeAnalyzer.stopChunkSession();
		}
		return model;
	}
	
	void stop() {
		this.stop= true;
	}
	
}
