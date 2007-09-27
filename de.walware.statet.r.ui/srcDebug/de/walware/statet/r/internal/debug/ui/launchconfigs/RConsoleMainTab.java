/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
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
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.debug.HelpRequestor;
import de.walware.statet.base.ui.debug.InputArgumentsComposite;
import de.walware.statet.base.ui.debug.LaunchConfigUtil;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.ui.RUI;


/**
 *
 */
public class RConsoleMainTab extends LaunchConfigTabWithDbc {
	
	private static final String ATTR_ROOT = "de.walware.statet.r.debug/RConsole/"; //$NON-NLS-1$
	public static final String ATTR_TYPE = ATTR_ROOT+"type"; //$NON-NLS-1$
	public static final String ATTR_OPTIONS = ATTR_ROOT+"arguments.options"; //$NON-NLS-1$

	
	private class ConsoleType {
		
		private String fName;
		private String fId;
		
		public ConsoleType(String name, String id) {
			fName = name;
			fId = id;
		}

		public String getName() {
			return fName;
		}

		public String getId() {
			return fId;
		}
	}
	
	private class RArgumentsComposite extends InputArgumentsComposite {

		public RArgumentsComposite(Composite parent) {
			super(parent);
		}
		
		@Override
		protected void fillMenu(Menu menu) {
			super.fillMenu(menu);
			
			if (fWithHelp) {
				fHelpItem = new MenuItem(menu, SWT.PUSH);
				fHelpItem.setText(RLaunchingMessages.RConsole_MainTab_RunHelp_label);
				fHelpItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						queryHelp();
						getTextControl().setFocus();
					}
				});
				checkHelp(fConfigCache);
			}
		}
	}
	
	private ConsoleType[] fTypes;
	private ConsoleType fSelectedType;
	
	private ComboViewer fTypesCombo;
	private RArgumentsComposite fArgumentsControl;
	
	private WritableValue fTypeValue;
	private WritableValue fArgumentsValue;
	
	boolean fWithHelp = false;
	private MenuItem fHelpItem;
	private ILaunchConfigurationTab fREnvTab;
	private ILaunchConfiguration fConfigCache;
	
	
	public RConsoleMainTab() {
		super();
		loadTypes();
	}
	
	private void loadTypes() {
		List<ConsoleType> types = new ArrayList<ConsoleType>();
		types.add(new ConsoleType("Rterm", "rterm")); //$NON-NLS-1$ //$NON-NLS-2$
		fTypes = types.toArray(new ConsoleType[types.size()]);
	}
	
	public String getName() {
		return RLaunchingMessages.RConsole_MainTab_name;
	}
	
	@Override
	public Image getImage() {
		return StatetImages.getImage(StatetImages.LAUNCHCONFIG_MAIN);
	}
	
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());
		
		Group group;
		group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		group.setText("Launch configuration:");
		createCommandControls(group);

		Label note = new Label(mainComposite, SWT.WRAP);
		note.setText(SharedMessages.Note_label + ": " + fArgumentsControl.getNoteText()); //$NON-NLS-1$
		note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));

		Dialog.applyDialogFont(parent);
		initBindings();
	}
		
	private void createCommandControls(Composite container) {
		for (ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof REnvTab) {
				fREnvTab = tab;
				break;
			}
		}
		fWithHelp = (fREnvTab != null) && (getLaunchConfigurationDialog() instanceof TrayDialog);

		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));

		Label label = new Label(container, SWT.LEFT);
		label.setText(RLaunchingMessages.RConsole_MainTab_LaunchType_label+':');
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		String[] names = new String[fTypes.length];
		for (int i = 0; i < fTypes.length; i++) {
			names[i] = fTypes[i].getName();
		}
		fTypesCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		fTypesCombo.setContentProvider(new ArrayContentProvider());
		fTypesCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				ConsoleType type = (ConsoleType) element;
				return type.getName();
			}
		});
		fTypesCombo.setInput(fTypes);
		fTypesCombo.getCombo().setVisibleItemCount(names.length);
		fTypesCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fTypesCombo.getControl().setEnabled(false);
		
		LayoutUtil.addSmallFiller(container, false);
		fArgumentsControl = new RArgumentsComposite(container);
		fArgumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}
	
	@Override
	protected void addBindings(DataBindingContext dbc, Realm realm) {
		fTypeValue = new WritableValue(realm, ConsoleType.class);
		fArgumentsValue = new WritableValue(realm, String.class);

		IObservableValue typeSelection = ViewersObservables.observeSingleSelection(fTypesCombo);
		dbc.bindValue(typeSelection, fTypeValue, null, null);
		
		dbc.bindValue(SWTObservables.observeText(fArgumentsControl.getTextControl(), SWT.Modify),
				fArgumentsValue, null, null);
	}
	
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_TYPE, fTypes[0].getId()); //s
		configuration.setAttribute(ATTR_OPTIONS, ""); //$NON-NLS-1$
	}

	@Override
	public void doInitialize(ILaunchConfiguration configuration) {
		fTypeValue.setValue(fTypes[0]);
		
		String options = null;
		try {
			options = configuration.getAttribute(ATTR_OPTIONS, ""); //$NON-NLS-1$
			
		} catch (CoreException e) {
			options = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fArgumentsValue.setValue(options);
		
		checkHelp(configuration);
	}
	
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		checkHelp(workingCopy);
		super.activated(workingCopy);
	}
	
	@Override
	public void doSave(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_TYPE, ((ConsoleType) fTypeValue.getValue()).getId());
		configuration.setAttribute(ATTR_OPTIONS, (String) fArgumentsValue.getValue());
	}

	private void checkHelp(ILaunchConfiguration configuration) {
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
			List<String> cmdLine = new ArrayList<String>();
			ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
			
			// r env
			REnvConfiguration renv = REnvTab.getREnv(fConfigCache);
			
			cmdLine.addAll(0, renv.getExecCommand(Exec.TERM));
			
			cmdLine.add("--help"); //$NON-NLS-1$
			HelpRequestor helper = new HelpRequestor(cmdLine, (TrayDialog) dialog);
			
			helper.getProcessBuilder().environment();
			Map<String, String> envp = helper.getProcessBuilder().environment();
			envp.putAll(renv.getEnvironmentsVariables());
			LaunchConfigUtil.configureEnvironment(fConfigCache, envp);

			dialog.run(true, true, helper);
			updateLaunchConfigurationDialog();
		}
		catch(CoreException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
					-1, RLaunchingMessages.RConsole_MainTab_error_CannotRunHelp_message, e),
					StatusManager.LOG | StatusManager.SHOW);
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
					-1, RLaunchingMessages.RConsole_MainTab_error_WhileRunningHelp_message, e.getTargetException()),
					StatusManager.LOG | StatusManager.SHOW);
		} catch (InterruptedException e) {
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
