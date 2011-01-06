/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import static de.walware.statet.r.internal.debug.ui.launchconfigs.RRemoteConsoleLaunchDelegate.DEFAULT_SSH_PORT;
import static de.walware.statet.r.launching.RConsoleLaunching.REMOTE_RJS;
import static de.walware.statet.r.launching.RConsoleLaunching.REMOTE_RJS_RECONNECT;
import static de.walware.statet.r.launching.RConsoleLaunching.REMOTE_RJS_SSH;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.databinding.NumberValidator;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.ui.components.WidgetToolsButton;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.launching.RConsoleLaunching;


public class RRemoteConsoleMainTab extends RConsoleMainTab {
	
	
	private class UpdateJob extends Job implements IValueChangeListener {
		
		private String fUser;
		private String fAddress;
		private Integer fSshPort;
		
		public UpdateJob() {
			super("Background Update for RRemoteConsoleMainTab");
			setSystem(true);
			setPriority(SHORT);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final StringBuilder sb = new StringBuilder();
			sb.setLength(0);
			sb.append(fUser);
			sb.append('@');
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			try {
				final RMIAddress rmiAddress = new RMIAddress(fAddress);
				sb.append(rmiAddress.getHostAddress().getHostAddress());
			}
			catch (final Exception e) {}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			final Integer port = fSshPort;
			if (port != null && port.intValue() != DEFAULT_SSH_PORT) {
				sb.append(':');
				sb.append(port.toString());
			}
			final String s = sb.toString();
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			fSshAddressValue.getRealm().asyncExec(new Runnable() {
				public void run() {
					fSshAddressValue.setValue(s);
				}
			});
			return Status.OK_STATUS;
		}
		
		public void handleValueChange(final ValueChangeEvent event) {
			cancel();
			fUser = (String) fUserValue.getValue();
			fAddress = (String) fAddressValue.getValue();
			fSshPort = (Integer) fSshPortValue.getValue();
			schedule(100);
		}
		
	}
	
	
	private Text fAddressControl;
	private Control[] fAddressControls;
	private RRemoteConsoleSelectionDialog fRemoteEngineSelectionDialog;
	
	private Text fUsernameControl;
	private Control[] fLoginControls;
	private Label fUsernameInfo;
	
	private Text fSshPortControl;
	private Text fSshAddress;
	private Control[] fSshControls;
	
	private Text fCommandControl;
	private Control[] fCommandControls;
	
	private WritableValue fAddressValue;
	private WritableValue fUserValue;
	private WritableValue fSshPortValue;
	private WritableValue fCommandValue;
	private WritableValue fSshAddressValue;
	
	private UpdateJob fUpdateJob;
	
	
	public RRemoteConsoleMainTab() {
	}
	
	
	@Override
	protected RConsoleType[] loadTypes() {
		final List<RConsoleType> types = new ArrayList<RConsoleType>();
		types.add(new RConsoleType("RJ (RMI/JRI) - Manual", REMOTE_RJS, false, false)); //$NON-NLS-1$
		types.add(new RConsoleType("RJ (RMI/JRI) - Start over SSH", REMOTE_RJS_SSH, false, false)); 
		types.add(new RConsoleType("RJ (RMI/JRI) - Quick Reconnect", REMOTE_RJS_RECONNECT, false, false)); 
		return types.toArray(new RConsoleType[types.size()]);
	}
	
	@Override
	public void createControl(final Composite parent) {
		fUpdateJob = new UpdateJob();
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IRUIHelpContextIds.R_REMOTE_CONSOLE_LAUNCH);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (fUpdateJob != null) {
			fUpdateJob.cancel();
			fUpdateJob = null;
		}
	}
	
	@Override
	protected Composite createTypeDetailGroup(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));
		group.setText("Connection:");
		
		{	// Address:
			final Label label = new Label(group, SWT.NONE);
			label.setText("&Address: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Composite composite = new Composite(group, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fAddressControl = new Text(composite, SWT.LEFT | SWT.BORDER);
			fAddressControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			final Button addressButton = new Button(composite, SWT.PUSH);
			addressButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			addressButton.setText("Browse...");
			addressButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (fRemoteEngineSelectionDialog == null) {
						fRemoteEngineSelectionDialog = new RRemoteConsoleSelectionDialog(getShell(), false);
						fRemoteEngineSelectionDialog.setUser((String) fUserValue.getValue());
					}
					if (fRemoteEngineSelectionDialog.open() == Dialog.OK) {
						final Object result = fRemoteEngineSelectionDialog.getFirstResult();
						fAddressValue.setValue(result);
					}
				}
			});
			
			fAddressControls = new Control[] {
					label, composite,
			};
		}
		{	// Login:
			final Label label = new Label(group, SWT.NONE);
			label.setText("&Username: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fUsernameControl = new Text(group, SWT.LEFT | SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint = gd.minimumWidth = LayoutUtil.hintWidth(fUsernameControl, 25);
			fUsernameControl.setLayoutData(gd);
			
			fUsernameInfo = new Label(group, SWT.LEFT);
			fUsernameInfo.setText("");
			fUsernameInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			fLoginControls = new Control[] {
					label, fUsernameControl, fUsernameInfo,
			};
		}
		
		{	// SSH:
			final Label label = new Label(group, SWT.NONE);
			label.setText("&SSH Port: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fSshPortControl = new Text(group, SWT.LEFT | SWT.BORDER);
			final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(fSshPortControl, 6);
			fSshPortControl.setLayoutData(gd);
			
			final Composite addressComposite = new Composite(group, SWT.NONE);
			addressComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			addressComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Label addressLabel = new Label(addressComposite, SWT.LEFT);
			addressLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			addressLabel.setText("SSH Address:");
			fSshAddress = new Text(addressComposite, SWT.LEFT | SWT.BORDER);
			fSshAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fSshAddress.setEditable(false);
			
			fSshControls = new Control[] {
					label, fSshPortControl, addressLabel, fSshAddress,
			};
		}
		
		{	// Remote Command:
			final Label label = new Label(group, SWT.NONE);
			label.setText("Re&mote Command: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Composite composite = new Composite(group, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fCommandControl = new Text(composite, SWT.LEFT | SWT.BORDER);
			fCommandControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			final WidgetToolsButton toolsButton = new WidgetToolsButton(fCommandControl) {
				@Override
				protected void fillMenu(final Menu menu) {
					final MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Insert &Variable...");
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell()) {
								@Override
								public void setElements(final Object[] elements) {
									super.setElements(new IStringVariable[] {
											RRemoteConsoleLaunchDelegate.ADDRESS_VARIABLE,
											RRemoteConsoleLaunchDelegate.NAME_VARIABLE,
											RRemoteConsoleLaunchDelegate.WD_VARIABLE,
									});
								}
							};
							if (dialog.open() != Dialog.OK) {
								return;
							}
							final String variable = dialog.getVariableExpression();
							if (variable == null) {
								return;
							}
							fCommandControl.insert(variable);
							fCommandControl.setFocus();
						}
					});
				}
			};
			toolsButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			
			fCommandControls = new Control[] {
					label, fCommandControl, toolsButton,
			};
		}
		
		return group;
	}
	
	@Override
	protected void createFooter(final Composite composite) {
		final Link link = new Link(composite, SWT.NONE);
		link.setText("Global preferences: "
				+ "<a href=\"de.walware.statet.nico.preferencePages.ResourceMappings\">Folder Mapping</a>, "
				+ "<a href=\"org.eclipse.jsch.ui.SSHPreferences\">SSH2 Options (Key Management)</a>.");
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final PreferenceDialog dialog = org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn(null, e.text, null, null);
				if (dialog != null) {
					dialog.open();
				}
			}
		});
		super.createFooter(composite);
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		super.addBindings(dbc, realm);
		
		final MultiValidator validator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				// Calculate the validation status
				if (!getType().getId().equals(REMOTE_RJS_RECONNECT)) {
					final String text = (String) fAddressValue.getValue();
					if (text == null || text.length() == 0) {
						return ValidationStatus.error("Missing address for R remote engine ('//host[:port]/rsessionname').");
					}
					try {
						RMIAddress.validate(text);
					}
					catch (final MalformedURLException e) {
						return ValidationStatus.error("Invalid address for R remote engine: "+e.getLocalizedMessage());
					}
				}
				return ValidationStatus.ok();
			}
		};
		
		final WritableValue addressValue1 = new WritableValue("", String.class); //$NON-NLS-1$
		dbc.bindValue(SWTObservables.observeText(fAddressControl, SWT.Modify),
				addressValue1, null, null);
		validator.observeValidatedValue(addressValue1);
		fAddressValue = new WritableValue("", String.class); 
		dbc.bindValue(addressValue1, fAddressValue, null, null);
		
		fUserValue = new WritableValue("", String.class); 
		dbc.bindValue(SWTObservables.observeText(fUsernameControl, SWT.Modify),
				fUserValue, null, null);
		
		fSshPortValue = new WritableValue(null, Integer.class); 
		dbc.bindValue(SWTObservables.observeText(fSshPortControl, SWT.Modify),
				fSshPortValue,
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(0, 65535, true,
						"Invalid SSH port number specified (0-65535).")), null);
		
		fCommandValue = new WritableValue("", String.class); 
		dbc.bindValue(SWTObservables.observeText(fCommandControl, SWT.Modify),
				fCommandValue, null, null);
		
		fSshAddressValue = new WritableValue();
		dbc.bindValue(SWTObservables.observeText(fSshAddress, SWT.Modify),
				fSshAddressValue, null, null);
		
		fAddressValue.addValueChangeListener(fUpdateJob);
		fUserValue.addValueChangeListener(fUpdateJob);
		fSshPortValue.addValueChangeListener(fUpdateJob);
		
		dbc.addValidationStatusProvider(validator);
		validator.observeValidatedValue(getTypeValue());
	}
	
	@Override
	protected void updateType(final RConsoleType type) {
		if (REMOTE_RJS.equals(type.getId())) {
			DialogUtil.setEnabled(fAddressControls, null, true);
			DialogUtil.setEnabled(getArgumentComposite(), null, true);
			DialogUtil.setEnabled(fLoginControls, null, true);
			fUsernameInfo.setText("(optional)");
			DialogUtil.setVisible(fSshControls, null, false);
			DialogUtil.setVisible(fCommandControls, null, false);
		}
		else if (REMOTE_RJS_SSH.equals(type.getId())) {
			DialogUtil.setEnabled(fAddressControls, null, true);
			DialogUtil.setEnabled(getArgumentComposite(), null, true);
			DialogUtil.setEnabled(fLoginControls, null, true);
			fUsernameInfo.setText("(required)");
			DialogUtil.setVisible(fSshControls, null, true);
			DialogUtil.setVisible(fCommandControls, null, true);
			
			UIAccess.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (fUserValue.getValue() == null || ((String) fUserValue.getValue()).length() == 0) {
						fUserValue.setValue(System.getProperty("user.name")); 
					}
					if (fSshPortValue.getValue() == null) {
						fSshPortValue.setValue(22);
					}
					if (fCommandValue.getValue() == null || ((String) fCommandValue.getValue()).length() == 0) {
						fCommandValue.setValue(RRemoteConsoleLaunchDelegate.DEFAULT_COMMAND);
					}
				}
			});
		}
		else {
			DialogUtil.setEnabled(fAddressControls, null, false);
			DialogUtil.setEnabled(getArgumentComposite(), null, false);
			DialogUtil.setEnabled(fLoginControls, null, true);
			fUsernameInfo.setText("");
			DialogUtil.setVisible(fSshControls, null, false);
			DialogUtil.setVisible(fCommandControls, null, false);
		}
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(RConsoleLaunching.ATTR_ADDRESS, "//host/rsessionname"); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		super.doInitialize(configuration);
		
		String address;
		try {
			address = configuration.getAttribute(RConsoleLaunching.ATTR_ADDRESS, ""); 
		}
		catch (final CoreException e) {
			address = ""; 
			logReadingError(e);
		}
		fAddressValue.setValue(address);
		
		String user;
		try {
			user = configuration.getAttribute(RConsoleLaunching.ATTR_LOGIN_NAME, ""); 
		}
		catch (final CoreException e) {
			user = ""; 
			logReadingError(e);
		}
		fUserValue.setValue(user);
		
		int port;
		try {
			port = configuration.getAttribute(RConsoleLaunching.ATTR_SSH_PORT, DEFAULT_SSH_PORT); 
		}
		catch (final CoreException e) {
			port = DEFAULT_SSH_PORT;
			logReadingError(e);
		}
		fSshPortValue.setValue(port);
		
		String command;
		try {
			command = configuration.getAttribute(RConsoleLaunching.ATTR_COMMAND, ""); 
		}
		catch (final CoreException e) {
			command = ""; 
			logReadingError(e);
		}
		fCommandValue.setValue(command);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		super.doSave(configuration);
		final boolean isSSH = getType().getId().equals(REMOTE_RJS_SSH);
		
		if (fAddressControl.isEnabled()) {
			configuration.setAttribute(RConsoleLaunching.ATTR_ADDRESS, (String) fAddressValue.getValue());
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_ADDRESS);
		}
		
		final String user = (String) fUserValue.getValue();
		if (user != null && user.length() > 0) {
			configuration.setAttribute(RConsoleLaunching.ATTR_LOGIN_NAME, user);
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_LOGIN_NAME);
		}
		
		final Integer port = (Integer) fSshPortValue.getValue();
		if (isSSH && port != null) {
			configuration.setAttribute(RConsoleLaunching.ATTR_SSH_PORT, port.intValue());
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_SSH_PORT);
		}
		
		final String command = (String) fCommandValue.getValue();
		if (isSSH && command != null) {
			configuration.setAttribute(RConsoleLaunching.ATTR_COMMAND, command);
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_COMMAND);
		}
	}
	
}
