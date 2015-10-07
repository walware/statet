/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.ConsolePageEditor;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.IRDocumentConstants;


/**
 * 
 */
public class RAssistInvocationContext extends AssistInvocationContext {
	
	
	private final RProcess process;
	
	
	public RAssistInvocationContext(final ISourceEditor editor, final int offset, final boolean isProposal,
			final IProgressMonitor monitor) {
		super(editor, offset, (isProposal) ? IModelManager.MODEL_FILE : IModelManager.NONE, monitor);
		
		this.process = determineRProcess();
	}
	
	public RAssistInvocationContext(final ISourceEditor editor, final IRegion region,
			final IProgressMonitor monitor) {
		super(editor, region, IModelManager.MODEL_FILE, monitor);
		
		this.process = determineRProcess();
	}
	
	
	private RProcess determineRProcess() {
		final ISourceEditor editor = getEditor();
		final ITool tool;
		if (editor instanceof ConsolePageEditor) {
			tool = (ITool) editor.getAdapter(ITool.class);
		}
		else {
			tool = NicoUITools.getTool(editor.getWorkbenchPart());
		}
		return (tool instanceof RProcess) ? (RProcess) tool : null;
	}
	
	
	@Override
	protected String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	protected String computeIdentifierPrefix(int offset) {
		final AbstractDocument document = (AbstractDocument) getSourceViewer().getDocument();
		if (offset <= 0 || offset > document.getLength()) {
			return ""; //$NON-NLS-1$
		}
		try {
			final String partitioning= getEditor().getDocumentContentInfo().getPartitioning();
			ITypedRegion partition= document.getPartition(partitioning, offset, true);
			if (partition.getType() == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE) {
				offset = partition.getOffset();
			}
			int start = offset;
			SEARCH_START: while (offset > 0) {
				final char c = document.getChar(offset - 1);
				if (RTokens.isRobustSeparator(c, false)) {
					switch (c) {
					case '$':
					case '@':
						offset --;
						continue SEARCH_START;
					case ' ':
					case '\t':
						if (offset >= 2) {
							final char c2 = document.getChar(offset - 2);
							if ((offset == getInvocationOffset()) ? 
									!RTokens.isRobustSeparator(c, false) :
									(c2 == '$' && c2 == '@')) {
								offset -= 2;
								continue SEARCH_START;
							}
						}
						break SEARCH_START;
					case '`':
						partition= document.getPartition(partitioning, offset - 1, false);
						if (partition.getType() == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE) {
							offset = start = partition.getOffset();
							continue SEARCH_START;
						}
						else {
							break SEARCH_START;
						}
					
					default:
						break SEARCH_START;
					}
				}
				else {
					offset --;
					start = offset;
					continue SEARCH_START;
				}
			}
			
			return document.get(start, getInvocationOffset() - start);
		}
		catch (final BadPartitioningException | BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	
	private static RElementName getElementAccessOfRegion(final RElementAccess access, final IRegion region) {
		int segmentCount = 0;
		RElementAccess current = access;
		while (current != null) {
			segmentCount++;
			final RAstNode nameNode = current.getNameNode();
			if (nameNode != null
					&& nameNode.getOffset() <= region.getOffset()
					&& nameNode.getEndOffset() >= region.getOffset()+region.getLength() ) {
				final RElementName[] segments = new RElementName[segmentCount];
				RElementAccess segment = access;
				for (int i = 0; i < segments.length; i++) {
					if (segment.getSegmentName() == null) {
						return null;
					}
					switch (segment.getType()) {
					case RElementName.MAIN_DEFAULT:
					case RElementName.SUB_NAMEDSLOT:
					case RElementName.SUB_NAMEDPART:
						segments[i] = segment;
						break;
					case RElementName.SUB_INDEXED_S:
					case RElementName.SUB_INDEXED_D:
						return null; // not yet supported
					case RElementName.MAIN_CLASS:
						if (segmentCount != 1) {
							return null;
						}
						segments[i] = segment;
						break;
					default:
//					case RElementName.MAIN_PACKAGE:
//					case RElementName.MAIN_ENV:
						return null;
					}
					segment = segment.getNextSegment();
				}
				return RElementName.concat(new ConstArrayList<>(segments));
			}
			current = current.getNextSegment();
		}
		
		return null;
	}
	
	public RElementName getNameSelection() {
		RAstNode node = (RAstNode) getAstSelection().getCovering();
		if (node == null) {
			return null;
		}
		RElementAccess access = null;
		while (node != null && access == null) {
			if (Thread.interrupted()) {
				return null;
			}
			final List<Object> attachments= node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					node= null;
					access= (RElementAccess) attachment;
					final RElementName e = getElementAccessOfRegion(access, this);
					if (e != null) {
						return e;
					}
					if (Thread.interrupted()) {
						return null;
					}
				}
			}
			if (node != null) {
				node = node.getRParent();
			}
		}
		return null;
	}
	
	
	public RProcess getTool() {
		return this.process;
	}
	
	public IREnv getREnv() {
		if (this.process != null) {
			return (IREnv) this.process.getAdapter(IREnv.class);
		}
		return null;
	}
	
}
