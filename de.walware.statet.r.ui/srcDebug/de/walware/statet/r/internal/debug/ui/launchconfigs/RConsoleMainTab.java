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

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.debug.ui.HelpRequestor;
import de.walware.ecommons.debug.ui.InputArgumentsComposite;
import de.walware.ecommons.debug.ui.LaunchConfigTabWithDbc;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.VariableFilter;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ChooseResourceComposite;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.ui.RUI;


/**
 * Main tab for R Console launch config.
 */
public class RConsoleMainTab extends LaunchConfigTabWithDbc {
	
	
	private class RArgumentsComposite extends InputArgumentsComposite {
		
		public RArgumentsComposite(final Composite parent) {
			super(parent);
		}
		
		@Override
		protected void fillMenu(final Menu menu) {
			super.fillMenu(menu);
			
			if (fWithHelp) {
				fHelpItem = new MenuItem(menu, SWT.PUSH);
				fHelpItem.setText(RLaunchingMessages.RConsole_MainTab_RunHelp_label);
				fHelpItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						queryHelp();
						getTextControl().setFocus();
					}
				});
				checkHelp(fConfigCache);
			}
		}
	}
	
	private final RConsoleType[] fTypes;
	private final RConsoleType fDefaultType;
	
	private ComboViewer fTypesCombo;
	
	private ChooseResourceComposite fWorkingDirectoryControl;
	private RArgumentsComposite fArgumentsControl;
	
	private WritableValue fTypeValue;
	private WritableValue fWorkingDirectoryValue;
	protected WritableValue fArgumentsValue;
	
	boolean fWithHelp = false;
	private MenuItem fHelpItem;
	private ILaunchConfigurationTab fREnvTab;
	private ILaunchConfiguration fConfigCache;
	
	
	public RConsoleMainTab() {
		super();
		fTypes = loadTypes();
		fDefaultType = fTypes[0];
	}
	
	
	protected RConsoleType[] loadTypes() {
		final List<RConsoleType> types = new ArrayList<RConsoleType>();
		types.add(new RConsoleType("RJ (RMI/JRI)", RConsoleLaunching.LOCAL_RJS, true, true)); //$NON-NLS-1$
		types.add(new RConsoleType("Rterm", RConsoleLaunching.LOCAL_RTERM, false, false)); //$NON-NLS-1$
		return types.toArray(new RConsoleType[types.size()]);
	}
	
	RConsoleType getSelectedType() {
		if (fTypeValue != null) {
			return (RConsoleType) fTypeValue.getValue();
		}
		return null;
	}
	
	public String getName() {
		return RLaunchingMessages.RConsole_MainTab_name;
	}
	
	@Override
	public Image getImage() {
		return StatetImages.getImage(StatetImages.LAUNCHCONFIG_MAIN);
	}
	
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		if (getLaunchConfigurationDialog().getMode().equals(ILaunchManager.DEBUG_MODE)) {
			final Label label = new Label(mainComposite, SWT.WRAP);
			label.setText("The 'debug' launch mode enables debug features for Java, not for R, and only if you use type 'RJ'."); //$NON-NLS-1$
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.widthHint = 300;
			label.setLayoutData(gd);
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
			LayoutUtil.addSmallFiller(mainComposite, false);
		}
		
		{	// Type
			final Composite composite = new Composite(mainComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Label label = new Label(composite, SWT.LEFT);
			label.setText(RLaunchingMessages.RConsole_MainTab_LaunchType_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final String[] names = new String[fTypes.length];
			for (int i = 0; i < fTypes.length; i++) {
				names[i] = fTypes[i].getName();
			}
			fTypesCombo = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			fTypesCombo.setContentProvider(new ArrayContentProvider());
			fTypesCombo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(final Object element) {
					final RConsoleType type = (RConsoleType) element;
					return type.getName();
				}
			});
			fTypesCombo.setInput(fTypes);
			fTypesCombo.getCombo().setVisibleItemCount(names.length);
			fTypesCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fTypesCombo.getControl().setEnabled(fTypes.length > 1);
		}
		
		final Composite detailGroup = createTypeDetailGroup(mainComposite);
		if (detailGroup != null) {
			detailGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		final Composite commandGroup = createROptionsGroup(mainComposite);
		commandGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		
		LayoutUtil.addSmallFiller(mainComposite, true);
		createFooter(mainComposite);
		
		Dialog.applyDialogFont(parent);
		initBindings();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IRUIHelpContextIds.R_CONSOLE_LAUNCH);
	}
	
	private Composite createROptionsGroup(final Composite parent) {
		for (final ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof REnvTab) {
				fREnvTab = tab;
				break;
			}
		}
		fWithHelp = (fREnvTab != null) && (getLaunchConfigurationDialog() instanceof TrayDialog);
		
		final Group group = new Group(parent, SWT.NONE);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));
		group.setText("R options:");
		
		fWorkingDirectoryControl = new ChooseResourceComposite(group,
				ChooseResourceComposite.STYLE_LABEL | ChooseResourceComposite.STYLE_TEXT,
				ChooseResourceComposite.MODE_DIRECTORY | ChooseResourceComposite.MODE_OPEN,
				RLaunchingMessages.REnv_Tab_WorkingDir_label);
		fWorkingDirectoryControl.showInsertVariable(true, new VariableFilter[] {
				VariableFilter.EXCLUDE_JAVA_FILTER,
				VariableFilter.EXCLUDE_BUILD_FILTER,
		});
		fWorkingDirectoryControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		fArgumentsControl = new RArgumentsComposite(group);
		fArgumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		return group;
	}
	
	protected Composite createTypeDetailGroup(final Composite parent) {
		return null;
	}
	
	protected Composite getArgumentComposite() {
		return fArgumentsControl;
	}
	
	protected void createFooter(final Composite composite) {
		final Label note = new Label(composite, SWT.WRAP);
		note.setText(SharedMessages.Note_label + ": " + fArgumentsControl.getNoteText()); //$NON-NLS-1$
		note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	}
	
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fTypeValue = new WritableValue(realm, RConsoleType.class);
		fWorkingDirectoryValue = new WritableValue(realm, null, String.class);
		fArgumentsValue = new WritableValue(realm, String.class);
		
		final IObservableValue typeSelection = ViewersObservables.observeSingleSelection(fTypesCombo);
		dbc.bindValue(typeSelection, fTypeValue, null, null);
		
		dbc.bindValue(SWTObservables.observeText(fArgumentsControl.getTextControl(), SWT.Modify),
				fArgumentsValue, null, null);
		
		fWorkingDirectoryControl.getValidator().setOnEmpty(IStatus.OK);
		dbc.bindValue(fWorkingDirectoryControl.createObservable(), fWorkingDirectoryValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new SavableErrorValidator(fWorkingDirectoryControl.getValidator())),
				null);
		
		fTypeValue.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final Object newValue = event.diff.getNewValue();
				updateType((RConsoleType) newValue);
			}
		});
	}
	
	public RConsoleType getType() {
		return (RConsoleType) fTypeValue.getValue();
	}
	
	protected IObservableValue getTypeValue() {
		return fTypeValue;
	}
	
	/**
	 * @param typeId
	 */
	protected void updateType(final RConsoleType type) {
	}
	
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConsoleLaunching.ATTR_TYPE, fDefaultType.getId()); //s
		configuration.setAttribute(RConsoleLaunching.ATTR_OPTIONS, ""); //$NON-NLS-1$
		configuration.setAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false); 
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		String type = null;
		try {
			type = configuration.getAttribute(RConsoleLaunching.ATTR_TYPE, ""); //$NON-NLS-1$
			// convert old rterm to new rj
			if (type.equals("rterm")) { //$NON-NLS-1$
				type = RConsoleLaunching.LOCAL_RTERM;
			}
		} catch (final CoreException e) {
			type = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		int i = 0;
		for (; i < fTypes.length; i++) {
			if (fTypes[i].getId().equals(type)) {
				fTypeValue.setValue(fTypes[i]);
				break;
			}
		}
		if (i >= fTypes.length) {
			fTypeValue.setValue(fDefaultType);
		}
		
		String wd = null;
		try {
			wd = configuration.getAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			wd = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fWorkingDirectoryValue.setValue(wd);
		
		String options = null;
		try {
			options = configuration.getAttribute(RConsoleLaunching.ATTR_OPTIONS, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
			options = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fArgumentsValue.setValue(options);
		
		checkHelp(configuration);
	}
	
	@Override
	public void activated(final ILaunchConfigurationWorkingCopy workingCopy) {
		checkHelp(workingCopy);
		super.activated(workingCopy);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConsoleLaunching.ATTR_TYPE, ((RConsoleType) fTypeValue.getValue()).getId());
		if (fArgumentsControl.isEnabled()) {
			configuration.setAttribute(RConsoleLaunching.ATTR_OPTIONS, (String) fArgumentsValue.getValue());
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_OPTIONS);
		}
		
		final String wd = (String) fWorkingDirectoryValue.getValue();
		if (wd != null && wd.length() > 0) {
			configuration.setAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, wd);
		}
		else {
			configuration.removeAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY);
		}
	}
	
	private void checkHelp(final ILaunchConfiguration configuration) {
		fConfigCache = configuration;
		if (fWithHelp && fHelpItem != null) {
			fHelpItem.setEnabled(fREnvTab.isValid(fConfigCache));
		}
	}
	
	private void queryHelp() {
		if (!fWithHelp) {
			return;
		}
		try {
			final List<String> cmdLine = new ArrayList<String>();
			final ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
			
			// r env
			final REnvConfiguration renv = REnvTab.getREnv(fConfigCache);
			
			cmdLine.addAll(0, renv.getExecCommand(Exec.TERM));
			
			cmdLine.add("--help"); //$NON-NLS-1$
			final ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
			final HelpRequestor helper = new HelpRequestor(processBuilder, (TrayDialog) dialog);
			
			final Map<String, String> envp = processBuilder.environment();
			LaunchConfigUtil.configureEnvironment(envp, fConfigCache, renv.getEnvironmentsVariables());
			
			dialog.run(true, true, helper);
			updateLaunchConfigurationDialog();
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					-1, RLaunchingMessages.RConsole_MainTab_error_CannotRunHelp_message, e),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					-1, RLaunchingMessages.RConsole_MainTab_error_WhileRunningHelp_message, e.getTargetException()),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	@Override
	public void dispose() {
		if (fWithHelp) {
			HelpRequestor.closeHelpTray((TrayDialog) getLaunchConfigurationDialog());
		}
		super.dispose();
	}
	
}
