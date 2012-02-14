/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.DropDownButton;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCorePreferenceNodes;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.launching.RRunDebugPreferenceConstants;
import de.walware.statet.r.ui.RUI;


/**
 * Preference page for R (Environment) configuration of the workbench.
 */
public class REnvPreferencePage extends ConfigurationBlockPreferencePage<REnvConfigurationBlock> {
	
	
	public static final String PREF_PAGE_ID = "de.walware.statet.r.preferencePages.REnvironmentPage"; //$NON-NLS-1$
	
	
	public REnvPreferencePage() {
	}
	
	
	@Override
	protected REnvConfigurationBlock createConfigurationBlock() throws CoreException {
		return new REnvConfigurationBlock(null, createStatusChangedListener());
	}
	
}

class REnvConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private final static int ADD_NEW_DEFAULT = ButtonGroup.ADD_NEW;
	private final static int ADD_NEW_REMOTE = ButtonGroup.ADD_NEW | (0x1 << 8);
	
	
	private TableViewer fListViewer;
	private ButtonGroup<IREnvConfiguration.WorkingCopy> fListButtons;
	
	private final IObservableList fList = new WritableList();
	private final IObservableValue fDefault = new WritableValue();
	private final IObservableValue fListStatus = new WritableValue();
	
	private ComboViewer fIndexConsoleViewer;
	private Button fNetworkEclipseControl;
	
	
	protected REnvConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project, statusListener);
	}
	
	
	@Override
	protected String getHelpContext() {
		return IRUIHelpContextIds.R_ENV;
	}
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		prefs.put(RRunDebugPreferenceConstants.PREF_RENV_CHECK_UPDATE, null);
		setupPreferenceManager(prefs);
		
		final Label label = new Label(pageComposite, SWT.LEFT);
		label.setText(Messages.REnv_REnvList_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		{	// Table area
			final Composite composite = new Composite(pageComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Composite table = createTable(composite);
			{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				gd.heightHint = LayoutUtil.hintHeight(fListViewer.getTable(), 10);
				table.setLayoutData(gd);
			}
			
			fListButtons = new ButtonGroup<IREnvConfiguration.WorkingCopy>(composite) {
				@Override
				protected IREnvConfiguration.WorkingCopy edit1(final int command,
						final IREnvConfiguration.WorkingCopy config, final Object parent) {
					final boolean newConfig = ((command & ButtonGroup.ADD_ANY) != 0);
					final IREnvConfiguration.WorkingCopy editConfig;
					if (newConfig) {
						if (config != null) { // copy
							editConfig = RCore.getREnvManager().newConfiguration(config.getType());
							editConfig.load(config);
						}
						else { // add
							if (command == ADD_NEW_REMOTE) {
								editConfig = RCore.getREnvManager()
										.newConfiguration(IREnvConfiguration.USER_REMOTE_TYPE);
							}
							else {
								editConfig = RCore.getREnvManager()
										.newConfiguration(IREnvConfiguration.USER_LOCAL_TYPE);
							}
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
					REnvConfigurationBlock.this.updateStatus();
				}
			};
			fListButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			final SelectionListener addDefaultListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					fListButtons.editElement(ADD_NEW_DEFAULT, null);
				}
			};
			final DropDownButton addButton = new DropDownButton(fListButtons) {
				@Override
				protected void fillDropDownMenu(final Menu menu) {
					{	final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
						menuItem.setText(Messages.REnv_Add_Local_label);
						menuItem.addSelectionListener(addDefaultListener);
					}
					{	final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
						menuItem.setText(Messages.REnv_Add_Remote_label);
						menuItem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent e) {
								fListButtons.editElement(ADD_NEW_REMOTE, null);
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
				@Override
				public int hashCode(final Object element) {
					if (element instanceof IREnvConfiguration) {
						return ((IREnvConfiguration) element).getReference().hashCode();
					}
					return element.hashCode();
				}
				
				@Override
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
		
		final Composite indexOptions = createIndexOptions(pageComposite);
		indexOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite networkOptions = createNetworkOptions(pageComposite);
		networkOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		initBindings();
		updateStatus();
		getDbc().addValidationStatusProvider(new ValidationStatusProvider() {
			@Override
			public IObservableValue getValidationStatus() {
				return fListStatus;
			}
			@Override
			public IObservableList getModels() {
				return Observables.staticObservableList(getDbc().getValidationRealm(),
						Collections.emptyList());
			}
			@Override
			public IObservableList getTargets() {
				return Observables.staticObservableList(getDbc().getValidationRealm(),
						Collections.emptyList());
			}
		});
		
		updateControls();
		fListButtons.refresh();
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
	
	private Composite createTable(final Composite parent) {
		final TableComposite composite = new ViewerUtil.TableComposite(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
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
					if (config.getType() == IREnvConfiguration.EPLUGIN_LOCAL_TYPE) {
						return "<embedded>";
					}
					return ""; //$NON-NLS-1$
				}
			});
		}
		
		composite.viewer.setContentProvider(new ArrayContentProvider());
		// Sorter
		composite.viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				return getComparator().compare(((IREnvConfiguration) e1).getName(), ((IREnvConfiguration) e2).getName());
			}
		});
		
		return composite;
	}
	
	private Composite createIndexOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		composite.setText(Messages.REnv_Index_label);
		
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		label.setText(Messages.REnv_Update_Console_label);
		
		fIndexConsoleViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fIndexConsoleViewer.getControl().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		
		fIndexConsoleViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element.equals(RRunDebugPreferenceConstants.AUTO)) {
					return Messages.REnv_Update_Console_Auto_label;
				}
				if (element.equals(RRunDebugPreferenceConstants.ASK)) {
					return Messages.REnv_Update_Console_Ask_label;
				}
				if (element.equals(RRunDebugPreferenceConstants.DISABLED)) {
					return Messages.REnv_Update_Console_Disabled_label;
				}
				return ""; //$NON-NLS-1$
			}
		});
		fIndexConsoleViewer.setContentProvider(new ArrayContentProvider());
		fIndexConsoleViewer.setInput(new String[] {
				RRunDebugPreferenceConstants.AUTO,
				RRunDebugPreferenceConstants.ASK,
				RRunDebugPreferenceConstants.DISABLED,
		});
		
		return composite;
	}
	
	private Composite createNetworkOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		composite.setText(Messages.REnv_Network_label);
		
		{	Composite line = new Composite(composite, SWT.NONE);
			line.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			final GridLayout layout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 2);
			layout.horizontalSpacing = 0;
			line.setLayout(layout);
			
			fNetworkEclipseControl = new Button(line, SWT.CHECK);
			final int idx = Messages.REnv_Network_UseEclipse_label.indexOf("<a");
			fNetworkEclipseControl.setText(Messages.REnv_Network_UseEclipse_label.substring(0, idx).trim());
			fNetworkEclipseControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			final Link link = addLinkControl(line, Messages.REnv_Network_UseEclipse_label.substring(idx));
			link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		dbc.bindValue(ViewersObservables.observeSingleSelection(fIndexConsoleViewer),
				createObservable(RRunDebugPreferenceConstants.PREF_RENV_CHECK_UPDATE),
				null, null );
		dbc.bindValue(SWTObservables.observeSelection(fNetworkEclipseControl),
				createObservable(RCorePreferenceNodes.PREF_RENV_NETWORK_USE_ECLIPSE),
				null, null );
	}
	
	
	@Override
	public void performApply() {
		super.performApply();
		saveValues(true);
	}
	@Override
	public boolean performOk() {
		final boolean superOk = super.performOk();
		if (fListButtons.isDirty()) {
			return superOk | saveValues(false);
		}
		return superOk;
	}
	
	
	private void updateStatus() {
		fListStatus.setValue((fDefault.getValue() == null) ?
				ValidationStatus.warning(Messages.REnv_warning_NoDefaultConfiguration_message) :
				ValidationStatus.ok() );
	}
	
	private boolean saveValues(final boolean saveStore) {
		try {
			final IREnvConfiguration defaultREnv = (IREnvConfiguration) fDefault.getValue();
			final String[] groupIds = RCore.getREnvManager().set(
					(IREnvConfiguration[]) fList.toArray(new IREnvConfiguration[fList.size()]),
					(defaultREnv != null) ? defaultREnv.getReference().getId() : null);
			if (groupIds != null) {
				ConfigurationBlock.scheduleChangeNotification(
						getContainer(), groupIds, saveStore);
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
			final IREnvConfiguration config = (rEnvConfig.isEditable()) ? rEnvConfig.createWorkingCopy() : rEnvConfig;
			fList.add(config);
			if (config.getReference() == defaultEnv) {
				fDefault.setValue(config);
			}
		}
	}
	
}
