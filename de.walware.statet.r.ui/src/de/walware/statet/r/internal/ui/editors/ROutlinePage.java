/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ui.ElementNameComparator;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor2OutlinePage;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.IStatetUIMenuIds;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.refactoring.RRefactoring;
import de.walware.statet.r.internal.ui.RUIMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;


/**
 * Outline page for R sources
 */
public class ROutlinePage extends SourceEditor2OutlinePage {
	
	private static final ViewerComparator ALPHA_COMPARATOR = new ElementNameComparator(RElementName.NAMEONLY_COMPARATOR);
	
	private class AlphaSortAction extends ToggleAction {
		
		
		public AlphaSortAction() {
			super("sort.alphabetically.enabled", false, 2); //$NON-NLS-1$
			setText(SharedMessages.ToggleSortAction_name);
			setImageDescriptor(SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SORT_ALPHA_IMAGE_ID));
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
	
	private class ContentFilter implements IModelElement.Filter {
		
		private boolean hideCommonVariables;
		private boolean hideLocalDefinitions;
		
		@Override
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
	
	
	private final ContentFilter fFilter = new ContentFilter();
	
	public ROutlinePage(final REditor editor) {
		super(editor, RModel.TYPE_ID, RRefactoring.getFactory(),
				"de.walware.r.menu.ROutlineViewContextMenu"); //$NON-NLS-1$
	}
	
	
	@Override
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "ROutlineView"); //$NON-NLS-1$
	}
	
	@Override
	protected IModelElement.Filter getContentFilter() {
		return fFilter;
	}
	
	@Override
	protected void configureViewer(final TreeViewer viewer) {
		super.configureViewer(viewer);
		
		viewer.setLabelProvider(new RLabelProvider());
	}
	
	@Override
	protected void contributeToActionBars(final IServiceLocator serviceLocator,
			final IActionBars actionBars, final HandlerCollection handlers) {
		super.contributeToActionBars(serviceLocator, actionBars, handlers);
		
		final IToolBarManager toolBarManager = actionBars.getToolBarManager();
		
		toolBarManager.appendToGroup(SharedUIResources.VIEW_SORT_MENU_ID,
				new AlphaSortAction());
		toolBarManager.appendToGroup(SharedUIResources.VIEW_FILTER_MENU_ID,
				new FilterCommonVariables());
		toolBarManager.appendToGroup(SharedUIResources.VIEW_FILTER_MENU_ID,
				new FilterLocalDefinitions());
	}
	
	@Override
	protected void contextMenuAboutToShow(final IMenuManager m) {
		super.contextMenuAboutToShow(m);
		final IPageSite site = getSite();
		
		if (m.find(IStatetUIMenuIds.GROUP_RUN_STAT_ID) == null) {
			m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID,
					new Separator(IStatetUIMenuIds.GROUP_RUN_STAT_ID) );
		}
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_RUN_STAT_ID, 
				new CommandContributionItem(new CommandContributionItemParameter(
						site, null, RCodeLaunching.RUN_SELECTION_COMMAND_ID, null,
						null, null, null,
						null, "R", null, //$NON-NLS-1$
						CommandContributionItem.STYLE_PUSH, null, false) ));
		
		m.add(new Separator(IStatetUIMenuIds.GROUP_ADD_MORE_ID));
	}
	
}
