/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_SSH_HOST_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_SSH_PORT_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_USERNAME_DATA_KEY;
import static de.walware.statet.r.console.ui.launching.RConsoleLaunching.REMOTE_RJS;
import static de.walware.statet.r.console.ui.launching.RConsoleLaunching.REMOTE_RJS_RECONNECT;
import static de.walware.statet.r.console.ui.launching.RConsoleLaunching.REMOTE_RJS_SSH;
import static de.walware.statet.r.internal.console.ui.launching.RRemoteConsoleLaunchDelegate.DEFAULT_SSH_PORT;

import java.net.MalformedURLException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.ui.components.WidgetToolsButton;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import com.jcraft.jsch.Session;

import de.walware.statet.r.console.ui.IRConsoleHelpContextIds;
import de.walware.statet.r.console.ui.launching.RConsoleLaunching;
import de.walware.statet.r.internal.console.ui.launching.RRemoteConsoleSelectionDialog.SpecialAddress;
import de.walware.statet.r.nico.impl.RjsUtil;


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
				@Override
				public void run() {
					fSshAddressValue.setValue(s);
				}
			});
			return Status.OK_STATUS;
		}
		
		@Override
		public void handleValueChange(final ValueChangeEvent event) {
			cancel();
			fUser = (String) fUserValue.getValue();
			fAddress = (String) fAddressValue.getValue();
			fSshPort = (Integer) fSshPortValue.getValue();
			schedule(100);
		}
		
	}
	
	
	private Text fAddressControl;
	private List<Control> fAddressControls;
	private RRemoteConsoleSelectionDialog fRemoteEngineSelectionDialog;
	
	private Text fUsernameControl;
	private List <Control> fLoginControls;
	private Label fUsernameInfo;
	
	private Text fSshPortControl;
	private Text fSshAddress;
	private Button fSshTunnelControl;
	private List<Control> fSshControls;
	
	private Text fCommandControl;
	private List<Control> fCommandControls;
	
	private WritableValue fAddressValue;
	private WritableValue fUserValue;
	private WritableValue fSshPortValue;
	private WritableValue fSshTunnelValue;
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IRConsoleHelpContextIds.R_REMOTE_CONSOLE_LAUNCH);
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
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 4));
		group.setText("Connection:");
		
		fAddressControls = new ArrayList<Control>(8);
		fLoginControls = new ArrayList<Control>(8);
		fSshControls = new ArrayList<Control>(8);
		fCommandControls = new ArrayList<Control>(8);
		
		{	// Address:
			final Label label = new Label(group, SWT.NONE);
			label.setText("&Address: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Composite composite = new Composite(group, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			fAddressControl = new Text(composite, SWT.LEFT | SWT.BORDER);
			fAddressControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			final Button addressButton = new Button(composite, SWT.PUSH);
			addressButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			addressButton.setText("Browse...");
			addressButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) {
					final boolean newDialog = (fRemoteEngineSelectionDialog == null);
					if (newDialog) {
						fRemoteEngineSelectionDialog = new RRemoteConsoleSelectionDialog(getShell(), false);
					}
					else {
						fRemoteEngineSelectionDialog.clearAdditionaAddress(true);
					}
					String userName = (String) fUserValue.getValue();
					if (userName != null && userName.isEmpty()) {
						userName = null;
					}
					final String text = fAddressControl.getText();
					if (text.length() > 0) {
						try {
							final RMIAddress rmiAddress = new RMIAddress(text);
							final StringBuilder sb = new StringBuilder();
							sb.append(rmiAddress.getHost());
							if (rmiAddress.getPortNum() != Registry.REGISTRY_PORT) {
								sb.append(':').append(rmiAddress.getPortNum());
							}
							final String address = sb.toString();
							fRemoteEngineSelectionDialog.addAdditionalAddress(address, null);
							if (newDialog) {
								fRemoteEngineSelectionDialog.setInitialAddress(address);
							}
							
							if (fSshTunnelControl.isEnabled() && fSshTunnelControl.getSelection()
									&& userName != null) {
								final Map<String, Object> loginData = new HashMap<String, Object>();
								loginData.put(LOGIN_SSH_HOST_DATA_KEY, rmiAddress.getHost());
								final Object sshPort = fSshPortValue.getValue();
								loginData.put(LOGIN_SSH_PORT_DATA_KEY, (sshPort instanceof Integer) ?
										((Integer) sshPort).intValue() : DEFAULT_SSH_PORT );
								loginData.put(LOGIN_USERNAME_DATA_KEY, userName);
								final SpecialAddress special = new SpecialAddress(
										rmiAddress.getHost(), "127.0.0.1", rmiAddress.getPortNum() ) {
									@Override
									public RMIClientSocketFactory getSocketFactory(
											final IProgressMonitor monitor) throws CoreException {
										final Session session = RjsUtil.getSession(loginData, monitor);
										return RjsUtil.createRMIOverSshClientSocketFactory(session);
									}
								};
								final String label = rmiAddress.getHost() + " through SSH tunnel";
								fRemoteEngineSelectionDialog.addAdditionalAddress(label, special);
								fRemoteEngineSelectionDialog.setInitialAddress(label);
							}
						}
						catch (final Exception e) {}
					}
					
					fRemoteEngineSelectionDialog.setUser(userName);
					
					if (fRemoteEngineSelectionDialog.open() == Dialog.OK) {
						final Object result = fRemoteEngineSelectionDialog.getFirstResult();
						fAddressValue.setValue(result);
					}
				}
			});
			
			fAddressControls.add(label);
			fAddressControls.add(composite);
		}
		{	// Username:
			{	final Label label = new Label(group, SWT.NONE);
				label.setText("&Username: ");
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				
				final Composite composite = new Composite(group, SWT.NONE);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
				
				fUsernameControl = new Text(composite, SWT.LEFT | SWT.BORDER);
				final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
				gd.widthHint = gd.minimumWidth = LayoutUtil.hintWidth(fUsernameControl, 20);
				fUsernameControl.setLayoutData(gd);
				
				fUsernameInfo = new Label(composite, SWT.LEFT);
				fUsernameInfo.setText("");
				fUsernameInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				
				fLoginControls.add(label);
				fLoginControls.add(fUsernameControl);
				fLoginControls.add(fUsernameInfo);
			}
			{	final Label label = new Label(group, SWT.NONE);
				label.setText("&SSH Port: ");
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				
				fSshPortControl = new Text(group, SWT.LEFT | SWT.BORDER);
				final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
				gd.widthHint = LayoutUtil.hintWidth(fSshPortControl, 6);
				fSshPortControl.setLayoutData(gd);
				
				fSshControls.add(label);
				fSshControls.add(fSshPortControl);
			}
		}
		{	// Ext SSH:
			{	final Label label = new Label(group, SWT.NONE);
				label.setText("SSH Options: ");
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				
				fSshTunnelControl = new Button(group, SWT.CHECK);
				fSshTunnelControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				fSshTunnelControl.setText("&Tunnel connections to R engine through SSH");
				
				fSshControls.add(label);
				fSshControls.add(fSshTunnelControl);
			}
			{	final Label label = new Label(group, SWT.LEFT);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText("SSH Address:");
				
				fSshAddress = new Text(group, SWT.LEFT | SWT.BORDER);
				fSshAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fSshAddress.setEditable(false);
				
				fSshControls.add(label);
				fSshControls.add(fSshAddress);
			}
		}
		
		{	// Remote Command:
			final Label label = new Label(group, SWT.NONE);
			label.setText("Re&mote Command: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Composite composite = new Composite(group, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
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
			
			fCommandControls.add(label);
			fCommandControls.add(fCommandControl);
			fCommandControls.add(toolsButton);
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
					if (text == null || text.isEmpty()) {
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
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(0, 65535, true,
						"Invalid SSH port number specified (0-65535)." )), null );
		fSshTunnelValue = new WritableValue(false, Boolean.TYPE);
		dbc.bindValue(SWTObservables.observeSelection(fSshTunnelControl),
				fSshTunnelValue, null, null );
		
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
			DialogUtil.setVisible(fSshControls, null, true);
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
				@Override
				public void run() {
					if (fUserValue.getValue() == null || ((String) fUserValue.getValue()).isEmpty()) {
						fUserValue.setValue(System.getProperty("user.name")); 
					}
					if (fSshPortValue.getValue() == null) {
						fSshPortValue.setValue(22);
					}
					if (fCommandValue.getValue() == null || ((String) fCommandValue.getValue()).isEmpty()) {
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
		
		boolean tunnel;
		try {
			tunnel = configuration.getAttribute(RConsoleLaunching.ATTR_SSH_TUNNEL_ENABLED, false); 
		}
		catch (final CoreException e) {
			tunnel = false;
			logReadingError(e);
		}
		fSshTunnelValue.setValue(tunnel);
		
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
		if (!getType().getId().equals(REMOTE_RJS_RECONNECT) && port != null) {
			configuration.setAttribute(RConsoleLaunching.ATTR_SSH_PORT, port.intValue());
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_SSH_PORT);
		}
		
		final Boolean tunnel = (Boolean) fSshTunnelValue.getValue();
		if (!getType().getId().equals(REMOTE_RJS_RECONNECT) && tunnel != null) {
			configuration.setAttribute(RConsoleLaunching.ATTR_SSH_TUNNEL_ENABLED, tunnel.booleanValue());
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_SSH_TUNNEL_ENABLED);
		}
		
		final String command = (String) fCommandValue.getValue();
		if (getType().getId().equals(REMOTE_RJS_SSH) && command != null) {
			configuration.setAttribute(RConsoleLaunching.ATTR_COMMAND, command);
		}
		else {
			configuration.removeAttribute(RConsoleLaunching.ATTR_COMMAND);
		}
	}
	
}
