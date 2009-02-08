/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.debug.ui.HelpRequestor;
import de.walware.ecommons.debug.ui.InputArgumentsComposite;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
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
	
	private RConsoleType[] fTypes;
	private RConsoleType fDefaultType;
	
	private ComboViewer fTypesCombo;
	private RArgumentsComposite fArgumentsControl;
	private Button fPinControl;
	
	private WritableValue fTypeValue;
	private WritableValue fArgumentsValue;
	private WritableValue fPinValue;
	
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
			label.setText("The 'debug' launch mode enables debug features for Java, not for R, and only if you use 'JR' type."); //$NON-NLS-1$
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.widthHint = 300;
			label.setLayoutData(gd);
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
			LayoutUtil.addSmallFiller(mainComposite, false);
		}
		
		Group group;
		group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		group.setText("Launch configuration:");
		createCommandControls(group);
		
		group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(RLaunchingMessages.RConsole_MainTab_ConsoleOptions_label);
		createConsoleOptions(group);
		
		final Label note = new Label(mainComposite, SWT.WRAP);
		note.setText(SharedMessages.Note_label + ": " + fArgumentsControl.getNoteText()); //$NON-NLS-1$
		note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
		
		LayoutUtil.addSmallFiller(mainComposite, false);
		
		Dialog.applyDialogFont(parent);
		initBindings();
	}
		
	private void createCommandControls(final Composite container) {
		for (final ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof REnvTab) {
				fREnvTab = tab;
				break;
			}
		}
		fWithHelp = (fREnvTab != null) && (getLaunchConfigurationDialog() instanceof TrayDialog);
		
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));
		
		final Label label = new Label(container, SWT.LEFT);
		label.setText(RLaunchingMessages.RConsole_MainTab_LaunchType_label+':');
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		final String[] names = new String[fTypes.length];
		for (int i = 0; i < fTypes.length; i++) {
			names[i] = fTypes[i].getName();
		}
		fTypesCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
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
		LayoutUtil.addGDDummy(container);
		
		createTypeDetails(container);
		
		LayoutUtil.addSmallFiller(container, false);
		fArgumentsControl = new RArgumentsComposite(container);
		fArgumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}
	
	protected void createTypeDetails(final Composite container) {
	}
	
	private void createConsoleOptions(final Composite container) {
		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		fPinControl = new Button(container, SWT.CHECK);
		fPinControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fPinControl.setText(RLaunchingMessages.RConsole_MainTab_ConsoleOptions_Pin_label);
	}
	
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fTypeValue = new WritableValue(realm, RConsoleType.class);
		fArgumentsValue = new WritableValue(realm, String.class);
		fPinValue = new WritableValue(realm, Boolean.class);
		
		final IObservableValue typeSelection = ViewersObservables.observeSingleSelection(fTypesCombo);
		dbc.bindValue(typeSelection, fTypeValue, null, null);
		
		dbc.bindValue(SWTObservables.observeText(fArgumentsControl.getTextControl(), SWT.Modify),
				fArgumentsValue, null, null);
		
		dbc.bindValue(SWTObservables.observeSelection(fPinControl), fPinValue, null, null);
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
		
		String options = null;
		try {
			options = configuration.getAttribute(RConsoleLaunching.ATTR_OPTIONS, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
			options = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fArgumentsValue.setValue(options);
		
		boolean pin;
		try {
			pin = configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false);
		}
		catch (final CoreException e) {
			pin = false;
			logReadingError(e);
		}
		fPinValue.setValue(pin);
		
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
		configuration.setAttribute(RConsoleLaunching.ATTR_OPTIONS, (String) fArgumentsValue.getValue());
		configuration.setAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, ((Boolean) fPinValue.getValue()).booleanValue());
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
			final HelpRequestor helper = new HelpRequestor(cmdLine, (TrayDialog) dialog);
			
			helper.getProcessBuilder().environment();
			final Map<String, String> envp = helper.getProcessBuilder().environment();
			LaunchConfigUtil.configureEnvironment(envp, fConfigCache, renv.getEnvironmentsVariables());
			
			dialog.run(true, true, helper);
			updateLaunchConfigurationDialog();
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
					-1, RLaunchingMessages.RConsole_MainTab_error_CannotRunHelp_message, e),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
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
