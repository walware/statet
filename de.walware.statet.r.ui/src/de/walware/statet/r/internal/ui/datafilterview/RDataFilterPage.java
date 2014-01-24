/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import static de.walware.ecommons.ui.actions.HandlerContributionItem.NO_COMMAND_ID;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.DNDUtil;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.contentfilter.IFilterPage;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.dataeditor.RDataEditor;
import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;
import de.walware.statet.r.internal.ui.datafilter.FilterSet;
import de.walware.statet.r.internal.ui.datafilter.IFilterListener;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.IRDataTableListener;
import de.walware.statet.r.ui.dataeditor.RDataTableComposite;


public class RDataFilterPage extends Page implements IFilterPage {
	
	
	private static final String EXPAND_ALL_COMMAND_ID = IWorkbenchCommandConstants.NAVIGATE_EXPAND_ALL;
	private static final String COLLAPSE_ALL_COMMAND_ID = IWorkbenchCommandConstants.NAVIGATE_COLLAPSE_ALL;
	private static final String COPY_EXPR_COMMAND_ID = ISourceEditorCommandIds.COPY_ELEMENT_NAME;
	
	
	private class ExpandCollapseAllHandler extends AbstractHandler {
		
		
		private final boolean fExpanded;
		
		
		public ExpandCollapseAllHandler(final boolean expanded) {
			fExpanded = expanded;
		}
		
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			fScrollComposite.setRedraw(false);
			fScrollComposite.setDelayedReflow(true);
			try {
				final List<VariableComposite> variables = fContainer.getVariables();
				for (final VariableComposite variable : variables) {
					variable.setExpanded(fExpanded);
				}
			}
			finally {
				fScrollComposite.setDelayedReflow(false);
				fScrollComposite.reflow(true);
				fScrollComposite.setRedraw(true);
			}
			return null;
		}
		
	}
	
	private class DisableFiltersHandler extends AbstractHandler implements IElementUpdater {
		
		
		public DisableFiltersHandler() {
		}
		
		
		@Override
		public void updateElement(final UIElement element, final Map parameters) {
			final FilterSet filterSet = fContainer.getFilterSet();
			element.setChecked(!filterSet.getEnabled());
		}
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final FilterSet filterSet = fContainer.getFilterSet();
			filterSet.setEnabled(!filterSet.getEnabled());
			return null;
		}
		
	}
	
	private class CopyFilterExpr extends AbstractHandler {
		
		
		public CopyFilterExpr() {
		}
		
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final FilterSet filterSet = fContainer.getFilterSet();
			final String rExpression = filterSet.getFilterRExpression(null, 0);
			if (rExpression != null) {
				final Clipboard clipboard = new Clipboard(fComposite.getDisplay());
				try {
					return DNDUtil.setContent(clipboard, 
							new Object[] { rExpression }, 
							new Transfer[] { TextTransfer.getInstance() });
				}
				finally {
					clipboard.dispose();
				}
			}
			return null;
		}
		
	}
	
	
	private Composite fComposite;
	
	private ScrolledPageComposite fScrollComposite;
	
	private Text fFilterText;
	
	private VariableContainer fContainer;
	private IFilterListener fFilterPostListener;
	
	private final RDataEditor fEditor;
	private IRDataTableListener fRDataTableListener;
	
	private HandlerContributionItem fDisableItem;
	
	
	public RDataFilterPage(final RDataEditor editor) {
		fEditor = editor;
	}
	
	
	public RDataEditor getDataEditor() {
		return fEditor;
	}
	
	protected IDialogSettings getDialogSettings() {
		return DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "RDataFilterPage"); //$NON-NLS-1$
	}
	
	@Override
	public void init(final IPageSite pageSite) {
		super.init(pageSite);
		
		final IHandlerService handlerService = (IHandlerService) pageSite.getService(IHandlerService.class);
		final IToolBarManager toolBarManager = pageSite.getActionBars().getToolBarManager();
		final IMenuManager menuManager = pageSite.getActionBars().getMenuManager();
		
		{	final IHandler2 handler = new ExpandCollapseAllHandler(true);
			handlerService.activateHandler(EXPAND_ALL_COMMAND_ID, handler);
		}
		{	final IHandler2 handler = new ExpandCollapseAllHandler(false);
			handlerService.activateHandler(COLLAPSE_ALL_COMMAND_ID, handler);
			toolBarManager.add(new HandlerContributionItem(new CommandContributionItemParameter(pageSite,
							null, COLLAPSE_ALL_COMMAND_ID, null,
							null, null, null,
							null, null, null,
							HandlerContributionItem.STYLE_PUSH, null, false),
					handler));
		}
		{	final IHandler2 handler = new DisableFiltersHandler();
			fDisableItem = new HandlerContributionItem(new CommandContributionItemParameter(pageSite,
							null, NO_COMMAND_ID, null,
							SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_DISABLE_FILTER_IMAGE_ID), null, null,
							Messages.Variables_DisableFilters_label, null, null,
							HandlerContributionItem.STYLE_CHECK, null, false),
					handler);
			toolBarManager.add(fDisableItem);
		}
		{	final IHandler2 handler = new CopyFilterExpr();
			handlerService.activateHandler(COPY_EXPR_COMMAND_ID, handler);
			menuManager.add(new CommandContributionItem(new CommandContributionItemParameter(pageSite,
					null, COPY_EXPR_COMMAND_ID, null,
					null, null, null,
					Messages.Variables_CopyExpr_label, null, null,
					CommandContributionItem.STYLE_PUSH, null, false )));
		}
	}
	
	@Override
	public void createControl(final Composite parent) {
		fComposite = new Composite(parent, SWT.NONE);
		fComposite.setLayout(LayoutUtil.createCompositeGrid(1));
		
		fScrollComposite = new ScrolledPageComposite(fComposite, SWT.V_SCROLL);
		fScrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fContainer = new VariableContainer(getSite(), fScrollComposite);
		
		fFilterText = new Text(fComposite, SWT.LEFT_TO_RIGHT | SWT.READ_ONLY);
		fFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fContainer.getFilterSet().addListener(new IFilterListener() {
			@Override
			public void filterChanged() {
				if (!UIAccess.isOkToUse(fFilterText)) {
					return;
				}
				fDisableItem.update();
				
				final String rExpression = fContainer.getFilterSet().getFilterRExpression();
				fFilterText.setText((rExpression != null) ? rExpression : ""); //$NON-NLS-1$
			}
		});
		fDisableItem.update();
		
		init();
	}
	
	protected void init() {
		fRDataTableListener = new IRDataTableListener() {
			@Override
			public void inputChanged(final IRDataTableInput input, final RDataTableContentDescription description) {
				if (description != null) {
					fContainer.updateInput(description);
				}
			}
		};
		fEditor.getRDataTable().addTableListener(fRDataTableListener);
		fFilterPostListener = new IFilterListener() {
			@Override
			public void filterChanged() {
				final RDataTableComposite rDataTable = fEditor.getRDataTable();
				if (!UIAccess.isOkToUse(rDataTable)) {
					return;
				}
				if (rDataTable.getDescription() == fContainer.getDescription()) {
					final FilterSet filterSet = fContainer.getFilterSet();
					if (filterSet.getEnabled()) {
						rDataTable.setFilter(fContainer.getFilterSet().getFilterRExpression());
					}
					else {
						rDataTable.setFilter(null);
					}
				}
			}
		};
		fContainer.getFilterSet().addPostListener(fFilterPostListener);
	}
	
	@Override
	public Control getControl() {
		return fComposite;
	}
	
	@Override
	public void setFocus() {
		fScrollComposite.getContent().setFocus();
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		
		RDataTableComposite rDataTable = fEditor.getRDataTable();
		if (!UIAccess.isOkToUse(rDataTable)) {
			rDataTable = null;
		}
		if (fRDataTableListener != null && rDataTable != null) {
			rDataTable.removeTableListener(fRDataTableListener);
			fRDataTableListener = null;
		}
		if (fFilterPostListener != null) {
			fContainer.getFilterSet().removePostListener(fFilterPostListener);
			fFilterPostListener = null;
		}
		if (rDataTable != null) {
			rDataTable.setFilter(null);
		}
		fContainer.dispose();
	}
	
}
