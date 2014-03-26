/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.text.FixInterningStringCache;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.docmlet.tex.core.ast.Embedded;
import de.walware.docmlet.tex.core.model.EmbeddedReconcileItem;
import de.walware.docmlet.tex.core.model.ILtxModelInfo;
import de.walware.docmlet.tex.core.model.ILtxSuModelContainerEmbeddedExtension;
import de.walware.docmlet.tex.core.model.LtxSuModelContainer;
import de.walware.docmlet.tex.core.model.TexModel;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRCompositeSourceElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RProblemReporter;
import de.walware.statet.r.core.rsource.ast.FCall.Arg;
import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.Sweave;


public class LtxRweaveSuModelContainer extends LtxSuModelContainer<ILtxRweaveSourceUnit>
		implements ILtxSuModelContainerEmbeddedExtension {
	
	
	private static IRModelInfo getRModel(final ILtxModelInfo texModel) {
		final List<Object> attachments= texModel.getAttachments();
		for (final Object object : attachments) {
			if (object instanceof IRModelInfo) {
				return (IRModelInfo) object;
			}
		}
		return null;
	}
	
	private static class RReconciler {
		
		private final IRModelManager rManager;
		
		private final IStringCache f1AstStringCache;
		
		private final RProblemReporter f3ProblemReporter;
		
		
		public RReconciler() {
			this.rManager= RCore.getRModelManager();
			
			this.f1AstStringCache= new FixInterningStringCache(24);
			this.f3ProblemReporter= new RProblemReporter();
		}
		
		
		public void reconcileAst(final LtxRweaveSuModelContainer adapter, final SourceContent content,
				final List<Embedded> list, final IProgressMonitor monitor) {
			final SourceParseInput input= new StringParseInput(content.text);
			final RScanner scanner= new RScanner(input, AstInfo.LEVEL_MODEL_DEFAULT, this.f1AstStringCache);
			
			for (final Embedded texNode : list) {
				if (texNode.getText() == RModel.TYPE_ID) {
					final RChunkNode rChunk= new RChunkNode(texNode);
					rChunk.startOffset= texNode.getOffset();
					rChunk.stopOffset= texNode.getStopOffset();
					texNode.setForeignNode(rChunk);
					
					final IRegion startRegion;
					final List<IRegion> rCode= new ArrayList<>(4);
					
					if (texNode.isInline()) {
						startRegion= null;
						rCode.add(new Region(texNode.getOffset(), texNode.getLength()));
					}
					else { // chunk
						input.init(texNode.getOffset(), texNode.getStopOffset());
						int num= 0;
						if (input.get(num+1) == '<' && input.get(num+2) == '<') {
							input.consume(2);
							SEARCH_START: while (true) {
								final int c= input.get(++num);
								switch (c) {
								case SourceParseInput.EOF:
								case '\r':
								case '\n':
									num--;
									break SEARCH_START;
								case '>':
									switch (input.get(num+1)) {
									case '>':
									case '\n':
									case '\r':
										num--;
										break SEARCH_START;
									}
									continue SEARCH_START;
								default:
									continue SEARCH_START;
								}
							}
							final int length= input.getLength(num);
							startRegion= new Region(input.getIndex(), length);
							input.consume(num, length);
							num= 0;
						}
						else {
							startRegion= new Region(texNode.getOffset(), 0);
						}
						boolean newLine= false;
						boolean inR= false;
						SEARCH_SECTIONS: while (true) {
							if (newLine) {
								switch (input.get(++num)) {
								case SourceParseInput.EOF:
									if (inR) {
										final int length= input.getLength(num-1);
										rCode.add(new Region(input.getIndex(), length));
									}
									break SEARCH_SECTIONS;
								case '<':
									if (input.get(num+1) == '<') {
										if (inR) {
											final int length= input.getLength(num-1);
											rCode.add(new Region(input.getIndex(), length));
											input.consume(num-1, length);
											num= 2;
											inR= false;
										}
										newLine= false;
										continue SEARCH_SECTIONS;
									}
									if (!inR) {
										input.consume(num-1);
										num= 1;
										inR= true;
									}
									newLine= false;
									continue SEARCH_SECTIONS;
								case '@':
									if (inR) {
										final int length= input.getLength(num-1);
										rCode.add(new Region(input.getIndex(), length));
										input.consume(num-1, length);
										num= 1;
										inR= false;
									}
									input.consume(num);
									num= 0;
									newLine= false;
									continue SEARCH_SECTIONS;
								case '\r':
								case '\n':
									if (!inR) {
										input.consume(num-1);
										num= 1;
										inR= true;
									}
									continue SEARCH_SECTIONS;
								default:
									if (!inR) {
										input.consume(num-1);
										num= 1;
										inR= true;
									}
									newLine= false;
									continue SEARCH_SECTIONS;
								}
							}
							else {
								switch (input.get(++num)) {
								case SourceParseInput.EOF:
									if (inR) {
										final int length= input.getLength(num-1);
										rCode.add(new Region(input.getIndex(), length));
									}
									break SEARCH_SECTIONS;
								case '\r':
									if (input.get(num+1) == '\n') {
										num++;
									}
									//$FALL-THROUGH$
								case '\n':
									newLine= true;
									continue SEARCH_SECTIONS;
								default:
									continue SEARCH_SECTIONS;
								}
							}
						}
						
					}
					if (startRegion != null) {
						rChunk.weaveArgs= scanner.scanFCallArgs(startRegion.getOffset(), startRegion.getLength(), true);
					}
					final SourceComponent[] rCodeNodes= new SourceComponent[rCode.size()];
					for (int j= 0; j < rCodeNodes.length; j++) {
						final IRegion region= rCode.get(j);
						rCodeNodes[j]= scanner.scanSourceRange(rChunk, region.getOffset(), region.getLength(), true);
					}
					rChunk.rSources= rCodeNodes;
				}
			}
		}
		
		public void reconcileModel(final LtxRweaveSuModelContainer adapter,
				final SourceContent content, final ILtxModelInfo texModel,
				final List<EmbeddedReconcileItem> list,
				final int level, final IProgressMonitor monitor) {
			if (list == null || list.isEmpty()) {
				return;
			}
			
			int chunkCount= 0;
			final List<TexRChunkElement> chunkElements= new ArrayList<>();
			final List<SourceComponent> inlineNodes= new ArrayList<>();
			
			for (final EmbeddedReconcileItem item : list) {
				if (item.getTypeId() == RModel.TYPE_ID) {
					final Embedded texNode= item.getAstNode();
					final RChunkNode rChunk= (RChunkNode) texNode.getForeignNode();
					if (rChunk == null) {
						continue;
					}
					
					if (texNode.isInline()) {
						if (rChunk.rSources.length == 1) {
							inlineNodes.add(rChunk.rSources[0]);
						}
					}
					else {
						chunkCount++;
						
						RElementName name= null;
						IRegion nameRegion= null;
						if (rChunk.weaveArgs != null) {
							final Arg arg= getLabelArg(rChunk.weaveArgs);
							if (arg != null && arg.hasValue()
									&& arg.getValueChild().getNodeType() == NodeType.SYMBOL) {
								final RAstNode nameNode= arg.getValueChild();
								name= RElementName.create(RElementName.MAIN_DEFAULT, nameNode.getText());
								nameRegion= nameNode;
							}
						}
						if (name == null) {
							name= RElementName.create(RElementName.MAIN_OTHER, "#"+Integer.toString(chunkCount)); //$NON-NLS-1$
							nameRegion= new Region(texNode.getOffset()+2, 0);
						}
						final TexRChunkElement element= new TexRChunkElement(item.getModelRefElement(),
								rChunk, name, nameRegion );
						item.setModelTypeElement(element);
						chunkElements.add(element);
					}
				}
			}
			
			if (chunkElements.isEmpty() && inlineNodes.isEmpty()) {
				return;
			}
			
			final IRModelInfo modelInfo= this.rManager.reconcile(adapter.getSourceUnit(), texModel,
					chunkElements, inlineNodes, level, monitor );
			texModel.addAttachment(modelInfo);
			adapter.setRModel(modelInfo);
		}
		
		private Arg getLabelArg(final Args weaveArgs) {
			if (!weaveArgs.hasChildren()) {
				return null;
			}
			for (int i= 0; i < weaveArgs.getChildCount(); i++) {
				final Arg arg= weaveArgs.getChild(i);
				if ((arg.hasName()) ?
						(arg.getNameChild().getNodeType() == NodeType.SYMBOL
								&& "label".equals(arg.getNameChild().getText()) ) : //$NON-NLS-1$
						(i == 0) ) {
					return arg;
				}
			}
			return null;
		}
		
		public void reportEmbeddedProblems(final LtxRweaveSuModelContainer adapter,
				final SourceContent content, final ILtxModelInfo texModel,
				final IProblemRequestor problemRequestor,
				final int level, final IProgressMonitor monitor) {
			final IRModelInfo rModel= getRModel(texModel);
			if (rModel == null) {
				return;
			}
			final ILtxRweaveSourceUnit su= adapter.getSourceUnit();
			final IRLangSourceElement element= rModel.getSourceElement();
			if (element instanceof IRCompositeSourceElement) {
				final List<? extends IRLangSourceElement> elements= ((IRCompositeSourceElement) element)
						.getCompositeElements();
				for (final IRLangSourceElement rChunk : elements) {
					final IAstNode rChunkNode= (IAstNode) rChunk.getAdapter(IAstNode.class);
					this.f3ProblemReporter.run(su, content, rChunkNode, problemRequestor);
				}
			}
		}
		
	}
	
	private static RReconciler RECONCILER;
	
	private static RReconciler getReconciler() {
		synchronized(RReconciler.class) {
			if (RECONCILER == null) {
				RECONCILER= new RReconciler();
			}
			return RECONCILER;
		}
	}
	
	
	private IRModelInfo rModel;
	
	
	public LtxRweaveSuModelContainer(final ILtxRweaveSourceUnit su) {
		super(su);
	}
	
	
	@Override
	public boolean isContainerFor(final String modelTypeId) {
		return (modelTypeId == TexModel.LTX_TYPE_ID || modelTypeId == Sweave.LTX_R_MODEL_TYPE_ID);
	}
	
	@Override
	public String getNowebType() {
		return RModel.TYPE_ID;
	}
	
	
	@Override
	public void reconcileEmbeddedAst(final SourceContent content, final List<Embedded> list,
			final int level, final IProgressMonitor monitor) {
		getReconciler().reconcileAst(this, content, list, monitor);
	}
	
	@Override
	public void reconcileEmbeddedModel(final SourceContent content, final ILtxModelInfo texModel,
			final List<EmbeddedReconcileItem> list,
			final int level, final IProgressMonitor monitor) {
		getReconciler().reconcileModel(this, content, texModel, list, level, monitor);
	}
	
	@Override
	public void reportEmbeddedProblems(final SourceContent content, final ILtxModelInfo texModel,
			final IProblemRequestor problemRequestor,
			final int level, final IProgressMonitor monitor) {
		getReconciler().reportEmbeddedProblems(this, content, texModel, problemRequestor,
				level, monitor );
	}
	
	protected void setRModel(final IRModelInfo model) {
		this.rModel= model;
	}
	
	protected IRModelInfo getCurrentRModel() {
		return this.rModel;
	}
	
	public IRModelInfo getRModelInfo(final int syncLevel, final IProgressMonitor monitor) {
		if (syncLevel >= IModelManager.MODEL_FILE) {
			getModelInfo(syncLevel, monitor);
		}
		return this.rModel;
	}
	
	@Override
	public IProblemRequestor createProblemRequestor(long stamp) {
		if (getMode() == LTK.EDITOR_CONTEXT) {
			return SweavePlugin.getDefault().getRTexDocumentProvider().createProblemRequestor(
					getSourceUnit(), stamp );
		}
		return null;
	}
	
}
