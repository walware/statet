/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.actions.ControlServicesUtil;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.components.DropDownButton;
import de.walware.ecommons.ui.components.SearchText;
import de.walware.ecommons.ui.util.AutoCheckController;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;

import de.walware.rj.data.RCharacterStore;

import de.walware.statet.r.internal.ui.datafilter.TextSearchType;
import de.walware.statet.r.internal.ui.datafilter.TextVariableFilter;


public class TextClient extends FilterClient {
	
	
	private class RemoveHandler extends AbstractHandler implements IElementUpdater {
		
		@Override
		public void setEnabled(final Object evaluationContext) {
			setBaseEnabled(!fValueListViewer.getSelection().isEmpty());
		}
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final IStructuredSelection selection = (IStructuredSelection) fValueListViewer.getSelection();
			fFilter.removeValues(selection.toList());
			updateInput();
			return null;
		}
		
		@Override
		public void updateElement(final UIElement element, final Map parameters) {
			element.setText(Messages.Items_Remove_label);
			element.setTooltip(Messages.Items_RemoveSelected_label);
		}
		
	}
	
	private class RemoveUncheckedHandler extends AbstractHandler {
		
		@Override
		public void setEnabled(final Object evaluationContext) {
			setBaseEnabled(fAvailableValues.getLength() > fSelectedValueSet.size());
		}
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final List<String> values= new ArrayList<>((int) fAvailableValues.getLength() - fSelectedValueSet.size());
			for (int i = 0; i < fAvailableValues.getLength(); i++) {
				final String value = fAvailableValues.get(i);
				if (!fSelectedValueSet.contains(value)) {
					values.add(value);
				}
			}
			fFilter.removeValues(values);
			updateInput();
			return null;
		}
		
	}
	
	private class RemoveAllHandler extends AbstractHandler {
		
		@Override
		public void setEnabled(final Object evaluationContext) {
			setBaseEnabled(fAvailableValues.getLength() > 0);
		}
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			fFilter.removeAllValues();
			updateInput();
			return null;
		}
		
	}
	
	
	private SearchText fSearchTextControl;
	private DropDownButton fSearchButtonControl;
	private MenuItem[] fSearchMenuItems;
	
	private TextSearchType fSearchType;
	
	private CheckboxTableViewer fValueListViewer;
	
	private RCharacterStore fAvailableValues;
	
	private final WritableSet fSelectedValueSet;
	
	private final HandlerCollection fValueListHandlers = new HandlerCollection();
	private MenuManager fValueListMenuManager;
	
	private final TextVariableFilter fFilter;
	
	
	public TextClient(final VariableComposite parent, final TextVariableFilter filter) {
		super(parent);
		
		fFilter = filter;
		filter.setListener(this);
		
		fAvailableValues = filter.getAvailableValues();
		fSelectedValueSet = filter.getSelectedValues();
		fSearchType = TextSearchType.ECLIPSE;
		init(2);
	}
	
	
	@Override
	public TextVariableFilter getFilter() {
		return fFilter;
	}
	
	@Override
	protected void addWidgets() {
		fSearchTextControl = new SearchText(this, "", SWT.FLAT); //$NON-NLS-1$
		fSearchTextControl.addListener(new SearchText.Listener() {
			@Override
			public void textChanged(final boolean user) {
			}
			@Override
			public void okPressed() {
				search(null);
			}
			@Override
			public void downPressed() {
			}
		});
		fSearchTextControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fSearchButtonControl = new DropDownButton(this, SWT.FLAT);
		fSearchButtonControl.setText(Messages.Items_Search_label);
		{	final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.heightHint = fSearchButtonControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			final int textHeight = fSearchTextControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			if (gd.heightHint - textHeight > 2) {
				gd.heightHint = textHeight + 2;
			}
			fSearchButtonControl.setLayoutData(gd);
		}
		fSearchButtonControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				search(null);
			}
		});
		{	final Menu searchMenu = fSearchButtonControl.getDropDownMenu();
			final List<TextSearchType> searchTypes = TextSearchType.TYPES;
			fSearchMenuItems = new MenuItem[searchTypes.size()];
			final Listener searchTypeListener = new Listener() {
				@Override
				public void handleEvent(final Event event) {
					search((TextSearchType) event.widget.getData());
				}
			};
			for (int id = 0; id < searchTypes.size(); id++) {
				final TextSearchType type = searchTypes.get(id);
				final MenuItem item = new MenuItem(searchMenu, SWT.RADIO);
				item.setText(type.getLabel());
				item.setData(type);
				item.addListener(SWT.Selection, searchTypeListener);
				fSearchMenuItems[id] = item;
			}
		}
		setSearchType(fSearchType);
		
		fValueListViewer = CheckboxTableViewer.newCheckList(this, SWT.MULTI | SWT.FLAT | SWT.FULL_SELECTION);
		fValueListViewer.setContentProvider(new RStoreContentProvider());
		fValueListViewer.setLabelProvider(new ColumnLabelProvider(fFilter.getColumn()));
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator) {
		final ControlServicesUtil servicesUtil = new ControlServicesUtil(serviceLocator,
				getClass().getName()+"/ValueList#"+hashCode(), this ); //$NON-NLS-1$
		servicesUtil.addControl(fValueListViewer.getTable());
		
		final AutoCheckController autoCheckController = new AutoCheckController(fValueListViewer, fSelectedValueSet);
		{	final IHandler2 handler = autoCheckController.createSelectAllHandler();
			fValueListHandlers.add(SELECT_ALL_COMMAND_ID, handler);
			servicesUtil.activateHandler(SELECT_ALL_COMMAND_ID, handler);
		}
		{	final IHandler2 handler = new RemoveHandler();
			fValueListHandlers.add(REMOVE_COMMAND_ID, handler);
			servicesUtil.activateHandler(IWorkbenchCommandConstants.EDIT_DELETE, handler);
		}
		{	final IHandler2 handler = new RemoveUncheckedHandler();
			fValueListHandlers.add(REMOVE_UNCHECKED_HANDLER_ID, handler);
		}
		{	final IHandler2 handler = new RemoveAllHandler();
			fValueListHandlers.add(REMOVE_ALL_HANDLER_COMMAND_ID, handler);
		}
		
		ViewerUtil.installSearchTextNavigation(fValueListViewer, fSearchTextControl, true);
		
		fValueListMenuManager = new MenuManager();
		fValueListMenuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, SELECT_ALL_COMMAND_ID, HandlerContributionItem.STYLE_PUSH),
				fValueListHandlers.get(SELECT_ALL_COMMAND_ID) ));
		fValueListMenuManager.add(new Separator());
		fValueListMenuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, REMOVE_COMMAND_ID, HandlerContributionItem.STYLE_PUSH),
				fValueListHandlers.get(REMOVE_COMMAND_ID) ));
		fValueListMenuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, HandlerContributionItem.NO_COMMAND_ID, null,
						null, null, null,
						Messages.Items_RemoveUnchecked_label, null, "Remove unchecked items",
						HandlerContributionItem.STYLE_PUSH, null, false),
				fValueListHandlers.get(REMOVE_UNCHECKED_HANDLER_ID) ));
		fValueListMenuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(serviceLocator,
						null, HandlerContributionItem.NO_COMMAND_ID, null,
						null, null, null,
						Messages.Items_RemoveAll_label, null, "Remove all items",
						HandlerContributionItem.STYLE_PUSH, null, false),
				fValueListHandlers.get(REMOVE_ALL_HANDLER_COMMAND_ID) ));
		fValueListViewer.getTable().setMenu(fValueListMenuManager.createContextMenu(fValueListViewer.getControl()));
		
		fValueListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				updateActions();
			}
		});
		updateActions();
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindSet(
				ViewersObservables.observeCheckedElements(fValueListViewer, Object.class),
				fSelectedValueSet,
				new UpdateSetStrategy().setConverter(UI2RStoreConverter.INSTANCE),
				new UpdateSetStrategy().setConverter(RStore2UIConverter.INSTANCE) );
	}
	
	@Override
	protected void updateInput() {
		fAvailableValues = fFilter.getAvailableValues();
		fValueListViewer.setInput(fAvailableValues);
		
		updateActions();
		if (updateLayout()) {
			getParent().layout(new Control[] { this });
		}
	}
	
	protected void updateActions() {
		fValueListHandlers.get(REMOVE_COMMAND_ID).setEnabled(null);
	}
	
	@Override
	protected boolean updateLayout() {
		return updateLayout(fValueListViewer, (int) fAvailableValues.getLength());
	}
	
	@Override
	protected int getMinHeightPadding() {
		return fSearchTextControl.getSize().y + 20 + 10 * LayoutUtil.defaultVSpacing();
	}
	
	protected void setSearchType(final TextSearchType type) {
		fSearchType = type;
		for (int id = 0; id < fSearchMenuItems.length; id++) {
			fSearchMenuItems[id].setSelection(type.getId() == id);
		}
		fSearchButtonControl.setToolTipText(type.getLabel());
	}
	
	private void search(TextSearchType type) {
		if (type != null) {
			if (fSearchType != type) {
				setSearchType(type);
			}
		}
		else {
			type = fSearchType;
		}
		final String text = fSearchTextControl.getText();
		fFilter.search(type, text);
	}
	
	@Override
	protected void onDispose() {
		if (fValueListMenuManager != null) {
			fValueListMenuManager.dispose();
			fValueListMenuManager = null;
		}
	}
	
}
