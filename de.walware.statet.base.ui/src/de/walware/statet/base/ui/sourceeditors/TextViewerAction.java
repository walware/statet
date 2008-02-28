/*******************************************************************************
 * Copyright (c) 2000-2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.eclipsecommons.ui.SharedMessages;


/**
 * Common function for actions that operate on a text viewer.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.0
 */
public class TextViewerAction extends Action implements IUpdate {
	
	
	private int fOperationCode;
	private ITextOperationTarget fOperationTarget;
	
	
	/**
	 * Constructs a new action in the given text viewer with
	 * the specified operation code.
	 * 
	 * @param viewer
	 * @param operationCode
	 */
	public TextViewerAction(final ITextViewer viewer, final int operationCode) {
		assert (viewer != null);
		assert (operationCode != -1);
		
		fOperationCode = operationCode;
		fOperationTarget = viewer.getTextOperationTarget();
		update();
	}
	
	public void update() {
		setEnabled(fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
	}
	
	@Override
	public void run() {
		if (isEnabled()) {
			fOperationTarget.doOperation(fOperationCode);
		}
	}
	
	
	public static TextViewerAction createDeleteAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.DELETE);
		action.setId(ActionFactory.DELETE.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.DELETE);
		action.setText(SharedMessages.DeleteAction_name);
		action.setToolTipText(SharedMessages.DeleteAction_tooltip);
		return action;
	}
	
	public static TextViewerAction createCutAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.CUT);
		action.setId(ActionFactory.CUT.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.CUT);
		action.setText(SharedMessages.CutAction_name);
		action.setToolTipText(SharedMessages.CutAction_tooltip);
		return action;
	}
	
	public static TextViewerAction createCopyAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.COPY);
		action.setId(ActionFactory.COPY.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
		action.setText(SharedMessages.CopyAction_name);
		action.setToolTipText(SharedMessages.CopyAction_tooltip);
		return action;
	}
	
	public static TextViewerAction createPasteAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.PASTE);
		action.setId(ActionFactory.PASTE.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.PASTE);
		action.setText(SharedMessages.PasteAction_name);
		action.setToolTipText(SharedMessages.PasteAction_tooltip);
		return action;
	}
	
	public static TextViewerAction createSelectAllAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.SELECT_ALL);
		action.setId(ActionFactory.SELECT_ALL.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.SELECT_ALL);
		action.setText(SharedMessages.SelectAllAction_name);
		action.setToolTipText(SharedMessages.SelectAllAction_tooltip);
		return action;
	}
	
	public static TextViewerAction createUndoAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.UNDO);
		action.setId(ActionFactory.UNDO.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.UNDO);
		action.setText(SharedMessages.UndoAction_name);
		action.setToolTipText(SharedMessages.UndoAction_tooltip);
		return action;
	}
	
	public static TextViewerAction createRedoAction(final ITextViewer viewer) {
		
		final TextViewerAction action = new TextViewerAction(viewer, ITextOperationTarget.REDO);
		action.setId(ActionFactory.REDO.getId());
		action.setActionDefinitionId(IWorkbenchActionDefinitionIds.REDO);
		action.setText(SharedMessages.RedoAction_name);
		action.setToolTipText(SharedMessages.RedoAction_tooltip);
		return action;
	}
	
}
