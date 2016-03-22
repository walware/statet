/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IBlockTextSelection;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRIndexedVariableItem;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRVariable;


public abstract class AbstractDebugHandler extends AbstractHandler {
	
	
	protected static void showView(final IWorkbenchPart activePart, final String viewId) {
		if (activePart.getSite().getId().equals(viewId)) {
			return;
		}
		IWorkbenchPage page= activePart.getSite().getPage();
		if (page == null) {
			page= UIAccess.getActiveWorkbenchPage(true);
		}
		if (page != null) {
			final IViewPart part= page.findView(viewId);
			if (part != null) {
				page.bringToTop(part);
			}
			else {
				try {
					page.showView(viewId);
				}
				catch (final PartInitException e) {
					StatusManager.getManager().handle(e.getStatus());
				}
			}
		}
	}
	
	protected static Point preparePopup(final TextViewer viewer, final @Nullable Position position) {
		try {
			if (position != null && !position.isDeleted()) {
				final IRegion lineInfo= viewer.getDocument().getLineInformationOfOffset(position.getOffset());
				final IRegion region;
				if (position.getOffset() + position.getLength() > lineInfo.getOffset() + lineInfo.getLength()) {
					region= new Region(position.getOffset(),
							lineInfo.getOffset() + lineInfo.getLength() - position.getOffset() );
				}
				else {
					region= new Region(position.getOffset(), position.getLength());
				}
				
				final StyledText textWidget= viewer.getTextWidget();
				
				if (viewer instanceof ITextViewerExtension5) {
					((ITextViewerExtension5) viewer).exposeModelRange(region);
				}
				final IRegion widgetRange= viewer.modelRange2WidgetRange(region);
				
				final int midOffset= widgetRange.getOffset() + (widgetRange.getLength() / 2);
				Point point= textWidget.getLocationAtOffset(midOffset);
				point.y+= (textWidget.getLineHeight(midOffset) / 2);
				point= textWidget.toDisplay(point);
				
				return point;
			}
		}
		catch (final BadLocationException e) {
		}
		return null;
	}
	
	protected static @Nullable StructuredViewer getStructuredViewer(final IWorkbenchPart part) {
		final IDebugView debugView= part.getAdapter(IDebugView.class);
		if (debugView != null){
			final Viewer viewer= debugView.getViewer();
			if (viewer instanceof StructuredViewer) {
				return (@Nullable StructuredViewer) viewer;
			}
		}
		return null;
	}
	
	private static Point getPopupAnchor(final Control control) {
		if (control instanceof Tree) {
			final Tree tree= (Tree) control;
			final TreeItem[] selection= tree.getSelection();
			if (selection.length > 0) {
				final Rectangle bounds= selection[0].getBounds();
				return tree.toDisplay(new Point(bounds.x, bounds.y + bounds.height));
			}
		}
		return null;
	}
	
	protected static @Nullable Point preparePopup(final StructuredViewer viewer,
			final IStructuredSelection selection) {
		viewer.setSelection(selection, true);
		return getPopupAnchor(viewer.getControl());
	}
	
	protected static @Nullable Point preparePopup(final IWorkbenchPart part) {
		//resolve the current control
		Control widget= null;
		if (part instanceof PageBookView) {
			final IPage page= ((PageBookView) part).getCurrentPage();
			if (page != null) {
				widget= page.getControl();
			}
		}
		else {
			widget= part.getAdapter(Control.class);
		}
		return getPopupAnchor(widget);
	}
	
	protected static void disposePosition(final IDocument document, final @Nullable Position position) {
		if (position != null) {
			document.removePosition(position);
		}
	}
	
	
	private @Nullable Position expressionPosition;
	
	
	public AbstractDebugHandler() {
	}
	
	
//	protected IRElementVariable getElementVariable(final TreePath treePath) {
//		final int treeLength= treePath.getSegmentCount();
//		IRElementVariable elementVariable= null;
//		for (int i= 0; i < treeLength; i++) {
//			final Object treeSegment= treePath.getSegment(i);
//			if (treeSegment instanceof IRElementVariable) {
//				elementVariable= (IRElementVariable) treeSegment;
//			}
//			else if (treeSegment instanceof IRVariable) {
//				continue;
//			}
//			else {
//				return null;
//			}
//		}
//		return elementVariable;
//	}
	
	protected IRElementVariable getElementVariable(IRVariable variable) {
		while (variable != null) {
			if (variable instanceof IRElementVariable) {
				return (IRElementVariable) variable;
			}
			variable= variable.getParent();
		}
		return null;
	}
	
//	protected long[] getVariableItemIndex(final TreePath treePath) {
//		final Object treeSegment= treePath.getLastSegment();
//		if (treeSegment instanceof IRIndexedVariableItem) {
//			return ((IRIndexedVariableItem) treeSegment).getIndex();
//		}
//		return null;
//	}
	
	protected long[] getVariableItemIndex(final IRVariable variable) {
		if (variable instanceof IRIndexedVariableItem) {
			return ((IRIndexedVariableItem) variable).getIndex();
		}
		return null;
	}
	
	protected @Nullable String getExpressionText(final IRElementVariable elementVariable) {
		final RElementName elementName= elementVariable.getFQElementName();
		if (elementName != null) {
			return elementName.getDisplayName(RElementName.DISPLAY_EXACT | RElementName.DISPLAY_FQN);
		}
		return null;
	}
	
	protected @Nullable String getExpressionText(final ITextSelection textSelection,
			final ISourceEditor sourceEditor) {
		if (textSelection.getLength() == 0) {
			final RHeuristicTokenScanner scanner= RHeuristicTokenScanner.create(
					sourceEditor.getDocumentContentInfo() );
			final IDocument document= sourceEditor.getViewer().getDocument();
			scanner.configure(document);
			final IRegion region= scanner.findRWord(textSelection.getOffset(), false, true);
			if (region != null) {
				try {
					final String expression= document.get(region.getOffset(), region.getLength());
					this.expressionPosition= new Position(region.getOffset(), region.getLength());
					return expression;
				}
				catch (final BadLocationException e) {}
			}
		}
		else {
			final String selectedText= textSelection.getText();
			final String expression= selectedText.trim();
			if (!expression.isEmpty()) {
				if (expression != selectedText && !(textSelection instanceof IBlockTextSelection)) {
					final int offset= textSelection.getOffset() + selectedText.indexOf(expression);
					this.expressionPosition= new Position(offset, expression.length());
				}
				else {
					this.expressionPosition= new Position(textSelection.getOffset(), textSelection.getLength());
				}
				return expression;
			}
		}
		return null;
	}
	
	protected @Nullable Position markExpressionPosition(final IDocument document) {
		if (this.expressionPosition != null) {
			try {
				document.addPosition(this.expressionPosition);
				return this.expressionPosition;
			}
			catch (final BadLocationException e) {}
		}
		return null;
	}
	
	protected IAdaptable getViewInput(final IWorkbenchPart part) {
		if (part instanceof IDebugView) {
			final Object input= ((IDebugView) part).getViewer().getInput();
			if (input instanceof IAdaptable) {
				return (IAdaptable) input;
			}
		}
		return null;
	}
	
	private @Nullable IAdaptable getWorkbenchDebugContext(final IWorkbenchPart workbenchPart) {
		if (workbenchPart != null) {
			final ISelection contexts= DebugUITools.getDebugContextForPart(workbenchPart);
			if (contexts instanceof IStructuredSelection) {
				final Object firstElement= ((IStructuredSelection) contexts).getFirstElement();
				if (firstElement instanceof IAdaptable) {
					return (IAdaptable) firstElement;
				}
			}
		}
		return DebugUITools.getDebugContext();
	}
	
	protected @Nullable IDebugElement getContextElement(IAdaptable context,
			final IWorkbenchPart workbenchPart) {
		if (context == null) {
			context= getWorkbenchDebugContext(workbenchPart);
			if (context == null) {
				return null;
			}
		}
		if (context instanceof IDebugElement) {
			return (IDebugElement) context;
		}
		if (context instanceof ILaunch) {
			return ((ILaunch) context).getDebugTarget();
		}
		return context.getAdapter(IDebugElement.class);
	}
	
	protected @Nullable IRStackFrame getContextStackFrame(final IWorkbenchPart workbenchPart) {
		final @Nullable IDebugElement contextElement= getContextElement(null, workbenchPart);
		if (contextElement != null) {
			return contextElement.getAdapter(IRStackFrame.class);
		}
		return null;
	}
	
	
	protected String addIndex(final String name, final long[] index) {
		if (name == null) {
			return null;
		}
		if (index == null) {
			return name;
		}
		final StringBuilder sb= new StringBuilder(name);
		sb.append('[');
		sb.append(index[0] + 1);
		for (int i= 1; i < index.length; i++) {
			sb.append(", "); //$NON-NLS-1$
			sb.append(index[i] + 1);
		}
		sb.append(']');
		return sb.toString();
	}
	
}
