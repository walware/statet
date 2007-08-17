/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - adapted to StatET
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;


/**
 * Groups the folding actions.
 */
public class FoldingActionGroup extends ActionGroup {
	
//	private class FoldingAction extends ResourceAction implements IUpdate {
//
//		FoldingAction(ResourceBundle bundle, String prefix) {
//			super(bundle, prefix, IAction.AS_PUSH_BUTTON);
//		}
//
//		public void update() {
//			setEnabled(FoldingActionGroup.this.isEnabled() && fViewer.isProjectionMode());
//		}
//
//	}
	
	private ProjectionViewer fViewer;
	
	private final IProjectionListener fProjectionListener;
	
	private TextOperationAction fToggle;
	private final TextOperationAction fExpand;
	private final TextOperationAction fCollapse;
	private final TextOperationAction fExpandAll;
	private final TextOperationAction fCollapseAll;
//	private final FoldingAction fCollapseMembers;
//	private final FoldingAction fCollapseComments;



	/**
	 * Creates a new projection action group for <code>editor</code>. If the
	 * supplied viewer is not an instance of <code>ProjectionViewer</code>, the
	 * action group is disabled.
	 * 
	 * @param editor the text editor to operate on
	 * @param viewer the viewer of the editor
	 */
	public FoldingActionGroup(final ITextEditor editor, ProjectionViewer viewer) {
		
		fViewer = viewer;
		
		fProjectionListener = new IProjectionListener() {
			public void projectionEnabled() {
				update();
				fToggle.setChecked(fViewer.isProjectionMode());
			}

			public void projectionDisabled() {
				update();
				fToggle.setChecked(fViewer.isProjectionMode());
			}
		};
		fViewer.addProjectionListener(fProjectionListener);
		
		fToggle = new TextOperationAction(EditorMessages.getCompatibilityBundle(), "Projection.Toggle.", editor, ProjectionViewer.TOGGLE);
		fToggle.setChecked(fViewer.isProjectionMode());
		fToggle.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
		editor.setAction("FoldingToggle", fToggle); //$NON-NLS-1$
		
		fExpandAll = new TextOperationAction(EditorMessages.getCompatibilityBundle(), "Projection.ExpandAll.", editor, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
		fExpandAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
		editor.setAction("FoldingExpandAll", fExpandAll); //$NON-NLS-1$
		
		fCollapseAll = new TextOperationAction(EditorMessages.getCompatibilityBundle(), "Projection.CollapseAll.", editor, ProjectionViewer.COLLAPSE_ALL, true); //$NON-NLS-1$
		fCollapseAll.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
		editor.setAction("FoldingCollapseAll", fCollapseAll); //$NON-NLS-1$
		
		fExpand = new TextOperationAction(EditorMessages.getCompatibilityBundle(), "Projection.Expand.", editor, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
		fExpand.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
		editor.setAction("FoldingExpand", fExpand); //$NON-NLS-1$
		
		fCollapse = new TextOperationAction(EditorMessages.getCompatibilityBundle(), "Projection.Collapse.", editor, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
		fCollapse.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
		editor.setAction("FoldingCollapse", fCollapse); //$NON-NLS-1$
		
	}
	
	/**
	 * Returns <code>true</code> if the group is enabled.
	 * <pre>
	 * Invariant: isEnabled() <=> fViewer and all actions are != null.
	 * </pre>
	 * 
	 * @return <code>true</code> if the group is enabled
	 */
	protected boolean isEnabled() {
		return fViewer != null;
	}
	
	@Override
	public void dispose() {
		if (isEnabled()) {
			fViewer.removeProjectionListener(fProjectionListener);
			fViewer= null;
		}
		super.dispose();
	}
	
	/**
	 * Updates the actions.
	 */
	protected void update() {
		if (isEnabled()) {
			fExpand.update();
			fExpandAll.update();
			fCollapse.update();
			fCollapseAll.update();
		}
	}
	
	/**
	 * Fills the menu with all folding actions.
	 * 
	 * @param manager the menu manager for the folding submenu
	 */
	public void fillMenu(IMenuManager manager) {
		if (isEnabled()) {
			update();
			manager.add(fToggle);
			manager.add(new Separator());
			manager.add(fExpandAll);
			manager.add(fCollapseAll);
		}
	}
	
	@Override
	public void updateActionBars() {
		update();
	}
	
}
