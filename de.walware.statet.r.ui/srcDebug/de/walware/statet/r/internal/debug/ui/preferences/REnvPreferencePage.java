/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.DropDownButton;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.ui.RUI;


/**
 * Preference page for R (Environment) configuration of the workbench.
 */
public class REnvPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	
	public static final String PREF_PAGE_ID = "de.walware.statet.r.preferencePages.REnvironmentPage"; //$NON-NLS-1$
	
	
	private TableViewer fListViewer;
	private ButtonGroup<IREnvConfiguration.WorkingCopy> fListButtons;
	
	private IObservableList fList;
	private IObservableValue fDefault;
	
	
	public REnvPreferencePage() {
	}
	
	public void init(final IWorkbench workbench) {
		fList = new WritableList();
		fDefault = new WritableValue();
	}
	
	
	@Override
	protected Control createContents(final Composite parent) {
		final Composite pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		final Label label = new Label(pageComposite, SWT.LEFT);
		label.setText(Messages.REnv_REnvList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		{	// Table area
			final Composite composite = new Composite(pageComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Composite table = createTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			fListButtons = new ButtonGroup<IREnvConfiguration.WorkingCopy>(composite) {
				@Override
				protected IREnvConfiguration.WorkingCopy edit1(final IREnvConfiguration.WorkingCopy config, final boolean newConfig, final Object parent) {
					IREnvConfiguration.WorkingCopy editConfig;
					if (newConfig) {
						if (config != null) {
							editConfig = RCore.getREnvManager().newConfiguration(config.getType());
							editConfig.load(config);
						}
						else {
							return null;
						}
					}
					else {
						editConfig = config.createWorkingCopy();
					}
					if (edit(editConfig, newConfig)) {
						if (newConfig) {
							return editConfig;
						}
						else {
							config.load(editConfig);
							return config;
						}
					}
					return null;
				}
				@Override
				protected boolean isModifyAllowed(final Object element) {
					final IREnvConfiguration config = (IREnvConfiguration) element;
					return config.isEditable();
				}
				@Override
				public void updateState() {
					super.updateState();
					REnvPreferencePage.this.updateStatus();
				}
			};
			fListButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			final SelectionListener addDefaultListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					final IREnvConfiguration.WorkingCopy config = RCore.getREnvManager()
							.newConfiguration(IREnvConfiguration.USER_LOCAL_TYPE);
					if (edit(config, true)) {
						fList.add(config);
						fListViewer.refresh();
					}
				}
			};
			final DropDownButton addButton = new DropDownButton(fListButtons) {
				@Override
				protected void fillDropDownMenu(final Menu menu) {
					{	final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
						menuItem.setText("Add Local (default)...");
						menuItem.addSelectionListener(addDefaultListener);
					}
					{	final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
						menuItem.setText("Add Remote...");
						menuItem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								final IREnvConfiguration.WorkingCopy config = RCore.getREnvManager()
										.newConfiguration(IREnvConfiguration.USER_REMOTE_TYPE);
								if (edit(config, true)) {
									fList.add(config);
									fListButtons.setDirty(true);
									fListViewer.refresh();
								}
							}
						});
					}
				}
			};
			addButton.addSelectionListener(addDefaultListener);
			addButton.setText(SharedMessages.CollectionEditing_AddItem_label);
			addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fListButtons.addCopyButton();
			fListButtons.addEditButton();
			fListButtons.addDeleteButton();
			fListButtons.addSeparator();
			fListButtons.addDefaultButton();
			
			fListButtons.connectTo(fListViewer, fList, fDefault);
			fListViewer.setComparer(new IElementComparer() {
				public int hashCode(final Object element) {
					if (element instanceof IREnvConfiguration) {
						return ((IREnvConfiguration) element).getReference().hashCode();
					}
					return element.hashCode();
				}
				
				public boolean equals(final Object a, final Object b) {
					if (a instanceof IREnvConfiguration && b instanceof IREnvConfiguration) {
						return ((IREnvConfiguration) a).getReference().equals(
								((IREnvConfiguration) b).getReference());
					}
					return a.equals(b);
				}
			});
			fListViewer.setInput(fList);
		}
		
		loadValues(PreferencesUtil.getInstancePrefs());
		fListButtons.refresh();
		
		applyDialogFont(pageComposite);
		return pageComposite;
	}
	
	private boolean edit(final IREnvConfiguration.WorkingCopy config, final boolean newConfig) {
		final List<IREnvConfiguration> existingConfigs = new ArrayList<IREnvConfiguration>(fList);
		if (!newConfig) {
			for (final Iterator<IREnvConfiguration> iter = existingConfigs.iterator(); iter.hasNext();) {
				final IREnvConfiguration existing = iter.next();
				if (existing.getReference() == config.getReference()) {
					iter.remove();
					break;
				}
			}
		}
		Dialog dialog;
		if (config.getType() == IREnvConfiguration.USER_LOCAL_TYPE) {
			dialog = new REnvLocalConfigDialog(getShell(),
					config, newConfig, existingConfigs);
		}
		else if (config.getType() == IREnvConfiguration.USER_REMOTE_TYPE) {
			dialog = new REnvRemoteConfigDialog(getShell(),
					config, newConfig, existingConfigs);
		}
		else {
			return false;
		}
		return (dialog.open() == Dialog.OK);
	}
	
	protected Composite createTable(final Composite parent) {
		final TableComposite composite = new ViewerUtil.TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		fListViewer = composite.viewer;
		composite.table.setHeaderVisible(true);
		composite.table.setLinesVisible(true);
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(Messages.REnv_NameColumn_name);
			column.setLabelProvider(new REnvLabelProvider(fDefault));
		}
		
		{	final TableViewerColumn column = new TableViewerColumn(composite.viewer, SWT.NONE);
			composite.layout.setColumnData(column.getColumn(), new ColumnWeightData(1));
			column.getColumn().setText(Messages.REnv_LocationColumn_name);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public Image getImage(final Object element) {
					return super.getImage(element);
				}
				@Override
				public String getText(final Object element) {
					final IREnvConfiguration config = (IREnvConfiguration) element;
					if (config.getType() == IREnvConfiguration.USER_LOCAL_TYPE) {
						return config.getRHome();
					}
					return ""; //$NON-NLS-1$
				}
			});
		}
		
		composite.viewer.setContentProvider(new ArrayContentProvider());
		// Sorter
		composite.viewer.setComparator(new ViewerComparator() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				return getComparator().compare(((IREnvConfiguration.WorkingCopy) e1).getName(), ((IREnvConfiguration.WorkingCopy) e2).getName());
			}
		});
		
		return composite;
	}
	
	@Override
	protected void performDefaults() {
	}
	
	@Override
	public boolean performOk() {
		if (fListButtons.isDirty()) {
			return saveValues(false);
		}
		return true;
	}
	
	@Override
	protected void performApply() {
		saveValues(true);
	}
	
	
	private void updateStatus() {
		if (fDefault.getValue() == null) {
			setMessage(Messages.REnv_warning_NoDefaultConfiguration_message, IStatus.WARNING);
			return;
		}
		setMessage(null);
	}
	
	private boolean saveValues(final boolean saveStore) {
		try {
			final IREnvConfiguration.WorkingCopy defaultREnv = (IREnvConfiguration.WorkingCopy) fDefault.getValue();
			final String[] groupIds = RCore.getREnvManager().set(
					(IREnvConfiguration[]) fList.toArray(new IREnvConfiguration.WorkingCopy[fList.size()]),
					(defaultREnv != null) ? defaultREnv.getReference().getId() : null);
			if (groupIds != null) {
				ConfigurationBlock.scheduleChangeNotification(
						(IWorkbenchPreferenceContainer) getContainer(), groupIds, saveStore);
			}
			return true;
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					-1, Messages.REnv_error_Saving_message, e),
					StatusManager.LOG | StatusManager.SHOW);
			return false;
		}
	}
	
	private void loadValues(final IPreferenceAccess prefs) {
		fList.clear();
		fDefault.setValue(null);
		
		final IREnvManager manager = RCore.getREnvManager();
		final IREnv defaultEnv = manager.getDefault().resolve();
		final IREnvConfiguration[] configurations = manager.getConfigurations();
		for (final IREnvConfiguration rEnvConfig : configurations) {
			final IREnvConfiguration.WorkingCopy config = rEnvConfig.createWorkingCopy();
			fList.add(config);
			if (config.getReference() == defaultEnv) {
				fDefault.setValue(config);
			}
		}
	}
	
}
