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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.debug.core.eval.IEvaluationListener;
import de.walware.ecommons.debug.core.eval.IEvaluationResult;
import de.walware.ecommons.debug.ui.ECommonsDebugUI;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IREvaluationResult;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.debug.core.IRVariable;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.internal.debug.ui.Messages;
import de.walware.statet.r.internal.debug.ui.RDebugUIPlugin;
import de.walware.statet.r.ui.editors.IRSourceEditor;


@NonNullByDefault
public class InspectHandler extends AbstractDebugHandler {
	
	
	private static class RInspectPopupDialog extends InspectPopupDialog {
		
		
		private final Viewer viewer;
		private final ISelection savedSelection;
		
		
		public RInspectPopupDialog(final Shell shell, final Point anchor, final String commandId,
				final IExpression expression,
				final Viewer viewer, final ISelection savedSelection) {
			super(shell, anchor, commandId, expression);
			
			this.viewer= viewer;
			this.savedSelection= savedSelection;
		}
		
		
		@Override
		protected Control createDialogArea(final Composite parent) {
			final Control control= super.createDialogArea(parent);
//			ViewerColumn viewerColumn= (ViewerColumn) control.getData("org.eclipse.jface.columnViewer");
//			TreeModelViewer viewer= (TreeModelViewer) viewerColumn.getViewer();
			return control;
		}
		
		@Override
		public boolean close() {
			final boolean closed= super.close();
			if (UIAccess.isOkToUse(this.viewer) && this.savedSelection != null
					&& !this.savedSelection.equals(this.viewer.getSelection()) ) {
				this.viewer.setSelection(this.savedSelection);
			}
			return closed;
		}
		
	}
	
	
	private class ResultHandler implements IEvaluationListener, Runnable {
		
		
		protected final IWorkbenchPart workbenchPart;
		
		protected final Display display;
		
		protected @Nullable IErrorReportingExpression expression;
		
		
		public ResultHandler(final IWorkbenchPart part) {
			this.workbenchPart= part;
			this.display= part.getSite().getShell().getDisplay();
		}
		
		protected void dispose() {
			if (this.expression != null) {
				this.expression.dispose();
				this.expression= null;
			}
		}
		
		@Override
		public void evaluationFinished(@NonNull final IEvaluationResult result) {
			if ((result.getValue() != null || result.getMessages() != null)
					&& RDebugUIPlugin.getDefault() != null && !this.display.isDisposed() ) {
				this.expression= RDebugModel.createExpression((@NonNull IREvaluationResult) result);
				this.display.asyncExec(this);
			}
			else {
				result.free();
				dispose();
			}
		}
		
		protected @Nullable Shell getShell() {
			return this.workbenchPart.getSite().getShell();
		}
		
		@Override
		public void run() {
			DebugPlugin.getDefault().getExpressionManager().addExpression(this.expression);
			showView(this.workbenchPart, IDebugUIConstants.ID_EXPRESSION_VIEW);
		}
		
	}
	
	private class VariablePopupResultHandler extends ResultHandler {
		
		
		private final IStructuredSelection selection;
		
		
		public VariablePopupResultHandler(final IWorkbenchPart part, final IStructuredSelection selection) {
			super(part);
			
			this.selection= selection;
		}
		
		
		@Override
		public void run() {
			try {
				final Shell shell= getShell();
				if (UIAccess.isOkToUse(shell)
						&& this.workbenchPart.getSite().getPage().isPartVisible(this.workbenchPart)) {
					final StructuredViewer viewer= getStructuredViewer(this.workbenchPart);
					final ISelection savedSelection= (viewer != null) ? viewer.getSelection() : null;
					final Point anchor= (viewer != null) ?
							preparePopup(viewer, this.selection) :
							preparePopup(this.workbenchPart);
					
					final DebugPopup popup= new RInspectPopupDialog(shell, anchor,
							getCommandId(), this.expression,
							viewer, savedSelection );
					popup.open();
					this.expression= null;
				}
			}
			finally {
				dispose();
			}
		}
		
	}
	
	private class SourceEditorPopupResultHandler extends ResultHandler {
		
		
		private final ISourceEditor editor;
		
		private final IDocument document;
		private final @Nullable Position position;
		
		
		public SourceEditorPopupResultHandler(final ISourceEditor editor) {
			super(editor.getWorkbenchPart());
			this.editor= editor;
			
			this.document= editor.getViewer().getDocument();
			this.position= markExpressionPosition(this.document);
		}
		
		
		@Override
		protected void dispose() {
			super.dispose();
			disposePosition(this.document, this.position);
		}
		
		@Override
		public void run() {
			try {
				final Shell shell= getShell();
				final SourceViewer viewer= this.editor.getViewer();
				if (UIAccess.isOkToUse(shell)
						&& this.workbenchPart.getSite().getPage().isPartVisible(this.workbenchPart)
						&& UIAccess.isOkToUse(viewer) && viewer.getDocument() == this.document) {
					final ISelection savedSelection= viewer.getSelection();
					final Point anchor= preparePopup(viewer, this.position);
					final DebugPopup popup= new RInspectPopupDialog(getShell(), anchor,
							getCommandId(), this.expression,
							viewer, savedSelection );
					popup.open();
					this.expression= null;
				}
			}
			finally {
				dispose();
			}
		}
		
	}
	
	
	public InspectHandler() {
	}
	
	
	private boolean isExpressionElementVariable(final TreePath treePath) {
		final Object firstSegment= treePath.getFirstSegment();
		if (!(firstSegment instanceof IExpression)
				|| ((IExpression) firstSegment).getExpressionText().isEmpty() ) {
			return false;
		}
		for (int i= 1; i < treePath.getSegmentCount(); i++) {
			final Object segment= treePath.getSegment(i);
			if (!(segment instanceof IRVariable)) {
				return false;
			}
		}
		return true;
	}
	
	private @Nullable String getExpressionElementVariableExpression(final TreePath treePath) {
		final Object firstSegment= treePath.getFirstSegment();
		final String expressionText;
		if (!(firstSegment instanceof IExpression)
				|| (expressionText= ((IExpression) firstSegment).getExpressionText()).isEmpty() ) {
			return null;
		}
		final List<RElementName> segments= new ArrayList<>();
		segments.add(RElementName.create(RElementName.MAIN_DEFAULT, "expr")); //$NON-NLS-1$
		for (int i= 1; i < treePath.getSegmentCount(); i++) {
			final Object segment= treePath.getSegment(i);
			if (!(segment instanceof IRVariable)) {
				return null;
			}
			if (segment instanceof IRElementVariable) {
				final ICombinedRElement element= ((IRElementVariable) segment).getElement();
				segments.add(element.getElementName());
			}
		}
		if (segments.size() == 1) {
			return null;
		}
		final String subName= RElementName.create(segments).getDisplayName(RElementName.DISPLAY_EXACT);
		if (subName == null) {
			return null;
		}
		return expressionText + subName.substring(4);
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(evaluationContext);
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(evaluationContext);
		if (part != null && selection != null
				&& getContextElement(null, part) != null) {
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				if (structSelection.size() != 1) {
					setBaseEnabled(false);
					return;
				}
				final Object obj= structSelection.getFirstElement();
				if (obj instanceof IExpression) {
					setBaseEnabled(!((IExpression) obj).getExpressionText().isEmpty());
					return;
				}
				if (obj instanceof IRElementVariable) {
					if (((IRElementVariable) obj).getFQElementName() != null) {
						setBaseEnabled(true);
					}
					else if (selection instanceof ITreeSelection
							&& isExpressionElementVariable(((ITreeSelection) selection).getPaths()[0])) {
						setBaseEnabled(true);
					}
					else {
						setBaseEnabled(false);
					}
					return;
				}
				else {
					setBaseEnabled(false);
					return;
				}
			}
			else if (selection instanceof ITextSelection && part instanceof IRSourceEditor) {
//				final ISourceEditor sourceEditor= (ISourceEditor) part;
//				final ITextSelection textSelection= (ITextSelection) selection;
				setBaseEnabled(true);
				return;
			}
		}
		setBaseEnabled(false);
	}
	
	
	@Override
	public @Nullable Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part= WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		final ISelection selection= WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		if (part != null && selection != null) {
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structSelection= (IStructuredSelection) selection;
				final Object obj= structSelection.getFirstElement();
				String expression= null;
				if (obj instanceof IExpression) {
					expression= ((IExpression) obj).getExpressionText();
				}
				if (obj instanceof IRElementVariable) {
					expression= getExpressionText((@NonNull IRElementVariable) obj);
					if (expression == null && selection instanceof ITreeSelection) {
						expression= getExpressionElementVariableExpression(((ITreeSelection) selection).getPaths()[0]);
					}
				}
				if (expression != null) {
					final IRStackFrame stackFrame= getContextStackFrame(part);
					if (stackFrame == null) {
						LTKWorkbenchUIUtil.indicateStatus(
								new StatusInfo(IStatus.ERROR, Messages.Expression_Context_Missing_message),
								event );
						return null;
					}
					final String commandExpression= toCommandExpression(expression);
					final ResultHandler resultHandler= (commandExpression != expression) ?
							new VariablePopupResultHandler(part, structSelection) :
							new ResultHandler(part);
					stackFrame.getThread().evaluate(commandExpression, stackFrame, false,
							resultHandler );
				}
			}
			else if (selection instanceof ITextSelection && part instanceof IRSourceEditor) {
				final ISourceEditor sourceEditor= (ISourceEditor) part;
				final ITextSelection textSelection= (ITextSelection) selection;
				final String expression= getExpressionText(textSelection, sourceEditor);
				if (expression != null
						&& sourceEditor.getWorkbenchPart() != null) {
					final IRStackFrame stackFrame= getContextStackFrame(part);
					if (stackFrame == null) {
						LTKWorkbenchUIUtil.indicateStatus(
								new StatusInfo(IStatus.ERROR, Messages.Expression_Context_Missing_message),
								event );
						return null;
					}
					final String commandExpression= toCommandExpression(expression);
					final ResultHandler resultHandler= new SourceEditorPopupResultHandler(
							sourceEditor );
					stackFrame.getThread().evaluate(commandExpression, stackFrame, false,
							resultHandler );
				}
			}
		}
		
		return null;
	}
	
	
	protected String getCommandId() {
		return ECommonsDebugUI.INSPECT_COMMAND_ID;
	}
	
	protected String toCommandExpression(final String expression) {
		return expression;
	}
	
}
