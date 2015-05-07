/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
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
			fData = data;
		}
		
		
		public String getElementId() {
			return fData.getElementId();
		}
		
		public int[] getRExpressionIndex() {
			return fData.getRExpressionIndex();
		}
		
	}
	
	public static ModelPosition getModelPosition(final IRLineBreakpoint breakpoint) {
		if (breakpoint instanceof RLineBreakpoint) {
			final RLineBreakpoint internal = (RLineBreakpoint) breakpoint;
			final CachedData cachedData = internal.getCachedData();
			if (cachedData != null) {
				return new ModelPosition(cachedData);
			}
		}
		return null;
	}
	
	
	private static final int LINE_TOLERANCE = 5;
	
	
	private final IRWorkspaceSourceUnit fSourceUnit;
	private final AbstractDocument fDocument;
	
	private IRModelInfo fModelInfo;
	
	private String fType;
	
	private int fOriginalLine;
	private int fLine;
	private int fStartOffset;
	private int fEndOffset;
	
	private IRLangSourceElement fMethodElement; // the deepest method element
	private IRLangSourceElement fBaseElement; // main element, null for script list
	private RAstNode fAstNode;
	private RAstNode fBaseExpressionRootNode; // the root for the R expression index
	
	
	public RLineBreakpointValidator(final IRWorkspaceSourceUnit su, final String type,
			final int offset, final IProgressMonitor monitor) {
		fSourceUnit = su;
		if (!initType(type)
				|| fSourceUnit.getResource().getType() != IResource.FILE ) {
			fDocument = null;
			setInvalid();
			return;
		}
		
		fDocument = fSourceUnit.getDocument(monitor);
		fOriginalLine = fLine = fStartOffset = fEndOffset = -1;
		check(offset, monitor);
	}
	
	public RLineBreakpointValidator(final IRWorkspaceSourceUnit su, final IRLineBreakpoint breakpoint,
			final IProgressMonitor monitor) throws CoreException {
		fSourceUnit = su;
		if (!initType(breakpoint.getBreakpointType())
				|| fSourceUnit.getResource().getType() != IResource.FILE ) {
			fDocument = null;
			setInvalid();
			return;
		}
		
		fDocument = fSourceUnit.getDocument(monitor);
		fOriginalLine = fLine = fStartOffset = fEndOffset = -1;
		
		final int offset = breakpoint.getCharStart();
		check(offset, monitor);
		
		if (fType != null && breakpoint instanceof RGenericLineBreakpoint && su.isSynchronized()) {
			((RGenericLineBreakpoint) breakpoint).setCachedData(new CachedData(
					fModelInfo.getStamp(), computeElementId(), computeRExpressionIndex() ));
		}
	}
	
	
	private boolean initType(final String type) {
		if (type == null) {
			return true;
		}
		if (type.equals(RDebugModel.R_LINE_BREAKPOINT_TYPE_ID)) {
			fType = RDebugModel.R_LINE_BREAKPOINT_TYPE_ID;
			return true;
		}
		else if (type.equals(RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID)) {
			fType = RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID;
			return true;
		}
		else {
			return false;
		}
	}
	
	private void check(final int offset, final IProgressMonitor monitor) {
		try {
			fLine = fOriginalLine = fDocument.getLineOfOffset(offset);
			fMethodElement = searchMethodElement(offset, monitor);
			if (fType == null) { // best
				if (fMethodElement != null
						&& fDocument.getLineOfOffset(fMethodElement.getSourceRange().getOffset()) == fLine ) {
					fType = RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID;
				}
				else {
					fType = RDebugModel.R_LINE_BREAKPOINT_TYPE_ID;
				}
			}
			
			if (fType == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
				IRegion lineInformation = fDocument.getLineInformation(fLine);
				final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
						fSourceUnit.getDocumentContentInfo() );
				scanner.configure(fDocument, IRDocumentConstants.R_CODE_CONTENT_CONSTRAINT);
				{	final IRegion lastLineInformation = fDocument.getLineInformation(
							Math.min(fLine + LINE_TOLERANCE, fDocument.getNumberOfLines()-1) );
					fStartOffset = scanner.findNonBlankForward(
							lineInformation.getOffset(),
							lastLineInformation.getOffset() + lastLineInformation.getLength(),
							true );
				}
				if (fStartOffset < 0) {
					setInvalid();
					return;
				}
				
				fAstNode = searchSuspendAstNode(fStartOffset, monitor);
				if (fAstNode == null) {
					setInvalid();
					return;
				}
				fStartOffset = fAstNode.getOffset();
				if (fStartOffset < 0) {
					setInvalid();
					return;
				}
				
				fLine = fDocument.getLineOfOffset(fStartOffset);
				if (fLine != fOriginalLine) {
					lineInformation = fDocument.getLineInformation(fLine);
				}
				
				fEndOffset = scanner.findNonBlankBackward(
						lineInformation.getOffset() + lineInformation.getLength(),
						fStartOffset - 1, true);
				if (fEndOffset < 0) { // should never happen
					setInvalid();
					return;
				}
				
				if (fMethodElement != null
						&& fMethodElement.getSourceRange().getOffset() != fStartOffset) {
					fBaseElement = searchBaseElement(fMethodElement);
					if (fBaseElement == null) {
						setInvalid();
						return;
					}
					
					fBaseExpressionRootNode = ((FDef) fBaseElement.getAdapter(FDef.class)).getContChild();
					if (!isBaseExpressionRootNodeValid()) {
//						new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, "Only in blocks.");
						setInvalid();
						return;
					}
				}
				else { // script line
//					fBaseExpressionRootNode = fAstNode.getRRoot();
				}
			}
			else if (fType == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
				if (fMethodElement != null) {
					fStartOffset = fMethodElement.getSourceRange().getOffset();
					if (fStartOffset < 0) {
						setInvalid();
						return;
					}
					fLine = fDocument.getLineOfOffset(fStartOffset);
					final IRegion lineInformation = fDocument.getLineInformation(fLine);
					
					final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
							fSourceUnit.getDocumentContentInfo() );
					scanner.configure(fDocument, IRDocumentConstants.R_CODE_CONTENT_CONSTRAINT);
					
					fEndOffset = scanner.findNonBlankBackward(
							Math.min(lineInformation.getOffset() + lineInformation.getLength(),
									fStartOffset + fMethodElement.getSourceRange().getLength() ),
							fStartOffset - 1, true );
					if (fEndOffset < 0) {
						setInvalid();
						return;
					}
					
					fBaseElement = searchBaseElement(fMethodElement);
					if (fBaseElement == null) {
						setInvalid();
						return;
					}
					
					if (fBaseElement != fMethodElement) {
						fAstNode = ((FDef) fMethodElement.getAdapter(FDef.class)).getContChild();
						if (fAstNode == null) {
							setInvalid();
							return;
						}
						fBaseExpressionRootNode = ((FDef) fBaseElement.getAdapter(FDef.class)).getContChild();
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
				throw new IllegalStateException(fType);
			}
		}
		catch (final BadLocationException e) {
			setInvalid();
		}
	}
	
	private boolean isBaseExpressionRootNodeValid() {
		return (fBaseExpressionRootNode != null
				&& fBaseExpressionRootNode.getNodeType() == NodeType.BLOCK
				&& RAst.isParentChild(fBaseExpressionRootNode, fAstNode) );
	}
	
	private IRModelInfo getModelInfo(final IProgressMonitor monitor) {
		if (fModelInfo == null) {
			fModelInfo = (IRModelInfo) fSourceUnit.getModelInfo(RModel.TYPE_ID,
					IModelManager.MODEL_FILE, monitor );
		}
		return fModelInfo;
	}
	
	private void setInvalid() {
		fType = null;
	}
	
	
	private IRLangSourceElement searchMethodElement(final int offset, final IProgressMonitor monitor)
			throws BadLocationException {
		final IRegion lineInformation = fDocument.getLineInformationOfOffset(offset);
		final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
				fSourceUnit.getDocumentContentInfo() );
		scanner.configure(fDocument, IRDocumentConstants.R_CODE_CONTENT_CONSTRAINT);
		int charStart = scanner.findNonBlankForward(
				lineInformation.getOffset(),
				lineInformation.getOffset() + lineInformation.getLength(),
				true);
		if (charStart < 0) {
			charStart = offset;
		}
		ISourceStructElement element = LTKUtil.getCoveringSourceElement(
				getModelInfo(monitor).getSourceElement(), charStart, charStart );
		while (element != null) {
			if (element instanceof IRLangSourceElement
					&& (element.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD) {
				return (IRLangSourceElement) element;
			}
			element = element.getSourceParent();
		}
		return null;
	}
	
	private RAstNode searchSuspendAstNode(final int offset, final IProgressMonitor monitor) {
		final IAstNode astNode = AstSelection.search(getModelInfo(monitor).getAst().root,
				offset, offset, AstSelection.MODE_COVERING_SAME_FIRST).getCovering();
		if (astNode instanceof RAstNode) {
			RAstNode rNode = (RAstNode) astNode;
			if (rNode.getOffset() < offset) {
				final AtomicReference<RAstNode> ref = new AtomicReference<RAstNode>();
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
							if (node.getStopOffset() >= offset) {
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
				while ((rParent = rNode.getRParent()) != null && rParent.getOffset() >= offset) {
					rNode = rParent;
				}
			}
			return rNode;
		}
		return null;
	}
	
	private IRLangSourceElement searchBaseElement(IRLangSourceElement element) {
		while (element != null) {
			final ISourceStructElement parent = element.getSourceParent();
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
			element = (IRLangSourceElement) parent;
		}
//		while (element != null) {
//			IRFrame frame = (IRFrame) element.getAdapter(IRFrame.class);
//			if (frame == null) {
//				return null;
//			}
//			switch (frame.getFrameType()) {
//			case IRFrame.FUNCTION:
//				element = element.getSourceParent();
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
		return fType;
	}
	
	/**
	 * Returns the line number of the original specified offset.
	 * 
	 * @return the line number (1-based)
	 */
	public int getOriginalLineNumber() {
		return (fOriginalLine + 1);
	}
	
	/**
	 * Returns the line number of the found breakpoint position.
	 * 
	 * @return the line number (1-based)
	 */
	public int getLineNumber() {
		return (fLine >= 0) ? (fLine + 1) : -1;
	}
	
	/**
	 * Returns the offset of the start of the breakpoint region.
	 * 
	 * @return start offset in the document
	 */
	public int getCharStart() {
		return fStartOffset;
	}
	
	/**
	 * Returns the offset of the end of the breakpoint region.
	 * 
	 * @return end offset in the document
	 */
	public int getCharEnd() {
		return (fEndOffset >= 0) ? (fEndOffset + 1) : -1;
	}
	
	public ISourceStructElement getMethodElement() {
		return fMethodElement;
	}
	
	public ISourceStructElement getBaseElement() {
		return fBaseElement;
	}
	
	public RAstNode getAstNode() {
		return fAstNode;
	}
	
	
	public int computeElementType() throws CoreException {
		if (fType == null) {
			throw invalid();
		}
		if (fBaseElement != null) {
			if ((fBaseElement.getElementType() & IModelElement.MASK_C1) == IModelElement.C1_METHOD) {
				if (fBaseElement.getElementType() == IRElement.R_S4METHOD) {
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
		if (fType == null) {
			throw invalid();
		}
		if (fBaseElement != null) {
			return RDbg.getElementId(fBaseElement);
		}
		else { // script line
			return null;
		}
	}
	
	public String computeElementLabel() throws CoreException {
		if (fType == null) {
			throw invalid();
		}
		if (fBaseElement != null) {
			return getLabel(fBaseElement);
		}
		else { // script line
			try {
				return fDocument.get(getCharStart(), getCharEnd()-getCharStart());
			}
			catch (final BadLocationException e) {
				return null;
			}
		}
	}
	
	public String computeSubLabel() throws CoreException {
		if (fType == null) {
			throw invalid();
		}
		if (fBaseElement != null) {
			RAstNode astNode = fAstNode;
			while (astNode != null && astNode.getNodeType() != NodeType.F_DEF) {
				astNode = astNode.getRParent();
			}
			if (astNode != null && (fMethodElement == null 
					|| (astNode != fMethodElement.getAdapter(FDef.class)
							&& astNode.getOffset() > fMethodElement.getSourceRange().getOffset() ))) {
				return "<unnamed>";
			}
			else if (fMethodElement != null && fMethodElement != fBaseElement) {
				return getLabel(fMethodElement);
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
		if (fBaseElement != null) {
			try {
				final FDef astNode = (FDef) fBaseElement.getAdapter(FDef.class);
				if (astNode != null) {
					return new RSrcref(fDocument, astNode.getContChild());
				}
				return null;
			}
			catch (final BadLocationException e) {
				throw failedComputing(e);
			}
		}
		else {
			return null;
		}
	}
	
	public int[] computeRExpressionIndex() {
		if (fBaseElement != null && fBaseExpressionRootNode != null) {
			return RAst.computeRExpressionIndex(fAstNode, fBaseExpressionRootNode);
		}
		else {
			return null;
		}
	}
	
	public IRSrcref computeRExpressionSrcref() throws CoreException {
		if (fBaseElement != null && fBaseExpressionRootNode != null) {
			try {
				return new RSrcref(fDocument, fAstNode);
			}
			catch (final BadLocationException e) {
				throw failedComputing(e);
			}
		}
		return null;
	}
	
	
	/**
	 * Creates a breakpoint with the found specifications.
	 * 
	 * @param monitor
	 */
	public IRBreakpoint createBreakpoint(final IProgressMonitor monitor) {
		if (fType == null) {
//			new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, "No valid breakpoint position.");
			return null;
		}
		else if (fType == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
			try {
				final String elementId = computeElementId();
				final RLineBreakpoint internal = new RLineBreakpoint(fSourceUnit.getResource(),
						getLineNumber(), getCharStart(), getCharEnd(),
						computeElementType(), elementId, computeElementLabel(), computeSubLabel(),
						false );
				internal.setCachedData(new CachedData(
						fModelInfo.getStamp(), elementId, computeRExpressionIndex() ));
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
		else if (fType == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
			try {
				final String elementId = computeElementId();
				final RMethodBreakpoint internal = new RMethodBreakpoint(fSourceUnit.getResource(),
						getLineNumber(), getCharStart(), getCharEnd(),
						computeElementType(), elementId, computeElementLabel(), computeSubLabel(),
						false );
				internal.setCachedData(new CachedData(
						fModelInfo.getStamp(), elementId, computeRExpressionIndex() ));
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
		throw new IllegalStateException("type= " + fType);
	}
	
	public void updateBreakpoint(final IRBreakpoint breakpoint) throws CoreException {
		if (fType != breakpoint.getBreakpointType()) {
			throw new IllegalArgumentException(fType);
		}
		if (!(breakpoint instanceof IRLineBreakpoint)) {
			throw new IllegalArgumentException(breakpoint.getClass().getName());
		}
		final IMarker marker = breakpoint.getMarker();
		final String elementId = computeElementId();
		RGenericLineBreakpoint.updatePosition(marker,
				getLineNumber(), getCharStart(), getCharEnd() );
		RGenericLineBreakpoint.updateElementInfo(marker,
				computeElementType(), elementId, computeElementLabel(), computeSubLabel() );
		if (breakpoint instanceof RGenericLineBreakpoint) {
			((RGenericLineBreakpoint) breakpoint).setCachedData(new CachedData(
					fModelInfo.getStamp(), elementId, computeRExpressionIndex() ));
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
		final StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append("\n").append("validator result:");
		sb.append("\n\t").append("type= ").append((fType != null) ? fType : "<no valid position found>");
		sb.append("\n\t").append("lineNumber= ").append(getLineNumber());
		sb.append("\n\t").append("charStart= ").append(getCharStart());
		sb.append("\n\t").append("charEnd= ").append(getCharEnd());
		return sb.toString();
	}
	
}
