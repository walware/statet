/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.databinding.jface.SWTMultiEnabledObservable;
import de.walware.ecommons.debug.ui.LaunchConfigTabWithDbc;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditorObservable;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.DataAdapter;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;

import de.walware.statet.nico.core.util.HistoryTrackingConfiguration;
import de.walware.statet.nico.core.util.TrackingConfiguration;
import de.walware.statet.nico.core.util.TrackingConfiguration2LaunchConfiguration;
import de.walware.statet.nico.ui.util.TrackingConfigurationComposite;
import de.walware.statet.nico.ui.util.TrackingConfigurationDialog;

import de.walware.statet.r.console.ui.launching.RConsoleLaunching;
import de.walware.statet.r.internal.console.ui.RConsoleMessages;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;


/**
 * Launch config tab for common R console options
 * <ul>
 *   <li>History (not yet implemented)</li>
 *   <li>Option to pin console page</li>
 *   <li>Option for a startup snippet</li>
 * </ul>
 */
public class RConsoleOptionsTab extends LaunchConfigTabWithDbc {
	
	
	static final String TRANSCRIPT_TRACKING_ID = "transcript"; //$NON-NLS-1$
	static final String CUSTOM_TRACKING_ID_PREFIX = "custom"; //$NON-NLS-1$
	
	static final String TRACKING_IDS = "tracking.ids"; //$NON-NLS-1$
	static final String TRACKING_ENABLED_IDS = "tracking.enabled.ids"; //$NON-NLS-1$
	
	static final TrackingConfiguration2LaunchConfiguration TRACKING_UTIL = new TrackingConfiguration2LaunchConfiguration();
	
	static final String ATTR_INTEGRATION_ROOT = "de.walware.statet.r.debug/integration"; //$NON-NLS-1$
	static final String ATTR_INTEGRATION_RHELP_ENABLED = ATTR_INTEGRATION_ROOT+"integration.rhelp.enabled"; //$NON-NLS-1$
	static final String ATTR_INTEGRATION_RGRAPHICS_ASDEFAULT = ATTR_INTEGRATION_ROOT+"integration.rgraphics.asdefault"; //$NON-NLS-1$
	static final String ATTR_INTEGRATION_RDBGEXT_ENABLED = ATTR_INTEGRATION_ROOT+"integration.rdbgext.enabled"; //$NON-NLS-1$
	
	
	private Button fPinControl;
	private WritableValue fPinValue;
	
	private CheckboxTableViewer fTrackingTable;
	private ButtonGroup<TrackingConfiguration> fTrackingButtons;
	private WritableList fTrackingList;
	private WritableSet fTrackingEnabledSet;
	private int fTrackingMaxCustomId;
	
	private SnippetEditor fStartupSnippetEditor;
	private WritableValue fStartupSnippetValue;
	
	private Button fRHelpByStatetControl;
	private WritableValue fRHelpByStatetValue;
	private Button fRGraphicsByStatetControl;
	private WritableValue fRGraphicsByStatetValue;
	private Button fRDbgExtControl;
	private WritableValue fRDbgExtValue;
	
	private Button fObjectDBEnabledControl;
	private WritableValue fObjectDBEnabledValue;
	private Button fObjectDBAutoEnabledControl;
	private WritableValue fObjectDBAutoEnabledValue;
	private Text fObjectDBListsChildrenControl;
	private WritableValue fObjectDBListsChildrenValue;
	private Text fObjectDBEnvsChildrenControl;
	private WritableValue fObjectDBEnvsChildrenValue;
	
	
	public RConsoleOptionsTab() {
	}
	
	
	@Override
	public String getName() {
		return RConsoleMessages.RConsole_OptionsTab_name;
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(GridLayoutFactory.swtDefaults().create());
		
		final Composite consoleComposite = createConsoleOptions(mainComposite);
		if (consoleComposite != null) {
			consoleComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		final Composite trackingComposite = createTrackingOptions(mainComposite);
		if (trackingComposite != null) {
			trackingComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		
		{	// Snippet options:
			final Group group = new Group(mainComposite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
			group.setText("R snippet run after startup:");
			createSnippetOptions(group);
		}
		
		{	// Object DB options:
			final Group group = new Group(mainComposite, SWT.NONE);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			group.setText("Eclipse Integration:");
			createEclipseOptions(group);
		}
		
		Dialog.applyDialogFont(parent);
		initBindings();
		fTrackingButtons.updateState();
	}
	
	private Composite createConsoleOptions(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText(RConsoleMessages.RConsole_MainTab_ConsoleOptions_label);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		
		fPinControl = new Button(group, SWT.CHECK);
		fPinControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fPinControl.setText(RConsoleMessages.RConsole_MainTab_ConsoleOptions_Pin_label);
		
		return group;
	}
	
	private Composite createTrackingOptions(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText("History / Transcript / Tracking:");
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		
		final ViewerUtil.CheckboxTableComposite trackingTable;
		{	trackingTable = new ViewerUtil.CheckboxTableComposite(group, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = LayoutUtil.hintHeight(trackingTable.table, 5);
			trackingTable.setLayoutData(gd);
			fTrackingTable = trackingTable.viewer;
		}
		{	final TableViewerColumn column = trackingTable.addColumn("Name", SWT.LEFT, new ColumnWeightData(100));
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final TrackingConfiguration config = (TrackingConfiguration) cell.getElement();
					cell.setText(config.getName());
				}
			});
		}
		
		fTrackingButtons = new ButtonGroup<TrackingConfiguration>(group) {
			@Override
			protected TrackingConfiguration edit1(TrackingConfiguration item, final boolean newItem, final Object parent) {
				TrackingConfigurationDialog dialog;
				if (!newItem && item != null && item.getId().equals(HistoryTrackingConfiguration.HISTORY_TRACKING_ID)) {
					item = new HistoryTrackingConfiguration(item.getId(), (HistoryTrackingConfiguration) item);
					dialog = new TrackingConfigurationDialog(RConsoleOptionsTab.this.getShell(), item, false) {
						@Override
						protected TrackingConfigurationComposite createConfigComposite(final Composite parent) {
							return new RHistoryConfigurationComposite(parent);
						}
					};
				}
				else {
					if (newItem) {
						final String id = CUSTOM_TRACKING_ID_PREFIX + (fTrackingMaxCustomId + 1);
						if (item == null) {
							item = new TrackingConfiguration(id);
						}
						else {
							item = new TrackingConfiguration(id, item);
						}
					}
					else {
						item = new TrackingConfiguration(item.getId(), item);
					}
					dialog = new TrackingConfigurationDialog(RConsoleOptionsTab.this.getShell(), item, newItem) {
						@Override
						protected TrackingConfigurationComposite createConfigComposite(final Composite parent) {
							return new RTrackingConfigurationComposite(parent);
						}
					};
				}
				if (dialog.open() == Dialog.OK) {
					if (newItem) {
						fTrackingMaxCustomId++;
					}
					return item;
				}
				return null;
			}
		};
		fTrackingButtons.addAddButton(null);
		fTrackingButtons.addDeleteButton(null);
		fTrackingButtons.addEditButton(null);
		fTrackingButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		return group;
	}
	
	private void createSnippetOptions(final Composite container) {
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		final TemplateVariableProcessor templateVariableProcessor = new TemplateVariableProcessor();
		final RSourceViewerConfigurator configurator = new RTemplateSourceViewerConfigurator(
				null, templateVariableProcessor );
		fStartupSnippetEditor = new SnippetEditor(configurator);
		fStartupSnippetEditor.create(container, SnippetEditor.DEFAULT_MULTI_LINE_STYLE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = LayoutUtil.hintHeight(fStartupSnippetEditor.getSourceViewer().getTextWidget(), 8);
		fStartupSnippetEditor.getControl().setLayoutData(gd);
	}
	
	private void createEclipseOptions(final Composite container) {
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		
		{	fRHelpByStatetControl = new Button(container, SWT.CHECK);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			fRHelpByStatetControl.setLayoutData(gd);
			fRHelpByStatetControl.setText("Enable R Help by StatET for help functions in R ('help', 'help.start', '?')");
		}
		{	fRGraphicsByStatetControl = new Button(container, SWT.CHECK);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			fRGraphicsByStatetControl.setLayoutData(gd);
			fRGraphicsByStatetControl.setText("Set R Graphic view by StatET as default graphic device for new plots in R");
		}
		{	fRDbgExtControl = new Button(container, SWT.CHECK);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			fRDbgExtControl.setLayoutData(gd);
			fRDbgExtControl.setText("Enable extensions by StatET for improved debug support of R code");
		}
		
		{	fObjectDBEnabledControl = new Button(container, SWT.CHECK);
			fObjectDBEnabledControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			fObjectDBEnabledControl.setText("Enable Object DB (for Object Browser etc.)");
		}
		
		{	fObjectDBAutoEnabledControl = new Button(container, SWT.CHECK);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			fObjectDBAutoEnabledControl.setLayoutData(gd);
			fObjectDBAutoEnabledControl.setText("Refresh DB automatically (initial setting)");
		}
		{	final Label label = new Label(container, SWT.NONE);
			label.setText("Max length of R lists to fetch:");
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			label.setLayoutData(gd);
		}
		{	fObjectDBListsChildrenControl = new Text(container, SWT.BORDER);
			fObjectDBListsChildrenControl.setTextLimit(10);
			final GridData gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fObjectDBListsChildrenControl, 10);
			fObjectDBListsChildrenControl.setLayoutData(gd);
		}
		{	final Label label = new Label(container, SWT.NONE);
			label.setText("Max length of R environments to fetch:");
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			label.setLayoutData(gd);
		}
		{	fObjectDBEnvsChildrenControl = new Text(container, SWT.BORDER);
			fObjectDBEnvsChildrenControl.setTextLimit(10);
			final GridData gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fObjectDBEnvsChildrenControl, 10);
			fObjectDBEnvsChildrenControl.setLayoutData(gd);
		}
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fPinValue = new WritableValue(realm, Boolean.class);
		dbc.bindValue(SWTObservables.observeSelection(fPinControl), fPinValue, null, null);
		
		fTrackingList = new WritableList(realm, new ArrayList<Object>(), TrackingConfiguration.class);
		fTrackingTable.setContentProvider(new ObservableListContentProvider());
		fTrackingTable.setInput(fTrackingList);
		
		fStartupSnippetValue = new WritableValue(realm, String.class);
		dbc.bindValue(new SnippetEditorObservable(realm, fStartupSnippetEditor, SWT.Modify), fStartupSnippetValue, null, null);
		
		fRHelpByStatetValue = new WritableValue(realm, Boolean.class);
		fRGraphicsByStatetValue = new WritableValue(realm, Boolean.class);
		fRDbgExtValue = new WritableValue(realm, Boolean.class);
		dbc.bindValue(SWTObservables.observeSelection(fRHelpByStatetControl), fRHelpByStatetValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(fRGraphicsByStatetControl), fRGraphicsByStatetValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(fRDbgExtControl), fRDbgExtValue, null, null);
		
		fObjectDBEnabledValue = new WritableValue(realm, Boolean.class);
		fObjectDBAutoEnabledValue = new WritableValue(realm, Boolean.class);
		fObjectDBListsChildrenValue = new WritableValue(realm, Integer.class);
		fObjectDBEnvsChildrenValue = new WritableValue(realm, Integer.class);
		final ISWTObservableValue dbObs = SWTObservables.observeSelection(fObjectDBEnabledControl);
		dbc.bindValue(dbObs, fObjectDBEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeSelection(fObjectDBAutoEnabledControl), fObjectDBAutoEnabledValue, null, null);
		dbc.bindValue(SWTObservables.observeText(fObjectDBListsChildrenControl, SWT.Modify), fObjectDBListsChildrenValue, 
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(100, Integer.MAX_VALUE, "Invalid max value for length of R lists to fetch (100-).")), null);
		dbc.bindValue(SWTObservables.observeText(fObjectDBEnvsChildrenControl, SWT.Modify), fObjectDBEnvsChildrenValue, 
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(100, Integer.MAX_VALUE, "Invalid max value for length of R environments to fetch (100-).")), null);
		
		dbc.bindValue(new SWTMultiEnabledObservable(realm, new Control[] {
						fObjectDBAutoEnabledControl, fObjectDBEnvsChildrenControl, fObjectDBListsChildrenControl,
				}, null), dbObs, null, null);
		
		fTrackingButtons.connectTo(fTrackingTable,
				new DataAdapter.ListAdapter<TrackingConfiguration>(fTrackingList, null) {
			@Override
			public boolean isDeleteAllowed(Object element) {
				return (super.isDeleteAllowed(element)
						&& ((TrackingConfiguration) element).getId().startsWith(CUSTOM_TRACKING_ID_PREFIX) );
			}
		});
		
		fTrackingEnabledSet = new WritableSet(realm, new HashSet<Object>(), TrackingConfiguration.class);
		fTrackingButtons.setCheckedModel(fTrackingEnabledSet);
		dbc.bindSet(ViewersObservables.observeCheckedElements(fTrackingTable, TrackingConfiguration.class), fTrackingEnabledSet);
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		boolean pin;
		try {
			pin = configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false);
		}
		catch (final CoreException e) {
			pin = false;
			logReadingError(e);
		}
		fPinValue.setValue(pin);
		
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(ATTR_INTEGRATION_RHELP_ENABLED, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fRHelpByStatetValue.setValue(enabled);
		}
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(ATTR_INTEGRATION_RGRAPHICS_ASDEFAULT, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fRGraphicsByStatetValue.setValue(enabled);
		}
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(ATTR_INTEGRATION_RDBGEXT_ENABLED, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fRDbgExtValue.setValue(enabled);
		}
		
		String startupSnippet;
		try {
			startupSnippet = configuration.getAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET, ""); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			startupSnippet = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fStartupSnippetValue.setValue(startupSnippet);
		
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBEnabledValue.setValue(enabled);
		}
		{	boolean enabled = true;
			try {
				enabled = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, enabled);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBAutoEnabledValue.setValue(enabled);
		}
		{	int max = 10000;
			try {
				max = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, 10000);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBListsChildrenValue.setValue(max);
		}
		{	int max = 10000;
			try {
				max = configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, 10000);
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			fObjectDBEnvsChildrenValue.setValue(max);
		}
		
		{	fTrackingList.clear();
			fTrackingMaxCustomId = 0;
			List<String> trackingIds = Collections.EMPTY_LIST;
			try {
				trackingIds = configuration.getAttribute(TRACKING_IDS, Collections.EMPTY_LIST);
				for (final String id : trackingIds) {
					final TrackingConfiguration trackingConfig = id.equals(HistoryTrackingConfiguration.HISTORY_TRACKING_ID) ?
							new HistoryTrackingConfiguration(id) : new TrackingConfiguration(id);
					try {
						TRACKING_UTIL.load(trackingConfig, configuration);
						fTrackingList.add(trackingConfig);
						if (id.startsWith(CUSTOM_TRACKING_ID_PREFIX)) {
							try {
								final int num = Integer.parseInt(id.substring(CUSTOM_TRACKING_ID_PREFIX.length()));
								fTrackingMaxCustomId = Math.max(fTrackingMaxCustomId, num);
							} catch (final Exception e) {}
						}
					}
					catch (final CoreException e) {
						trackingIds.remove(id);
						logReadingError(e);
					}
				}
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
			if (!trackingIds.contains(HistoryTrackingConfiguration.HISTORY_TRACKING_ID)) {
				final TrackingConfiguration trackingConfig = new HistoryTrackingConfiguration(HistoryTrackingConfiguration.HISTORY_TRACKING_ID);
				trackingConfig.setName("History");
				trackingConfig.setFilePath(RHistoryConfigurationComposite.HISTORY_TRACKING_DEFAULT_PATH);
				fTrackingList.add(trackingConfig);
			}
			if (!trackingIds.contains(TRANSCRIPT_TRACKING_ID)) {
				final TrackingConfiguration trackingConfig = new TrackingConfiguration(TRANSCRIPT_TRACKING_ID);
				trackingConfig.setName("Transcript");
				trackingConfig.setFilePath(RTrackingConfigurationComposite.TRANSCRIPT_TRACKING_DEFAULT_PATH);
				fTrackingList.add(trackingConfig);
			}
		}
		
		{	fTrackingEnabledSet.clear();
			List<String> trackingEnabledIds = Collections.EMPTY_LIST;
			try {
				trackingEnabledIds = configuration.getAttribute(TRACKING_ENABLED_IDS, Collections.EMPTY_LIST);
				final List<TrackingConfiguration> trackingList = fTrackingList;
				for (final TrackingConfiguration trackingConfig : trackingList) {
					if (trackingEnabledIds.contains(trackingConfig.getId())) {
						fTrackingEnabledSet.add(trackingConfig);
					}
				}
			}
			catch (final CoreException e) {
				logReadingError(e);
			}
		}
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, ((Boolean) fPinValue.getValue()).booleanValue());
		
		{	final Boolean enabled = (Boolean) fRHelpByStatetValue.getValue();
			configuration.setAttribute(ATTR_INTEGRATION_RHELP_ENABLED, enabled.booleanValue());
		}
		{	final Boolean enabled = (Boolean) fRGraphicsByStatetValue.getValue();
			configuration.setAttribute(ATTR_INTEGRATION_RGRAPHICS_ASDEFAULT, enabled.booleanValue());
		}
		{	final Boolean enabled = (Boolean) fRDbgExtValue.getValue();
			configuration.setAttribute(ATTR_INTEGRATION_RDBGEXT_ENABLED, enabled.booleanValue());
		}
		
		final String startupSnippet = (String) fStartupSnippetValue.getValue();
		if (startupSnippet != null && startupSnippet.length() > 0) {
			configuration.setAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET, startupSnippet);
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET);
		}
		
		{	final Boolean enabled = (Boolean) fObjectDBEnabledValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, enabled.booleanValue());
		}
		{	final Boolean enabled = (Boolean) fObjectDBAutoEnabledValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, enabled.booleanValue());
		}
		{	final Integer max = (Integer) fObjectDBListsChildrenValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, max.intValue());
		}
		{	final Integer max = (Integer) fObjectDBEnvsChildrenValue.getValue();
			configuration.setAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, max.intValue());
		}
		
		final List<String> trackingIds = new ArrayList<String>(fTrackingList.size());
		
		final List<TrackingConfiguration> trackingList = fTrackingList;
		for (final TrackingConfiguration trackingConfig : trackingList) {
			final String id = trackingConfig.getId();
			trackingIds.add(id);
			TRACKING_UTIL.save(trackingConfig, configuration);
		}
		configuration.setAttribute(TRACKING_IDS, trackingIds);
		
		final List<String> trackingEnabledIds = new ArrayList<String>(fTrackingEnabledSet.size());
		final Set<TrackingConfiguration> trackingEnabledSet = fTrackingEnabledSet;
		for (final TrackingConfiguration trackingConfig : trackingEnabledSet) {
			final String id = trackingConfig.getId();
			trackingEnabledIds.add(id);
		}
		configuration.setAttribute(TRACKING_ENABLED_IDS, trackingEnabledIds);
	}
	
}
