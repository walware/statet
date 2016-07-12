/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.debug.core.breakpoints;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.LTKUtil;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceStructElement;

import de.walware.statet.r.console.core.RDbg;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.internal.debug.core.breakpoints.RGenericLineBreakpoint;
import de.walware.statet.r.internal.debug.core.breakpoints.RGenericLineBreakpoint.CachedData;
import de.walware.statet.r.internal.debug.core.breakpoints.RLineBreakpoint;
import de.walware.statet.r.internal.debug.core.breakpoints.RMethodBreakpoint;
import de.walware.statet.r.nico.IRSrcref;
import de.walware.statet.r.nico.RSrcref;


public class RLineBreakpointValidator {
	
	
	public static class ModelPosition {
		
		private final RGenericLineBreakpoint.CachedData fData;
		
		
		private ModelPosition(final RGenericLineBreakpoint.CachedData data) {
			this.fData= data;
		}
		
		
		public String getElementId() {
			return this.fData.getElementId();
		}
		
		public int[] getRExpressionIndex() {
			return this.fData.getRExpressionIndex();
		}
		
	}
	
	public static ModelPosition getModelPosition(final IRLineBreakpoint breakpoint) {
		if (breakpoint instanceof RLineBreakpoint) {
			final RLineBreakpoint internal= (RLineBreakpoint) breakpoint;
			final CachedData cachedData= internal.getCachedData();
			if (cachedData != null) {
				return new ModelPosition(cachedData);
			}
		}
		return null;
	}
	
	
	private static final int LINE_TOLERANCE= 5;
	
	private static final String TOPLEVEL_ELEMENT_ID= "200:"; // Integer.toHexString(IModelElement.C1_SOURCE) + ':' //$NON-NLS-1$
	
	
	private final IRWorkspaceSourceUnit sourceUnit;
	private final AbstractDocument document;
	
	private IRModelInfo modelInfo;
	
	private String type;
	
	private int originalLine;
	private int line;
	private int startOffset;
	private int endOffset;
	
	private IRLangSourceElement methodElement; // the deepest method element
	private IRLangSourceElement baseElement; // main element, null for script list
	private RAstNode astNode;
	private RAstNode baseExpressionRootNode; // the root for the R expression index
	
	
	public RLineBreakpointValidator(final IRWorkspaceSourceUnit su, final String type,
			final int offset, final IProgressMonitor monitor) {
		this.sourceUnit= su;
		if (!initType(type)
				|| this.sourceUnit.getResource().getType() != IResource.FILE ) {
			this.document= null;
			setInvalid();
			return;
		}
		
		this.document= this.sourceUnit.getDocument(monitor);
		this.originalLine= this.line= this.startOffset= this.endOffset= -1;
		check(offset, monitor);
	}
	
	public RLineBreakpointValidator(final IRWorkspaceSourceUnit su, final IRLineBreakpoint breakpoint,
			final IProgressMonitor monitor) throws CoreException {
		this.sourceUnit= su;
		if (!initType(breakpoint.getBreakpointType())
				|| this.sourceUnit.getResource().getType() != IResource.FILE ) {
			this.document= null;
			setInvalid();
			return;
		}
		
		this.document= this.sourceUnit.getDocument(monitor);
		this.originalLine= this.line= this.startOffset= this.endOffset= -1;
		
		final int offset= breakpoint.getCharStart();
		check(offset, monitor);
		
		if (this.type != null && breakpoint instanceof RGenericLineBreakpoint && su.isSynchronized()) {
			((RGenericLineBreakpoint) breakpoint).setCachedData(new CachedData(
					this.modelInfo.getStamp().getSourceStamp(), computeElementId(), computeRExpressionIndex() ));
		}
	}
	
	
	private boolean initType(final String type) {
		if (type == null) {
			return true;
		}
		if (type.equals(RDebugModel.R_LINE_BREAKPOINT_TYPE_ID)) {
			this.type= RDebugModel.R_LINE_BREAKPOINT_TYPE_ID;
			return true;
		}
		else if (type.equals(RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID)) {
			this.type= RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID;
			return true;
		}
		else {
			return false;
		}
	}
	
	private void check(final int offset, final IProgressMonitor monitor) {
		try {
			this.line= this.originalLine= this.document.getLineOfOffset(offset);
			this.methodElement= searchMethodElement(offset, monitor);
			if (this.type == null) { // best
				if (this.methodElement != null
						&& this.document.getLineOfOffset(this.methodElement.getSourceRange().getOffset()) == this.line ) {
					this.type= RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID;
				}
				else {
					this.type= RDebugModel.R_LINE_BREAKPOINT_TYPE_ID;
				}
			}
			
			if (this.type == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
				IRegion lineInformation= this.document.getLineInformation(this.line);
				final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
						this.sourceUnit.getDocumentContentInfo() );
				scanner.configure(this.document, IRDocumentConstants.R_CODE_CONTENT_CONSTRAINT);
				{	final IRegion lastLineInformation= this.document.getLineInformation(
							Math.min(this.line + LINE_TOLERANCE, this.document.getNumberOfLines()-1) );
					this.startOffset= scanner.findNonBlankForward(
							lineInformation.getOffset(),
							lastLineInformation.getOffset() + lastLineInformation.getLength(),
							true );
				}
				if (this.startOffset < 0) {
					setInvalid();
					return;
				}
				
				this.astNode= searchSuspendAstNode(this.startOffset, monitor);
				if (this.astNode == null) {
					setInvalid();
					return;
				}
				this.startOffset= this.astNode.getOffset();
				if (this.startOffset < 0) {
					setInvalid();
					return;
				}
				
				this.line= this.document.getLineOfOffset(this.startOffset);
				if (this.line != this.originalLine) {
					lineInformation= this.document.getLineInformation(this.line);
				}
				
				this.endOffset= scanner.findNonBlankBackward(
						lineInformation.getOffset() + lineInformation.getLength(),
						this.startOffset - 1, true);
				if (this.endOffset < 0) { // should never happen
					setInvalid();
					return;
				}
				
				if (this.methodElement != null
						&& this.methodElement.getSourceRange().getOffset() != this.startOffset) {
					this.baseElement= searchBaseElement(this.methodElement);
					if (this.baseElement == null) {
						setInvalid();
						return;
					}
					
					this.baseExpressionRootNode= this.baseElement.getAdapter(FDef.class).getContChild();
					if (!isBaseExpressionRootNodeValid()) {
//						new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, "Only in blocks.");
						setInvalid();
						return;
					}
				}
				else { // script line
					this.baseExpressionRootNode= this.astNode.getRRoot();
				}
			}
			else if (this.type == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
				if (this.methodElement != null) {
					this.startOffset= this.methodElement.getSourceRange().getOffset();
					if (this.startOffset < 0) {
						setInvalid();
						return;
					}
					this.line= this.document.getLineOfOffset(this.startOffset);
					final IRegion lineInformation= this.document.getLineInformation(this.line);
					
					final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
							this.sourceUnit.getDocumentContentInfo() );
					scanner.configure(this.document, IRDocumentConstants.R_CODE_CONTENT_CONSTRAINT);
					
					this.endOffset= scanner.findNonBlankBackward(
							Math.min(lineInformation.getOffset() + lineInformation.getLength(),
									this.startOffset + this.methodElement.getSourceRange().getLength() ),
							this.startOffset - 1, true );
					if (this.endOffset < 0) {
						setInvalid();
						return;
					}
					
					this.baseElement= searchBaseElement(this.methodElement);
					if (this.baseElement == null) {
						setInvalid();
						return;
					}
					
					if (this.baseElement != this.methodElement) {
						this.astNode= this.methodElement.getAdapter(FDef.class).getContChild();
						if (this.astNode == null) {
							setInvalid();
							return;
						}
						this.baseExpressionRootNode= this.baseElement.getAdapter(FDef.class).getContChild();
						if (!isBaseExpressionRootNodeValid()) {
//							new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, "Only in blocks.");
							setInvalid();
							return;
						}
					}
				}
				else {
					setInvalid();
					return;
				}
			}
			else {
				throw new IllegalStateException(this.type);
			}
		}
		catch (final BadLocationException e) {
			setInvalid();
		}
	}
	
	private boolean isBaseExpressionRootNodeValid() {
		return (this.baseExpressionRootNode != null
				&& this.baseExpressionRootNode.getNodeType() == NodeType.BLOCK
				&& RAst.isParentChild(this.baseExpressionRootNode, this.astNode) );
	}
	
	private IRModelInfo getModelInfo(final IProgressMonitor monitor) {
		if (this.modelInfo == null) {
			this.modelInfo= (IRModelInfo) this.sourceUnit.getModelInfo(RModel.R_TYPE_ID,
					IModelManager.MODEL_FILE, monitor );
		}
		return this.modelInfo;
	}
	
	private void setInvalid() {
		this.type= null;
	}
	
	
	private IRLangSourceElement searchMethodElement(final int offset, final IProgressMonitor monitor)
			throws BadLocationException {
		final IRModelInfo modelInfo= getModelInfo(monitor);
		if (modelInfo == null) {
			return null;
		}
		
		final IRegion lineInformation= this.document.getLineInformationOfOffset(offset);
		final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
				this.sourceUnit.getDocumentContentInfo() );
		scanner.configure(this.document, IRDocumentConstants.R_CODE_CONTENT_CONSTRAINT);
		int charStart= scanner.findNonBlankForward(
				lineInformation.getOffset(),
				lineInformation.getOffset() + lineInformation.getLength(),
				true);
		if (charStart < 0) {
			charStart= offset;
		}
		ISourceStructElement element= LTKUtil.getCoveringSourceElement(
				modelInfo.getSourceElement(), charStart, charStart );
		
		while (element != null) {
			if (element instanceof IRLangSourceElement
					&& (element.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD) {
				return (IRLangSourceElement) element;
			}
			element= element.getSourceParent();
		}
		return null;
	}
	
	private RAstNode searchSuspendAstNode(final int offset, final IProgressMonitor monitor) {
		final IRModelInfo modelInfo= getModelInfo(monitor);
		if (modelInfo == null) {
			return null;
		}
		
		final IAstNode astNode= AstSelection.search(modelInfo.getAst().root,
				offset, offset, AstSelection.MODE_COVERING_SAME_FIRST).getCovering();
		if (astNode instanceof RAstNode) {
			RAstNode rNode= (RAstNode) astNode;
			if (rNode.getOffset() < offset) {
				final AtomicReference<RAstNode> ref= new AtomicReference<>();
				try {
					rNode.acceptInR(new GenericVisitor() {
						@Override
						public void visitNode(final RAstNode node) throws InvocationTargetException {
							if (ref.get() != null) {
								return;
							}
							if (node.getOffset() >= offset) {
								ref.set(node);
								return;
							}
							if (node.getEndOffset() >= offset) {
								node.acceptInRChildren(this);
							}
						}
					});
				}
				catch (final InvocationTargetException e) {}
				if (ref.get() != null) {
					return ref.get();
				}
			}
			else {
				RAstNode rParent;
				while ((rParent= rNode.getRParent()) != null && rParent.getOffset() >= offset) {
					rNode= rParent;
				}
			}
			return rNode;
		}
		return null;
	}
	
	private IRLangSourceElement searchBaseElement(IRLangSourceElement element) {
		while (element != null) {
			final ISourceStructElement parent= element.getSourceParent();
			if (!(parent instanceof IRLangSourceElement)) {
				return null;
			}
			if ((parent.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_SOURCE) {
				if ((element.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD
						&& element.getAdapter(FDef.class) != null) {
					return element;
				}
				return null;
			}
			element= (IRLangSourceElement) parent;
		}
//		while (element != null) {
//			IRFrame frame= (IRFrame) element.getAdapter(IRFrame.class);
//			if (frame == null) {
//				return null;
//			}
//			switch (frame.getFrameType()) {
//			case IRFrame.FUNCTION:
//				element= element.getSourceParent();
//				continue;
//			case IRFrame.PROJECT:
//			case IRFrame.PACKAGE:
//				return element;
//			default:
//				return null;
//			}
//		}
		return null;
	}
	
	
	public String getType() {
		return this.type;
	}
	
	/**
	 * Returns the line number of the original specified offset.
	 * 
	 * @return the line number (1-based)
	 */
	public int getOriginalLineNumber() {
		return (this.originalLine + 1);
	}
	
	/**
	 * Returns the line number of the found breakpoint position.
	 * 
	 * @return the line number (1-based)
	 */
	public int getLineNumber() {
		return (this.line >= 0) ? (this.line + 1) : -1;
	}
	
	/**
	 * Returns the offset of the start of the breakpoint region.
	 * 
	 * @return start offset in the document
	 */
	public int getCharStart() {
		return this.startOffset;
	}
	
	/**
	 * Returns the offset of the end of the breakpoint region.
	 * 
	 * @return end offset in the document
	 */
	public int getCharEnd() {
		return (this.endOffset >= 0) ? (this.endOffset + 1) : -1;
	}
	
	public ISourceStructElement getMethodElement() {
		return this.methodElement;
	}
	
	public ISourceStructElement getBaseElement() {
		return this.baseElement;
	}
	
	public RAstNode getAstNode() {
		return this.astNode;
	}
	
	
	public int computeElementType() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		if (this.baseElement != null) {
			if ((this.baseElement.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD) {
				if (this.baseElement.getElementType() == IRElement.R_S4METHOD) {
					return IRLineBreakpoint.R_S4_METHOD_ELEMENT_TYPE;
				}
				return IRLineBreakpoint.R_COMMON_FUNCTION_ELEMENT_TYPE;
			}
		}
		else { // script line
			return IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE;
		}
		return -1;
	}
	
	public String computeElementId() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		if (this.baseElement != null) {
			return RDbg.getElementId(this.baseElement);
		}
		else { // script line
			return TOPLEVEL_ELEMENT_ID;
		}
	}
	
	public String computeElementLabel() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		if (this.baseElement != null) {
			return getLabel(this.baseElement);
		}
		else { // script line
			try {
				return this.document.get(getCharStart(), getCharEnd()-getCharStart());
			}
			catch (final BadLocationException e) {
				return null;
			}
		}
	}
	
	public String computeSubLabel() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		if (this.baseElement != null) {
			RAstNode astNode= this.astNode;
			while (astNode != null && astNode.getNodeType() != NodeType.F_DEF) {
				astNode= astNode.getRParent();
			}
			if (astNode != null && (this.methodElement == null 
					|| (astNode != this.methodElement.getAdapter(FDef.class)
							&& astNode.getOffset() > this.methodElement.getSourceRange().getOffset() ))) {
				return "<unnamed>";
			}
			else if (this.methodElement != null && this.methodElement != this.baseElement) {
				return getLabel(this.methodElement);
			}
			else {
				return null;
			}
		}
		else { // script line
			return null;
		}
	}
	
	private String getLabel(final ISourceStructElement element) {
		return element.getElementName().toString();
	}
	
	public IRSrcref computeElementSrcref() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		try {
			if (this.baseElement != null) {
				final FDef astNode= this.baseElement.getAdapter(FDef.class);
				if (astNode != null) {
					return new RSrcref(this.document, astNode.getContChild());
				}
			}
			else {
				if (this.baseExpressionRootNode != null) {
					return new RSrcref(this.document, this.baseExpressionRootNode);
				}
			}
			return null;
		}
		catch (final BadLocationException e) {
			throw failedComputing(e);
		}
	}
	
	public int[] computeRExpressionIndex() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		if (this.astNode != null && this.baseExpressionRootNode != null) {
			return RAst.computeRExpressionIndex(this.astNode, this.baseExpressionRootNode);
		}
		else {
			return null;
		}
	}
	
	public IRSrcref computeRExpressionSrcref() throws CoreException {
		if (this.type == null) {
			throw invalid();
		}
		if (this.astNode != null && this.baseExpressionRootNode != null) {
			try {
				return new RSrcref(this.document, this.astNode);
			}
			catch (final BadLocationException e) {
				throw failedComputing(e);
			}
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Creates a breakpoint with the found specifications.
	 * 
	 * @param monitor
	 */
	public IRBreakpoint createBreakpoint(final IProgressMonitor monitor) {
		if (this.type == null) {
//			new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, "No valid breakpoint position.");
			return null;
		}
		else if (this.type == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
			try {
				final String elementId= computeElementId();
				final RLineBreakpoint internal= new RLineBreakpoint(this.sourceUnit.getResource(),
						getLineNumber(), getCharStart(), getCharEnd(),
						computeElementType(), elementId, computeElementLabel(), computeSubLabel(),
						false );
				internal.setCachedData(new CachedData(
						this.modelInfo.getStamp().getSourceStamp(), elementId,
						computeRExpressionIndex() ));
				return internal;
			}
			catch (final Exception e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
						"An error occurred when creating R line breakpoint from validation data\n" + toString(),
						e ));
//				new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
//						"Creating R line breakpoint failed.");
				return null;
			}
		}
		else if (this.type == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
			try {
				final String elementId= computeElementId();
				final RMethodBreakpoint internal= new RMethodBreakpoint(this.sourceUnit.getResource(),
						getLineNumber(), getCharStart(), getCharEnd(),
						computeElementType(), elementId, computeElementLabel(), computeSubLabel(),
						false );
				internal.setCachedData(new CachedData(
						this.modelInfo.getStamp().getSourceStamp(), elementId,
						computeRExpressionIndex() ));
				return internal;
			}
			catch (final Exception e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
						"An error occurred when creating R method breakpoint from validation data\n" + toString(),
						e ));
//				new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID,
//						"Create R method breakpoint failed.");
				return null;
			}
		}
		throw new IllegalStateException("type= " + this.type);
	}
	
	public void updateBreakpoint(final IRBreakpoint breakpoint) throws CoreException {
		if (this.type != breakpoint.getBreakpointType()) {
			throw new IllegalArgumentException(this.type);
		}
		if (!(breakpoint instanceof IRLineBreakpoint)) {
			throw new IllegalArgumentException(breakpoint.getClass().getName());
		}
		final IMarker marker= breakpoint.getMarker();
		final String elementId= computeElementId();
		RGenericLineBreakpoint.updatePosition(marker,
				getLineNumber(), getCharStart(), getCharEnd() );
		RGenericLineBreakpoint.updateElementInfo(marker,
				computeElementType(), elementId, computeElementLabel(), computeSubLabel() );
		if (breakpoint instanceof RGenericLineBreakpoint) {
			((RGenericLineBreakpoint) breakpoint).setCachedData(new CachedData(
					this.modelInfo.getStamp().getSourceStamp(), elementId,
					computeRExpressionIndex() ));
		}
	}
	
	private CoreException invalid() {
		return new CoreException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
				"Validation result was negative.", null ));
	}
	
	private CoreException failedComputing(final Throwable e) {
		return new CoreException(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
				"An error occurred when computing breakpoint data.", e ));
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb= new StringBuilder(getClass().getName());
		sb.append("\n").append("validator result:");
		sb.append("\n\t").append("type= ").append((this.type != null) ? this.type : "<no valid position found>");
		sb.append("\n\t").append("lineNumber= ").append(getLineNumber());
		sb.append("\n\t").append("charStart= ").append(getCharStart());
		sb.append("\n\t").append("charEnd= ").append(getCharEnd());
		return sb.toString();
	}
	
}
