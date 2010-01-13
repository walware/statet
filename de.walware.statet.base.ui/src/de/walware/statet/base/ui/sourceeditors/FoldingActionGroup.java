/*******************************************************************************
 * Copyright (c) 2000-2010 WalWare/StatET-Project (www.walware.de/goto/statet)
 * and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - adapted to StatET
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.ecommons.ui.HandlerContributionItem;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetMessages;


/**
 * Groups the folding actions.
 */
public class FoldingActionGroup extends ActionGroup implements IProjectionListener {
	
	private class ViewerOperationHandler extends AbstractHandler {
		
		private int fOperationCode;
		
		public ViewerOperationHandler(final int operationCode) {
			super();
			fOperationCode = operationCode;
		}
		
		public void update() {
			setBaseEnabled(UIAccess.isOkToUse(fViewer) && fViewer.isProjectionMode());
		}
		
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			if (UIAccess.isOkToUse(fViewer) && fViewer.canDoOperation(fOperationCode)) {
				fViewer.doOperation(fOperationCode);
			}
			return null;
		}
		
	}
	
	private ITextEditor fEditor;
	private ProjectionViewer fViewer;
	
	private final ViewerOperationHandler fExpand;
	private final ViewerOperationHandler fCollapse;
	private final ViewerOperationHandler fExpandAll;
	private final ViewerOperationHandler fCollapseAll;
	
	
	/**
	 * Creates a new projection action group for <code>editor</code>.
	 * 
	 * @param editor the text editor to operate on
	 * @param viewer the viewer of the editor
	 */
	public FoldingActionGroup(final ITextEditor editor, final ProjectionViewer viewer) {
		fEditor = editor;
		fViewer = viewer;
		final IHandlerService handlerService = (IHandlerService) fEditor.getSite().getService(IHandlerService.class);
		
		fExpandAll = new ViewerOperationHandler(ProjectionViewer.EXPAND_ALL);
		handlerService.activateHandler(IFoldingCommandIds.FOLDING_EXPAND_ALL, fExpandAll);
		
		fCollapseAll = new ViewerOperationHandler(ProjectionViewer.COLLAPSE_ALL); 
		handlerService.activateHandler(IFoldingCommandIds.FOLDING_COLLAPSE_ALL, fCollapseAll);
		
		fExpand = new ViewerOperationHandler(ProjectionViewer.EXPAND); 
		handlerService.activateHandler(IFoldingCommandIds.FOLDING_EXPAND, fExpand);
		
		fCollapse = new ViewerOperationHandler(ProjectionViewer.COLLAPSE); 
		handlerService.activateHandler(IFoldingCommandIds.FOLDING_COLLAPSE, fCollapse);
		
		fViewer.addProjectionListener(this);
		
		update();
	}
	
	/**
	 * Note: this is not intend to use to remove the actions from the editor
	 */
	@Override
	public void dispose() {
		fEditor = null;
		fViewer = null;
		super.dispose();
	}
	
	/**
	 * Updates the actions.
	 */
	protected void update() {
		if (fViewer != null) {
			fExpand.update();
			fExpandAll.update();
			fCollapse.update();
			fCollapseAll.update();
		}
	}
	
	/**
	 * Fills the menu with all folding actions.
	 * 
	 * @param menuManager the menu manager for the folding submenu
	 */
	public void fillMenu(final IMenuManager menuManager) {
		if (fViewer != null) {
			update();
			menuManager.add(new CommandContributionItem(new CommandContributionItemParameter(
					fEditor.getSite(), null, IFoldingCommandIds.FOLDING_TOGGLE, null, 
					null, null, null,
					StatetMessages.CodeFolding_Enable_label, StatetMessages.CodeFolding_Enable_mnemonic, null, SWT.CHECK, null, false) ));
			menuManager.add(new Separator());
			menuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(
					fEditor.getSite(), null, IFoldingCommandIds.FOLDING_EXPAND_ALL, null, 
					null, null, null,
					StatetMessages.CodeFolding_ExpandAll_label, StatetMessages.CodeFolding_ExpandAll_mnemonic, null, SWT.CHECK, null, false),
					fExpandAll ));
			menuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(
					fEditor.getSite(), null, IFoldingCommandIds.FOLDING_COLLAPSE_ALL, null, 
					null, null, null,
					StatetMessages.CodeFolding_CollapseAll_label, StatetMessages.CodeFolding_CollapseAll_mnemonic, null, SWT.CHECK, null, false),
					fCollapseAll ));
		}
	}
	
	@Override
	public void updateActionBars() {
		update();
	}
	
	public void projectionEnabled() {
		update();
	}
	
	public void projectionDisabled() {
		update();
	}
	
}
