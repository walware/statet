/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.text.core.IFragmentDocument;
import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.ConsolePageEditor;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.util.LoadReferencesUtil;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.ui.editors.IRSourceEditor;


/**
 * AssistInvocationContext for R
 */
public class RAssistInvocationContext extends AssistInvocationContext {
	
	
	public class FCallInfo {
		
		private final FCall node;
		
		private final RElementAccess access;
		
		private RFrameSearchPath searchPath;
		
		
		public FCallInfo(final FCall node, final RElementAccess access) {
			this.node= node;
			this.access= access;
		}
		
		
		public FCall getNode() {
			return this.node;
		}
		
		public RElementAccess getAccess() {
			return this.access;
		}
		
		private RFrameSearchPath createSearchPath(final int mode) {
			final RFrameSearchPath searchPath= new RFrameSearchPath();
			final RAstNode parent= this.node.getRParent();
			searchPath.init(RAssistInvocationContext.this, (parent != null) ? parent : this.node,
					mode, getAccess().getScope() );
			return searchPath;
		}
		
		public RFrameSearchPath getSearchPath(final int mode) {
			final int defaultMode= getDefaultRFrameSearchMode();
			if (mode == 0 || mode == defaultMode) {
				if (this.searchPath == null) {
					this.searchPath= createSearchPath(defaultMode);
				}
				return this.searchPath;
			}
			else {
				return createSearchPath(mode);
			}
		}
		
		public int getArgIdx(final int offset) {
			if (offset <= this.node.getArgsOpenOffset()
					|| (this.node.getArgsCloseOffset() != Integer.MIN_VALUE 
							&& offset > this.node.getArgsCloseOffset() )) {
				return -1;
			}
			final FCall.Args args= this.node.getArgsChild();
			final int last= args.getChildCount() - 1;
			if (last < 0) {
				return 0;
			}
			for (int argIdx= 0; argIdx < last; argIdx++) {
				if (args.getSeparatorOffset(argIdx) >= offset) {
					return argIdx;
				}
			}
			return last;
		}
		
		public int getArgBeginOffset(final int argIdx) {
			if (argIdx < 0) {
				return IAstNode.NA_OFFSET;
			}
			final int sep= (argIdx == 0) ?
					this.node.getArgsOpenOffset() :
					this.node.getArgsChild().getSeparatorOffset(argIdx - 1);
			return sep + 1;
		}
		
		public FCall.Arg getArg(final int argIdx) {
			if (argIdx < 0) {
				return null;
			}
			final FCall.Args args= this.node.getArgsChild();
			return (argIdx < args.getChildCount()) ? args.getChild(argIdx) : null;
		}
		
	}
	
	
	private static final byte PARSE_OPERATOR=               1 << 0;
	private static final byte PARSE_SYMBOL=                 1 << 1;
	
	private static final char[] F_BRACKETS= new char[] { '(', ')' };
	
	
	private RHeuristicTokenScanner scanner;
	
	private RElementName prefixName;
	
	private int prefixLastSegmentOffset= -1;
	
	
	private final RProcess tool;
	
	private LoadReferencesUtil toolReferencesUtil;
	
	
	public RAssistInvocationContext(final IRSourceEditor editor,
			final int offset, final String contentType,
			final boolean isProposal,
			final RHeuristicTokenScanner scanner,
			final IProgressMonitor monitor) {
		super(editor, offset, contentType,
				(isProposal) ? IModelManager.MODEL_FILE : IModelManager.NONE, monitor );
		
		this.scanner= scanner;
		
		this.tool= determineRProcess();
	}
	
	public RAssistInvocationContext(final IRSourceEditor editor,
			final IRegion region, final String contentType,
			final RHeuristicTokenScanner scanner,
			final IProgressMonitor monitor) {
		super(editor, region, contentType, IModelManager.MODEL_FILE, monitor);
		
		this.scanner= scanner;
		
		this.tool= determineRProcess();
	}
	
	
	private RProcess determineRProcess() {
		final ISourceEditor editor= getEditor();
		final ITool tool;
		if (editor instanceof ConsolePageEditor) {
			tool= (ITool) editor.getAdapter(ITool.class);
		}
		else {
			tool= NicoUITools.getTool(editor.getWorkbenchPart());
		}
		return (tool instanceof RProcess) ? (RProcess) tool : null;
	}
	
	
	@Override
	protected boolean reuse(final ISourceEditor editor, final int offset) {
		if (super.reuse(editor, offset)) {
			if (this.toolReferencesUtil != null) {
				this.toolReferencesUtil.setWaitTimeout(getToolReferencesWaitTimeout());
			}
			return true;
		}
		return false;
	}
	
	
	@Override
	protected String getModelTypeId() {
		return RModel.R_TYPE_ID;
	}
	
	@Override
	public IRSourceEditor getEditor() {
		return (IRSourceEditor) super.getEditor();
	}
	
	@Override
	public IRSourceUnit getSourceUnit() {
		return (IRSourceUnit) super.getSourceUnit();
	}
	
	
	@Override
	protected String computeIdentifierPrefix(final int endOffset)
			throws BadPartitioningException, BadLocationException {
		final AbstractDocument document= (AbstractDocument) getDocument();
		
		if (endOffset < 0 || endOffset > document.getLength()) {
			throw new BadLocationException("offset= " + endOffset); //$NON-NLS-1$
		}
		if (endOffset == 0) {
			return ""; //$NON-NLS-1$
		}
		
		int offset= endOffset;
		byte currentMode= (PARSE_SYMBOL | PARSE_OPERATOR);
		byte validModes= (PARSE_SYMBOL | PARSE_OPERATOR);
		final String partitioning= getEditor().getDocumentContentInfo().getPartitioning();
		ITypedRegion partition= document.getPartition(partitioning, offset, true);
		if (partition.getType() == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE
				|| partition.getType() == IRDocumentConstants.R_STRING_CONTENT_TYPE) {
			offset= partition.getOffset();
			currentMode= PARSE_OPERATOR;
		}
		int beginOffset= offset;
		SEARCH_START: while (offset > 0) {
			final char c= document.getChar(offset - 1);
			if (RTokens.isRobustSeparator(c)) {
				switch (c) {
				case '$':
				case '@':
					if ((currentMode & PARSE_OPERATOR) != 0) {
						offset--;
						beginOffset= offset;
						currentMode= (byte) (validModes & PARSE_SYMBOL);
						continue SEARCH_START;
					}
					break SEARCH_START;
				case ':':
					if ((currentMode & PARSE_OPERATOR) != 0
							&& offset >= 2 && document.getChar(offset - 2) == ':') {
						if (offset >= 3 && document.getChar(offset - 3) == ':') {
							offset-= 3;
						}
						else {
							offset-= 2;
						}
						validModes&= ~PARSE_OPERATOR;
						currentMode= (byte) (validModes & PARSE_SYMBOL);
						continue SEARCH_START;
					}
					break SEARCH_START;
//					case ' ':
//					case '\t':
//						if (offset >= 2) {
//							final char c2= document.getChar(offset - 2);
//							if ((offset == getInvocationOffset()) ? 
//									!RTokens.isRobustSeparator(c2, false) :
//									(c2 == '$' && c2 == '@')) {
//								offset-= 2;
//								continue SEARCH_START;
//							}
//						}
//						break SEARCH_START;
				case '`':
					if ((currentMode & PARSE_SYMBOL) != 0) {
						partition= document.getPartition(partitioning, offset - 1, false);
						if (partition.getType() == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE) {
							offset= partition.getOffset();
							beginOffset= offset;
							currentMode= (byte) (validModes & PARSE_OPERATOR);
							continue SEARCH_START;
						}
					}
					break SEARCH_START;
				
				default:
					break SEARCH_START;
				}
			}
			else {
				if ((currentMode & PARSE_SYMBOL) != 0) {
					offset--;
					beginOffset= offset;
					currentMode|= (byte) (validModes & PARSE_OPERATOR);
					continue SEARCH_START;
				}
				break SEARCH_START;
			}
		}
		
		return document.get(beginOffset, endOffset - beginOffset);
	}
	
	protected int computeIdentifierPrefixLastSegmentOffset(final int endOffset)
			throws BadPartitioningException, BadLocationException {
		final AbstractDocument document= (AbstractDocument) getDocument();
		
		if (endOffset < 0 || endOffset > document.getLength()) {
			throw new BadLocationException("endOffset= " + endOffset); //$NON-NLS-1$
		}
		if (endOffset == 0) {
			return 0;
		}
		
		int offset= endOffset;
		final String partitioning= getEditor().getDocumentContentInfo().getPartitioning();
		final ITypedRegion partition= document.getPartition(partitioning, offset, true);
		if (partition.getType() == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE
				|| partition.getType() == IRDocumentConstants.R_STRING_CONTENT_TYPE) {
			return partition.getOffset();
		}
		int beginOffset= offset;
		SEARCH_START: while (offset > 0) {
			final char c= document.getChar(offset - 1);
			if (RTokens.isRobustSeparator(c, false)) {
				break SEARCH_START;
			}
			else {
				offset--;
				beginOffset= offset;
			}
		}
		
		return beginOffset;
	}
	
	public RElementName getIdentifierElementName() {
		if (this.prefixName == null) {
			this.prefixName= RElementName.parseDefault(getIdentifierPrefix());
		}
		return this.prefixName;
	}
	
	public int getIdentifierLastSegmentOffset() {
		if (this.prefixLastSegmentOffset < 0) {
			try {
				this.prefixLastSegmentOffset= computeIdentifierPrefixLastSegmentOffset(
						getInvocationOffset() );
			}
			catch (final BadPartitioningException | BadLocationException e) {
				this.prefixLastSegmentOffset= getInvocationOffset();
				throw new RuntimeException(e);
			}
		}
		return this.prefixLastSegmentOffset;
	}
	
	
	public final RHeuristicTokenScanner getRHeuristicTokenScanner() {
		if (this.scanner == null) {
			this.scanner= RHeuristicTokenScanner.create(getEditor().getDocumentContentInfo());
		}
		return this.scanner;
	}
	
	private static RElementName getElementAccessOfRegion(final RElementAccess access, final IRegion region) {
		RElementAccess current= access;
		while (current != null) {
			if (current.getSegmentName() == null) {
				return null;
			}
			switch (current.getType()) {
			case RElementName.SCOPE_NS:
			case RElementName.SCOPE_NS_INT:
			case RElementName.SCOPE_SEARCH_ENV:
			case RElementName.SCOPE_PACKAGE:
			
			case RElementName.MAIN_DEFAULT:
			case RElementName.MAIN_CLASS:
			
			case RElementName.SUB_NAMEDSLOT:
			case RElementName.SUB_NAMEDPART:
				break;
			default:
				return null;
			}
			
			final RAstNode nameNode= current.getNameNode();
			if (nameNode != null
					&& nameNode.getOffset() <= region.getOffset()
					&& nameNode.getEndOffset() >= region.getOffset() + region.getLength() ) {
				return RElementName.create(access, current.getNextSegment(), true);
			}
			current= current.getNextSegment();
		}
		
		return null;
	}
	
	public RElementName getNameSelection() {
		final IAstNode selectedNode= getAstSelection().getCovering();
		if (selectedNode instanceof RAstNode) {
			RAstNode node= (RAstNode) selectedNode;
			RElementAccess access= null;
			while (node != null && access == null) {
				if (Thread.interrupted()) {
					return null;
				}
				final List<Object> attachments= node.getAttachments();
				for (final Object attachment : attachments) {
					if (attachment instanceof RElementAccess) {
						node= null;
						access= (RElementAccess) attachment;
						final RElementName e= getElementAccessOfRegion(access, this);
						if (e != null) {
							return e;
						}
						if (Thread.interrupted()) {
							return null;
						}
					}
				}
				if (node != null) {
					node= node.getRParent();
				}
			}
		}
		return null;
	}
	
	
	public FCallInfo getFCallInfo() {
		final RHeuristicTokenScanner scanner= getRHeuristicTokenScanner();
		
		int offset= getIdentifierOffset();
		IDocument document= getDocument();
		int offsetShift= 0;
		if (document instanceof IFragmentDocument) {
			final IFragmentDocument inputDoc= (IFragmentDocument) document;
			document= inputDoc.getMasterDocument();
			offsetShift= inputDoc.getOffsetInMasterDocument();
			offset+= offsetShift;
		}
		
		if (scanner == null || offset < 2) {
			return null;
		}
		
		scanner.configureDefaultParitions(document);
		if (IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT.matches(scanner.getPartition(offset - 1).getType())) {
			final int index= scanner.findOpeningPeer(offset - 1, F_BRACKETS);
			if (index >= 0) {
				return searchFCallInfo(index - offsetShift);
			}
		}
		return null;
	}
	
	private FCallInfo searchFCallInfo(final int openOffset) {
		final AstInfo astInfo= getAstInfo();
		if (astInfo == null || astInfo.root == null) {
			return null;
		}
		final AstSelection selection= AstSelection.search(astInfo.root,
				openOffset, openOffset + 1, AstSelection.MODE_COVERING_SAME_LAST );
		IAstNode node= selection.getCovering();
		
		while (node != null && node instanceof RAstNode) {
			final RAstNode rnode= (RAstNode) node;
			FCall fcallNode= null;
			if (rnode.getNodeType() == NodeType.F_CALL
					&& (openOffset == (fcallNode= ((FCall) rnode)).getArgsOpenOffset())) {
				final List<Object> attachments= fcallNode.getAttachments();
				for (final Object attachment : attachments) {
					if (attachment instanceof RElementAccess) {
						final RElementAccess fcallAccess= (RElementAccess) attachment;
						if (fcallAccess.isFunctionAccess() && !fcallAccess.isWriteAccess()) {
							return new FCallInfo(fcallNode, fcallAccess);
						}
					}
				}
			}
			node= rnode.getParent();
		}
		return null;
	}
	
	
	public RProcess getTool() {
		return this.tool;
	}
	
	public boolean isToolConsole() {
		return (getEditor() instanceof ConsolePageEditor);
	}
	
	public LoadReferencesUtil getToolReferencesUtil() {
		if (this.toolReferencesUtil == null && this.tool != null) {
			this.toolReferencesUtil= new LoadReferencesUtil(this.tool, getToolReferencesWaitTimeout()) {
				@Override
				protected void allFinished(final ImList<ICombinedRElement> resolvedElements) {
					if (!resolvedElements.isEmpty()) {
						RAssistInvocationContext.this.toolReferencesResolved(resolvedElements);
					}
				}
			};
		}
		return this.toolReferencesUtil;
	}
	
	protected int getToolReferencesWaitTimeout() {
		return LoadReferencesUtil.MAX_EXPLICITE_WAIT;
	}
	
	protected void toolReferencesResolved(final ImList<ICombinedRElement> resolvedElements) {
	}
	
	
	public int getDefaultRFrameSearchMode() {
		return (isToolConsole()) ? RFrameSearchPath.CONSOLE_MODE : RFrameSearchPath.WORKSPACE_MODE;
	}
	
}
