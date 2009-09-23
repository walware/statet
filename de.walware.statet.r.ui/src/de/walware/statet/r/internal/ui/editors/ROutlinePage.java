/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElement.Filter;
import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.ltk.ui.ElementNameComparator;
import de.walware.ecommons.ltk.ui.refactoring.AbstractElementsHandler;
import de.walware.ecommons.ltk.ui.refactoring.CopyElementsHandler;
import de.walware.ecommons.ltk.ui.refactoring.CopyNamesHandler;
import de.walware.ecommons.ltk.ui.refactoring.CutElementsHandler;
import de.walware.ecommons.ltk.ui.refactoring.DeleteElementsHandler;
import de.walware.ecommons.ltk.ui.refactoring.PasteElementsHandler;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.base.ui.IStatetUIMenuIds;
import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.sourceeditors.StatextOutlinePage1;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RRefactoring;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.internal.ui.RUIMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.REditor;


/**
 * Outline page for R sources
 */
public class ROutlinePage extends StatextOutlinePage1 {
	
	private static final ViewerComparator ALPHA_COMPARATOR = new ElementNameComparator(RElementName.NAMEONLY_COMPARATOR);
	
	private class AlphaSortAction extends ToggleAction {
		
		
		public AlphaSortAction() {
			super("sort.alphabetically.enabled", false, 2); //$NON-NLS-1$
			setText(SharedMessages.ToggleSortAction_name);
			setImageDescriptor(StatetImages.getDescriptor(StatetImages.LOCTOOL_SORT_ALPHA));
			setToolTipText(SharedMessages.ToggleSortAction_tooltip);
		}
		
		@Override
		protected void configure(final boolean on) {
			final TreeViewer viewer = getViewer();
			if (UIAccess.isOkToUse(viewer)) {
				viewer.setComparator(on ? ALPHA_COMPARATOR : null);
			}
		}
		
	}
	
	private class FilterCommonVariables extends ToggleAction {
		
		public FilterCommonVariables() {
			super("filter.common_var.enabled", false, 2); //$NON-NLS-1$
			setText(RUIMessages.Outline_HideGeneralVariables_name);
			setImageDescriptor(RUI.getImageDescriptor(RUIPlugin.IMG_LOCTOOL_FILTER_GENERAL));
			setToolTipText(RUIMessages.Outline_HideGeneralVariables_name);
		}
		
		@Override
		protected void configure(final boolean on) {
			final TreeViewer viewer = getViewer();
			fFilter.hideCommonVariables = on;
			if (UIAccess.isOkToUse(viewer)) {
				viewer.refresh(false);
			}
		}
		
	}
	
	private class FilterLocalDefinitions extends ToggleAction {
		
		public FilterLocalDefinitions() {
			super("filter.local.enabled", false, 2); //$NON-NLS-1$
			setText(RUIMessages.Outline_HideLocalElements_name);
			setImageDescriptor(RUI.getImageDescriptor(RUIPlugin.IMG_LOCTOOL_FILTER_LOCAL));
			setToolTipText(RUIMessages.Outline_HideLocalElements_name);
		}
		
		@Override
		protected void configure(final boolean on) {
			final TreeViewer viewer = getViewer();
			fFilter.hideLocalDefinitions = on;
			if (UIAccess.isOkToUse(viewer)) {
				viewer.refresh(false);
			}
		}
		
	}
	
	private class ContentFilter implements Filter<IModelElement> {
		
		private boolean hideCommonVariables;
		private boolean hideLocalDefinitions;
		
		public boolean include(final IModelElement element) {
			switch (element.getElementType()) {
			case IRElement.R_ARGUMENT:
				return false;
			case IRElement.R_GENERAL_VARIABLE:
				return !hideCommonVariables;
			case IRElement.R_GENERAL_LOCAL_VARIABLE:
				return !hideCommonVariables && !hideLocalDefinitions;
			case IRElement.R_COMMON_LOCAL_FUNCTION:
				return !hideLocalDefinitions;
			default:
				return true;
			}
		};
		
	}
	
	
	private final REditor fEditor;
	private final ContentFilter fFilter = new ContentFilter();
	
	private RefactoringAdapter fLTK;
	
	public ROutlinePage(final REditor editor) {
		super(editor, RModel.TYPE_ID, "de.walware.r.menu.ROutlineViewContextMenu"); //$NON-NLS-1$
		fEditor = editor;
	}
	
	
	@Override
	protected IDialogSettings getSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "ROutlineView"); //$NON-NLS-1$
	}
	
	@Override
	protected IModelElement.Filter getContentFilter() {
		return fFilter;
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		viewer.setLabelProvider(new RLabelProvider());
	}
	
	@Override
	protected void initActions() {
		fLTK = new RRefactoringAdapter();
		super.initActions();
		final IPageSite site = getSite();
		
		final IHandlerService handlerSvc = (IHandlerService) site.getService(IHandlerService.class);
		final AbstractElementsHandler cutHandler = new CutElementsHandler(fLTK, RRefactoring.getFactory());
		registerHandlerToUpdate(cutHandler);
		handlerSvc.activateHandler(IWorkbenchActionDefinitionIds.CUT, cutHandler);
		final AbstractElementsHandler copyHandler = new CopyElementsHandler(fLTK);
		registerHandlerToUpdate(copyHandler);
		handlerSvc.activateHandler(IWorkbenchActionDefinitionIds.COPY, copyHandler);
		final AbstractElementsHandler copyNamesHandler = new CopyNamesHandler(fLTK);
		registerHandlerToUpdate(copyNamesHandler);
		handlerSvc.activateHandler(IStatetUICommandIds.COPY_ELEMENT_NAME, copyNamesHandler);
		final AbstractElementsHandler pasteHandler = new PasteElementsHandler(fEditor, fLTK);
		handlerSvc.activateHandler(IWorkbenchActionDefinitionIds.PASTE, pasteHandler);
		final AbstractElementsHandler deleteHandler = new DeleteElementsHandler(fLTK, RRefactoring.getFactory());
		handlerSvc.activateHandler(IWorkbenchActionDefinitionIds.DELETE, deleteHandler);
		
		final IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
		final IMenuManager menuManager = site.getActionBars().getMenuManager();
		
		toolBarManager.appendToGroup(IStatetUIMenuIds.GROUP_VIEW_SORT_ID,
				new AlphaSortAction());
		toolBarManager.appendToGroup(IStatetUIMenuIds.GROUP_VIEW_FILTER_ID,
				new FilterCommonVariables());
		toolBarManager.appendToGroup(IStatetUIMenuIds.GROUP_VIEW_FILTER_ID,
				new FilterLocalDefinitions());
	}
	
	@Override
	protected void contextMenuAboutToShow(final IMenuManager m) {
		super.contextMenuAboutToShow(m);
		final IPageSite site = getSite();
		
		m.add(new SelectCodeRangeAction(fLTK));
		
		m.add(new Separator(IStatetUIMenuIds.GROUP_EDIT_COPYPASTE_ID));
		m.add(new CommandContributionItem(new CommandContributionItemParameter(
				site, null, IWorkbenchActionDefinitionIds.CUT, CommandContributionItem.STYLE_PUSH)));
		m.add(new CommandContributionItem(new CommandContributionItemParameter(
				site, null, IWorkbenchActionDefinitionIds.COPY, CommandContributionItem.STYLE_PUSH)));
		m.add(new CommandContributionItem(new CommandContributionItemParameter(
				site, null, IStatetUICommandIds.COPY_ELEMENT_NAME, CommandContributionItem.STYLE_PUSH)));
		m.add(new CommandContributionItem(new CommandContributionItemParameter(
				site, null, IWorkbenchActionDefinitionIds.PASTE, CommandContributionItem.STYLE_PUSH)));
//		m.add(new CommandContributionItem(new CommandContributionItemParameter(
//				site, null, IWorkbenchActionDefinitionIds.DELETE, CommandContributionItem.STYLE_PUSH)));
		
		m.add(new Separator(IStatetUIMenuIds.GROUP_RUN_STAT_ID));
		m.add(new CommandContributionItem(new CommandContributionItemParameter(
				site, null, RCodeLaunching.RUN_SELECTION_COMMAND_ID, null,
				null, null, null,
				null, "R", null, //$NON-NLS-1$
				CommandContributionItem.STYLE_PUSH, null, false)));
		final Separator additions = new Separator(IStatetUIMenuIds.GROUP_ADDITIONS_ID);
		additions.setVisible(false);
		m.add(additions);
		m.add(new Separator(IStatetUIMenuIds.GROUP_ADD_MORE_ID));
	}
	
}
