/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.cmd.ui.launching;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
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
import org.eclipse.jface.databinding.swt.WidgetProperties;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.AbstractSettingsModelObject;
import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.databinding.core.util.UpdateableErrorValidator;
import de.walware.ecommons.debug.core.util.LaunchUtils;
import de.walware.ecommons.debug.ui.HelpRequestor;
import de.walware.ecommons.debug.ui.config.InputArgumentsComposite;
import de.walware.ecommons.debug.ui.config.LaunchConfigTabWithDbc;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvConfiguration.Exec;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.launching.ui.REnvTab;


/**
 * Main tab to configure R CMD tool launch configs.
 */
public class RCmdMainTab extends LaunchConfigTabWithDbc {
	
	public static final String NS= "de.walware.statet.r.debug/RCmd/"; //$NON-NLS-1$
	
	private static class Cmd extends AbstractSettingsModelObject {
		
		public final static int PACKAGE_DIR= 1;
		public final static int PACKAGE_DIR_OR_ARCHIVE= 2;
		public final static int DOC= 3;
		public final static int DOC_OR_DIR= 4;
		public final static int CUSTOM= 5;
		
		private final String name;
		private String command;
		private final int type;
		
		public Cmd(final String name, final String command, final int type) {
			this.name= name;
			this.command= command;
			this.type= type;
		}
		
		public String getName() {
			return this.name;
		}
		
		public int getType() {
			return this.type;
		}
		
		public void setCommand(final String command) {
			this.command= command.trim();
		}
		
		public String getCommand() {
			return this.command;
		}
		
	}
	
	
	private Cmd[] commands;
	private Cmd customCommand;
	
	private final WritableValue cmdValue;
	private final WritableValue argumentsValue;
	private final WritableValue resourceValue;
	
	private final WritableValue workingDirectoryValue;
	
	private ComboViewer cmdCombo;
	private Text cmdText;
	private Button helpButton;
	private InputArgumentsComposite argumentsControl;
	private ResourceInputComposite resourceControl;
	
	private ResourceInputComposite workingDirectoryControl;
	
	boolean withHelp= false;
	private ILaunchConfigurationTab rEnvTab;
	private ILaunchConfiguration configCache;
	
	
	public RCmdMainTab() {
		super();
		createCommands();
		
		final Realm realm= getRealm();
		this.cmdValue= new WritableValue(realm, null, Cmd.class);
		this.argumentsValue= new WritableValue(realm, null, String.class);
		this.resourceValue= new WritableValue(realm, null, String.class);
		this.workingDirectoryValue= new WritableValue(realm, null, String.class);
	}
	
	private void createCommands() {
		final List<Cmd> commands= new ArrayList<>();
		commands.add(new Cmd(Messages.RCmd_CmdCheck_name, "CMD check", Cmd.PACKAGE_DIR_OR_ARCHIVE)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdBuild_name, "CMD build", Cmd.PACKAGE_DIR)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdInstall_name, "CMD INSTALL", Cmd.PACKAGE_DIR_OR_ARCHIVE)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdRemove_name, "CMD REMOVE", Cmd.PACKAGE_DIR)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdRdconv_name, "CMD Rdconv", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdRd2dvi_name, "CMD Rd2dvi", Cmd.DOC_OR_DIR)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdRd2txt_name, "CMD Rd2txt", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdSd2Rd_name, "CMD Sd2Rd", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdRoxygen_name, "CMD roxygen", Cmd.PACKAGE_DIR)); //$NON-NLS-1$
		commands.add(new Cmd(Messages.RCmd_CmdSweave_name, "CMD Sweave", Cmd.DOC)); //$NON-NLS-1$
		this.customCommand= new Cmd(Messages.RCmd_CmdOther_name, "", Cmd.CUSTOM); //$NON-NLS-1$
		commands.add(this.customCommand);
		
		this.commands= commands.toArray(new Cmd[commands.size()]);
		resetCommands();
	}
	
	private void resetCommands() {
		this.customCommand.command= "CMD "; //$NON-NLS-1$
	}
	
	@Override
	public String getName() {
		return Messages.MainTab_name;
	}
	
	@Override
	public Image getImage() {
		return StatetImages.getImage(StatetImages.LAUNCHCONFIG_MAIN);
	}
	
	@Override
	public void createControl(final Composite parent) {
		final Composite mainComposite= new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayout(LayoutUtil.createTabGrid(1));
		
		Group group;
		group= new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.MainTab_Cmd_label);
		createCommandControls(group);
		
		{	final Composite composite= createWorkingDirectoryGroup(mainComposite);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		{	final Label note= new Label(mainComposite, SWT.WRAP);
			note.setText(SharedMessages.Note_label + ": " + this.argumentsControl.getNoteText()); //$NON-NLS-1$
			note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
		}
		
		Dialog.applyDialogFont(parent);
		
		initBindings();
	}
	
	private Composite createWorkingDirectoryGroup(final Composite parent) {
		final ResourceInputComposite control= new ResourceInputComposite(parent,
				ResourceInputComposite.STYLE_GROUP | ResourceInputComposite.STYLE_TEXT,
				ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
				Messages.MainTab_WorkingDir_label);
		control.setShowInsertVariable(true, 
				DialogUtil.DEFAULT_INTERACTIVE_FILTERS, null );
		this.workingDirectoryControl= control;
		
		return control;
	}
	
	private void createCommandControls(final Composite container) {
		for (final ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof REnvTab) {
				this.rEnvTab= tab;
				break;
			}
		}
		this.withHelp= (this.rEnvTab != null) && (getLaunchConfigurationDialog() instanceof TrayDialog);
		
		container.setLayout(LayoutUtil.createGroupGrid(3));
		
		final String[] names= new String[this.commands.length];
		for (int i= 0; i < this.commands.length; i++) {
			names[i]= this.commands[i].getName();
		}
		this.cmdCombo= new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.cmdCombo.setContentProvider(new ArrayContentProvider());
		this.cmdCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				final Cmd cmd= (Cmd) element;
				return cmd.getName();
			}
		});
		this.cmdCombo.setInput(this.commands);
		this.cmdCombo.getCombo().setVisibleItemCount(names.length);
		this.cmdCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		this.cmdText= new Text(container, SWT.BORDER | SWT.SINGLE);
		this.cmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, this.withHelp ? 1 : 2, 1));
		
		if (this.withHelp) {
			this.helpButton= new Button(container, SWT.PUSH);
			this.helpButton.setText(Messages.MainTab_RunHelp_label);
			this.helpButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					queryHelp();
				}
			});
			final GridData gd= new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint= LayoutUtil.hintWidth(this.helpButton);
			this.helpButton.setLayoutData(gd);
		}
		
		LayoutUtil.addSmallFiller(container, false);
		this.argumentsControl= new InputArgumentsComposite(container);
		this.argumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		this.resourceControl= new ResourceInputComposite(container,
				ResourceInputComposite.STYLE_LABEL | ResourceInputComposite.STYLE_TEXT,
				ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN,
				"" ); //$NON-NLS-1$
		this.resourceControl.setShowInsertVariable(true,
				ImCollections.newList(DialogUtil.EXCLUDE_JAVA_FILTER),
				null );
		this.resourceControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc) {
		final IObservableValue cmdSelection= ViewersObservables.observeSingleSelection(this.cmdCombo);
		dbc.bindValue(cmdSelection, this.cmdValue, null, null);
		final IValidator cmdValidator= new IValidator() {
			@Override
			public IStatus validate(final Object value) {
				final String s= (String) value;
				if (s == null || s.trim().isEmpty()) {
					return ValidationStatus.warning(Messages.MainTab_error_MissingCMD_message);
				}
				return ValidationStatus.ok();
			}
		};
		
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(this.cmdText),
				BeanProperties.value(Cmd.class, "command").observeDetail(cmdSelection), //$NON-NLS-1$
				new UpdateValueStrategy().setAfterGetValidator(cmdValidator),
				new UpdateValueStrategy().setBeforeSetValidator(cmdValidator) );
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(this.argumentsControl.getTextControl()),
				this.argumentsValue, null, null);
		
		this.resourceControl.getValidator().setOnLateResolve(IStatus.WARNING);
		this.resourceControl.getValidator().setOnEmpty(IStatus.OK);
		this.resourceControl.getValidator().setIgnoreRelative(true);
		final Binding resourceBinding= dbc.bindValue(this.resourceControl.getObservable(), this.resourceValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new UpdateableErrorValidator(this.resourceControl.getValidator())),
				null );
		cmdSelection.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final Cmd cmd= (Cmd) event.diff.getNewValue();
				if (cmd != null) {
					RCmdMainTab.this.cmdText.setEditable(cmd.getType() == Cmd.CUSTOM);
					String label;
					int mode= 0;
					switch (cmd.getType()) {
					case Cmd.PACKAGE_DIR:
						label= Messages.MainTab_Resource_PackageDir_label;
						mode= ResourceInputComposite.MODE_DIRECTORY;
						break;
					case Cmd.PACKAGE_DIR_OR_ARCHIVE:
						label= Messages.MainTab_Resource_PackageDirOrArchive_label;
						mode= ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_DIRECTORY;
						break;
					case Cmd.DOC:
						label= Messages.MainTab_Resource_Doc_label;
						mode= ResourceInputComposite.MODE_FILE;
						break;
					case Cmd.DOC_OR_DIR:
						label= Messages.MainTab_Resource_DocOrDir_label;
						mode= ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_DIRECTORY;
						break;
					default: // Cmd.CUSTOM:
						label= Messages.MainTab_Resource_Other_label;
						mode= ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_DIRECTORY;
						break;
					}
					RCmdMainTab.this.resourceControl.setResourceLabel(label);
					RCmdMainTab.this.resourceControl.setMode(mode | ResourceInputComposite.MODE_OPEN);
					resourceBinding.validateTargetToModel();
				}
			} });
		
		this.workingDirectoryControl.getValidator().setOnEmpty(IStatus.OK);
		dbc.bindValue(this.workingDirectoryControl.getObservable(), this.workingDirectoryValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new UpdateableErrorValidator(this.workingDirectoryControl.getValidator())),
				null );
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RCmdLaunching.R_CMD_COMMAND_ATTR_NAME, this.commands[0].getCommand());
		configuration.setAttribute(RCmdLaunching.R_CMD_OPTIONS_ATTR_NAME, ""); //$NON-NLS-1$
		configuration.setAttribute(RCmdLaunching.R_CMD_RESOURCE_ATTR_NAME, "${resource_loc}"); //$NON-NLS-1$
		REnvTab.setWorkingDirectory(configuration, ""); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		resetCommands();
		
		Cmd cmd= null;
		{	try {
				final String command= configuration.getAttribute(RCmdLaunching.R_CMD_COMMAND_ATTR_NAME, ""); //$NON-NLS-1$
				for (final Cmd candidate : this.commands) {
					if (candidate.getCommand().equals(command)) {
						cmd= candidate;
						break;
					}
				}
				if (cmd == null) {
					this.customCommand.setCommand(command);
					cmd= this.customCommand;
				}
			}
			catch (final CoreException e) {
				cmd= this.commands[0];
				logReadingError(e);
			}
			this.cmdValue.setValue(cmd);
		}
		{	String value;
			try {
				value= configuration.getAttribute(RCmdLaunching.R_CMD_OPTIONS_ATTR_NAME, ""); //$NON-NLS-1$
				
			}
			catch (final CoreException e) {
				value= ""; //$NON-NLS-1$
				logReadingError(e);
			}
			this.argumentsValue.setValue(value);
		}
		{	String value;
			try {
				value= configuration.getAttribute(RCmdLaunching.R_CMD_RESOURCE_ATTR_NAME, ""); //$NON-NLS-1$
			}
			catch (final CoreException e) {
				value= ""; //$NON-NLS-1$
			}
			this.resourceValue.setValue(value);
		}
		
		{	String value;
			try {
				value= REnvTab.readWorkingDirectory(configuration);
			}
			catch (final CoreException e) {
				value= ""; //$NON-NLS-1$
				logReadingError(e);
			}
			this.workingDirectoryValue.setValue(value);
		}
		
		checkHelp(configuration);
	}
	
	@Override
	public void activated(final ILaunchConfigurationWorkingCopy workingCopy) {
		checkHelp(workingCopy);
		super.activated(workingCopy);
	}
	
	private void checkHelp(final ILaunchConfiguration configuration) {
		this.configCache= configuration;
		if (this.withHelp) {
			this.helpButton.setEnabled(this.rEnvTab.isValid(this.configCache));
		}
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RCmdLaunching.R_CMD_COMMAND_ATTR_NAME, ((Cmd) this.cmdValue.getValue()).getCommand());
		configuration.setAttribute(RCmdLaunching.R_CMD_OPTIONS_ATTR_NAME, (String) this.argumentsValue.getValue());
		configuration.setAttribute(RCmdLaunching.R_CMD_RESOURCE_ATTR_NAME, (String) this.resourceValue.getValue());
		REnvTab.setWorkingDirectory(configuration, (String) workingDirectoryValue.getValue());
	}
	
	
	private void queryHelp() {
		if (!this.withHelp) {
			return;
		}
		try {
			final List<String> cmdLine= new ArrayList<>();
			final ILaunchConfigurationDialog dialog= getLaunchConfigurationDialog();
			
			// r env
			final IREnvConfiguration renv= RLaunching.getREnvConfig(this.configCache, true);
			
			final String cmd= ((Cmd) this.cmdValue.getValue()).getCommand().trim();
			if (cmd.length() != 0) {
				cmdLine.addAll(Arrays.asList(cmd.split(" "))); //$NON-NLS-1$
			}
			String arg1= null;
			if (cmdLine.size() > 0) {
				arg1= cmdLine.remove(0);
			}
			cmdLine.addAll(0, renv.getExecCommand(arg1, EnumSet.of(Exec.CMD)));
			
			cmdLine.add("--help"); //$NON-NLS-1$
			final ProcessBuilder processBuilder= new ProcessBuilder(cmdLine);
			final HelpRequestor helper= new HelpRequestor(processBuilder, (TrayDialog) dialog);
			
			final Map<String, String> envp= processBuilder.environment();
			LaunchUtils.configureEnvironment(envp, this.configCache, renv.getEnvironmentsVariables());
			
			dialog.run(true, true, helper);
			updateLaunchConfigurationDialog();
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID, -1,
					Messages.MainTab_error_CannotRunHelp_message, e ),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID, -1,
					Messages.MainTab_error_WhileRunningHelp_message, e.getTargetException()),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
		}
	}
	
	@Override
	public void dispose() {
		if (this.withHelp) {
			HelpRequestor.closeHelpTray((TrayDialog) getLaunchConfigurationDialog());
		}
		super.dispose();
	}
	
}
