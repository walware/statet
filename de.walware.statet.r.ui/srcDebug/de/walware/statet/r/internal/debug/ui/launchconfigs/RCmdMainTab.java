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

import static de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations.ATTR_R_CMD_COMMAND;
import static de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations.ATTR_R_CMD_OPTIONS;
import static de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations.ATTR_R_CMD_RESOURCE;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.AbstractSettingsModelObject;
import de.walware.ecommons.ConstList;
import de.walware.ecommons.debug.ui.HelpRequestor;
import de.walware.ecommons.debug.ui.InputArgumentsComposite;
import de.walware.ecommons.debug.ui.LaunchConfigTabWithDbc;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;
import de.walware.ecommons.variables.core.VariableFilter;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvConfiguration.Exec;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.ui.RUI;


/**
 * Main tab to configure R CMD tool launch configs.
 */
public class RCmdMainTab extends LaunchConfigTabWithDbc {
	
	public static final String NS = "de.walware.statet.r.debug/RCmd/"; //$NON-NLS-1$
	
	private static class Cmd extends AbstractSettingsModelObject {
		
		public final static int PACKAGE = 1;
		public final static int DOC = 2;
		public final static int OTHER = 3;
		public final static int CUSTOM = 4;
		
		private final String fName;
		private String fCommand;
		private int fType;
		
		public Cmd(final String name, final String command, final int type) {
			fName = name;
			fCommand = command;
			fType = type;
		}
		
		public String getName() {
			return fName;
		}
		
		public int getType() {
			return fType;
		}
		
		public void setCommand(final String command) {
			fCommand = command.trim();
		}
		
		public String getCommand() {
			return fCommand;
		}
		
	}
	
	
	private Cmd[] fCommands;
	private Cmd fCustomCommand;
	
	private ComboViewer fCmdCombo;
	private Text fCmdText;
	private Button fHelpButton;
	private InputArgumentsComposite fArgumentsControl;
	private ResourceInputComposite fResourceControl;
	
	private WritableValue fCmdValue;
	private WritableValue fArgumentsValue;
	private WritableValue fResourceValue;
	
	boolean fWithHelp = false;
	private ILaunchConfigurationTab fREnvTab;
	private ILaunchConfiguration fConfigCache;
	
	
	public RCmdMainTab() {
		super();
		createCommands();
	}
	
	private void createCommands() {
		final List<Cmd> commands = new ArrayList<Cmd>();
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdCheck_name, "CMD check", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdBuild_name, "CMD build", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdInstall_name, "CMD INSTALL", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRemove_name, "CMD REMOVE", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRdconv_name, "CMD Rdconv", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRd2dvi_name, "CMD Rd2dvi", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRd2txt_name, "CMD Rd2txt", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdSd2Rd_name, "CMD Sd2Rd", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRoxygen_name, "CMD roxygen", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdSweave_name, "CMD Sweave", Cmd.DOC)); //$NON-NLS-1$
		fCustomCommand = new Cmd(RLaunchingMessages.RCmd_CmdOther_name, "", Cmd.CUSTOM); //$NON-NLS-1$
		commands.add(fCustomCommand);
		
		fCommands = commands.toArray(new Cmd[commands.size()]);
		resetCommands();
	}
	
	private void resetCommands() {
		fCustomCommand.fCommand = "CMD "; //$NON-NLS-1$
	}
	
	public String getName() {
		return RLaunchingMessages.RCmd_MainTab_name;
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
		
		Group group;
		group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(RLaunchingMessages.RCmd_MainTab_Cmd_label);
		createCommandControls(group);
		
		final Label note = new Label(mainComposite, SWT.WRAP);
		note.setText(SharedMessages.Note_label + ": " + fArgumentsControl.getNoteText()); //$NON-NLS-1$
		note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
		
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
		
		final String[] names = new String[fCommands.length];
		for (int i = 0; i < fCommands.length; i++) {
			names[i] = fCommands[i].getName();
		}
		fCmdCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		fCmdCombo.setContentProvider(new ArrayContentProvider());
		fCmdCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				final Cmd cmd = (Cmd) element;
				return cmd.getName();
			}
		});
		fCmdCombo.setInput(fCommands);
		fCmdCombo.getCombo().setVisibleItemCount(names.length);
		fCmdCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		fCmdText = new Text(container, SWT.BORDER | SWT.SINGLE);
		fCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, fWithHelp ? 1 : 2, 1));
		
		if (fWithHelp) {
			fHelpButton = new Button(container, SWT.PUSH);
			fHelpButton.setText(RLaunchingMessages.RCmd_MainTab_RunHelp_label);
			fHelpButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					queryHelp();
				}
			});
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(fHelpButton);
			fHelpButton.setLayoutData(gd);
		}
		
		LayoutUtil.addSmallFiller(container, false);
		fArgumentsControl = new InputArgumentsComposite(container);
		fArgumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		fResourceControl = new ResourceInputComposite(container,
				ResourceInputComposite.STYLE_LABEL | ResourceInputComposite.STYLE_TEXT,
				ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN,
				""); //$NON-NLS-1$
		fResourceControl.setShowInsertVariable(true, new ConstList<VariableFilter>(
				VariableFilter.EXCLUDE_JAVA_FILTER ), null);
		fResourceControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fCmdValue = new WritableValue(realm, Cmd.class);
		fArgumentsValue = new WritableValue(realm, String.class);
		fResourceValue = new WritableValue(realm, String.class);
		
		final IObservableValue cmdSelection = ViewersObservables.observeSingleSelection(fCmdCombo);
		dbc.bindValue(cmdSelection, fCmdValue, null, null);
		final IValidator cmdValidator = new IValidator() {
			public IStatus validate(final Object value) {
				final String s = (String) value;
				if (s == null || s.trim().length() == 0) {
					return ValidationStatus.warning(RLaunchingMessages.RCmd_MainTab_error_MissingCMD_message);
				}
				return ValidationStatus.ok();
			}
		};
		dbc.bindValue(SWTObservables.observeText(fCmdText, SWT.Modify),
				BeansObservables.observeDetailValue(realm, cmdSelection, "command", String.class), //$NON-NLS-1$
				new UpdateValueStrategy().setAfterGetValidator(cmdValidator),
				new UpdateValueStrategy().setBeforeSetValidator(cmdValidator) );
		dbc.bindValue(SWTObservables.observeText(fArgumentsControl.getTextControl(), SWT.Modify),
				fArgumentsValue, null, null);
		
		fResourceControl.getValidator().setOnLateResolve(IStatus.WARNING);
		fResourceControl.getValidator().setOnEmpty(IStatus.OK);
		fResourceControl.getValidator().setIgnoreRelative(true);
		final Binding resourceBinding = dbc.bindValue(fResourceControl.getObservable(), fResourceValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new SavableErrorValidator(fResourceControl.getValidator())), null);
		cmdSelection.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final Cmd cmd = (Cmd) event.diff.getNewValue();
				if (cmd != null) {
					fCmdText.setEditable(cmd.getType() == Cmd.CUSTOM);
					String label;
					int mode = 0;
					switch (cmd.getType()) {
					case Cmd.PACKAGE:
						label = RLaunchingMessages.RCmd_Resource_Package_label;
						mode = ResourceInputComposite.MODE_DIRECTORY;
						break;
					case Cmd.DOC:
						label = RLaunchingMessages.RCmd_Resource_Doc_label;
						mode = ResourceInputComposite.MODE_FILE;
						break;
					default: // Cmd.OTHER, Cmd.CUSTOM
						label = RLaunchingMessages.RCmd_Resource_Other_label;
						mode = ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_DIRECTORY;
						break;
					}
					fResourceControl.setResourceLabel(label);
					fResourceControl.setMode(mode | ResourceInputComposite.MODE_OPEN);
					resourceBinding.validateTargetToModel();
				}
			} });
	}
	
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_R_CMD_COMMAND, fCommands[0].getCommand());
		configuration.setAttribute(ATTR_R_CMD_OPTIONS, ""); //$NON-NLS-1$
		configuration.setAttribute(ATTR_R_CMD_RESOURCE, "${resource_loc}"); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		resetCommands();
		Cmd cmd = null;
		try {
			final String command = configuration.getAttribute(ATTR_R_CMD_COMMAND, ""); //$NON-NLS-1$
			for (final Cmd candidate : fCommands) {
				if (candidate.getCommand().equals(command)) {
					cmd = candidate;
					break;
				}
			}
			if (cmd == null) {
				fCustomCommand.setCommand(command);
				cmd = fCustomCommand;
			}
		}
		catch (final CoreException e) {
			cmd = fCommands[0];
			logReadingError(e);
		}
		fCmdValue.setValue(cmd);
		
		String options = null;
		try {
			options = configuration.getAttribute(RLaunchConfigurations.ATTR_R_CMD_OPTIONS, ""); //$NON-NLS-1$
			
		}
		catch (final CoreException e) {
			options = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fArgumentsValue.setValue(options);
		
		String resource = null;
		try {
			resource = configuration.getAttribute(ATTR_R_CMD_RESOURCE, ""); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			resource = ""; //$NON-NLS-1$
		}
		fResourceValue.setValue(resource);
		
		checkHelp(configuration);
	}
	
	@Override
	public void activated(final ILaunchConfigurationWorkingCopy workingCopy) {
		checkHelp(workingCopy);
		super.activated(workingCopy);
	}
	
	private void checkHelp(final ILaunchConfiguration configuration) {
		fConfigCache = configuration;
		if (fWithHelp) {
			fHelpButton.setEnabled(fREnvTab.isValid(fConfigCache));
		}
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_R_CMD_COMMAND, ((Cmd) fCmdValue.getValue()).getCommand());
		configuration.setAttribute(ATTR_R_CMD_OPTIONS, (String) fArgumentsValue.getValue());
		configuration.setAttribute(RLaunchConfigurations.ATTR_R_CMD_RESOURCE, (String) fResourceValue.getValue());
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
			
			final String cmd = ((Cmd) fCmdValue.getValue()).getCommand().trim();
			if (cmd.length() != 0) {
				cmdLine.addAll(Arrays.asList(cmd.split(" "))); //$NON-NLS-1$
			}
			String arg1 = null;
			if (cmdLine.size() > 0) {
				arg1 = cmdLine.remove(0);
			}
			cmdLine.addAll(0, renv.getExecCommand(arg1, EnumSet.of(Exec.CMD)));
			
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
					-1, RLaunchingMessages.RCmd_MainTab_error_CannotRunHelp_message, e),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					-1, RLaunchingMessages.RCmd_MainTab_error_WhileRunningHelp_message, e.getTargetException()),
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
