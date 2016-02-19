/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.string.IStringFactory;
import de.walware.jcommons.string.InternStringCache;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.impl.SourceModelStamp;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.text.core.input.OffsetStringParserInput;
import de.walware.ecommons.text.core.input.StringParserInput;
import de.walware.ecommons.text.core.input.TextParserInput;

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
	
	private final Object raLock= new Object();
	private final IStringFactory raAstStringCache;
	private final StringParserInput raInput= new StringParserInput(0x1000);
	private final RoxygenScanner raRoxygenScanner;
	
	private final Object rmLock= new Object();
	private final SourceAnalyzer rmScopeAnalyzer;
	
	private final Object rpLock= new Object();
	private final RProblemReporter rpReporter;
	
	
	public RReconciler(final RModelManager manager) {
		this.rManager= manager;
		this.raAstStringCache= new InternStringCache(0x20);
		this.raRoxygenScanner= new RoxygenScanner(this.raAstStringCache);
		this.rmScopeAnalyzer= new SourceAnalyzer();
		this.rpReporter= new RProblemReporter();
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
		
		synchronized (this.raLock) {
			if (this.stop || monitor.isCanceled()) {
				return;
			}
			updateAst(data, monitor);
		}
		
		if (this.stop || monitor.isCanceled()
				|| (flags & 0xf) < IModelManager.MODEL_FILE) {
			return;
		}
		
		synchronized (this.rmLock) {
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
		
		if ((flags & IModelManager.RECONCILE) != 0 && data.newModel != null) {
			if (this.stop || monitor.isCanceled()) {
				return;
			}
			
			IProblemRequestor problemRequestor= null;
			synchronized (this.rpLock) {
				if (!this.stop && !monitor.isCanceled()
						&& data.newModel == adapter.getCurrentModel() ) {
					problemRequestor= adapter.createProblemRequestor();
					if (problemRequestor != null) {
						this.rpReporter.run(su, data.content,
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
		synchronized (this.rmLock) {
			return updateModel(su, modelInfo, chunkElements, inlineNodes);
		}
	}
	
	protected final void updateAst(final Data data, final IProgressMonitor monitor) {
		final SourceModelStamp stamp= new SourceModelStamp(data.content.getStamp());
		
		data.ast= data.adapter.getCurrentAst();
		if (data.ast != null && !stamp.equals(data.ast.getStamp())) {
			data.ast= null;
		}
		
		if (data.ast == null) {
			final long startAst;
			final long stopAst;
			startAst= System.nanoTime();
			
			final TextParserInput input;
			if (data.content.getBeginOffset() != 0) {
				input= new OffsetStringParserInput(data.content.getText(), data.content.getBeginOffset());
			}
			else {
				input= raInput.reset(data.content.getText());
			}
			
			final RScanner scanner= new RScanner(AstInfo.LEVEL_MODEL_DEFAULT,
					this.raAstStringCache );
			scanner.setCommentLevel(100);
			final SourceComponent sourceComponent= scanner.scanSourceRange(
					input.init(data.content.getBeginOffset(), data.content.getEndOffset()),
					null );
			data.ast= new AstInfo(scanner.getAstLevel(), stamp, sourceComponent);
			
			stopAst= System.nanoTime();
			
			this.raRoxygenScanner.init(
					input.init(data.content.getBeginOffset(), data.content.getEndOffset()));
			this.raRoxygenScanner.update(sourceComponent);
			
			if (LOG_TIME) {
				System.out.println(this.raAstStringCache.toString());
				System.out.println("RReconciler/createAST   : " + DecimalFormat.getInstance().format(stopAst-startAst)); //$NON-NLS-1$
			}
			
			synchronized (data.adapter) {
				data.adapter.setAst(data.ast);
			}
		}
	}
	
	protected final boolean updateModel(final Data data) {
		data.newModel= data.adapter.getCurrentModel();
		if (data.newModel != null && !data.ast.getStamp().equals(data.newModel.getStamp())) {
			data.newModel= null;
		}
		
		if (data.newModel == null) {
			final long startModel;
			final long stopModel;
			startModel= System.nanoTime();
			
			final IRModelInfo model= this.rmScopeAnalyzer.createModel(data.adapter.getSourceUnit(), data.ast);
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
			this.rmScopeAnalyzer.beginChunkSession(su, ast);
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
				this.rmScopeAnalyzer.processChunk(chunkElement, rootNodes);
			}
			for (final SourceComponent inlineNode : inlineNodes) {
				this.rmScopeAnalyzer.processInlineNode(inlineNode);
			}
		}
		finally {
			model= this.rmScopeAnalyzer.stopChunkSession();
		}
		return model;
	}
	
	void stop() {
		this.stop= true;
	}
	
}
