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

package de.walware.statet.nico.ui.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.ToolEventHandlerUtil;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUI;


/**
 * Default login handler prompting dialog for user input.
 * 
 * Uses Equinox Security storage to save login data
 */
public class LoginHandler implements IToolEventHandler {
	
	
	private static final String SECURE_PREF_ROOT = "/statet/nico"; //$NON-NLS-1$
	private static final String SECURE_PREF_CHARSET = "UTF-8"; //$NON-NLS-1$
	private static final String SECURE_PREF_NAME_KEY = "name"; //$NON-NLS-1$
	private static final String SECURE_PREF_PASSWORD_KEY = "password"; //$NON-NLS-1$
	
	
	private static class LoginDialog extends ToolMessageDialog {
		
		private String fMessage;
		private Callback[] fCallbacks;
		
		private boolean fAllowSave;
		private Button fSaveControl;
		private boolean fSave;
		
		private String fUsername;
		
		private final List<Runnable> fOkRunners = new ArrayList<Runnable>();
		
		
		public LoginDialog(final ToolProcess process, final Shell shell) {
			super(process, shell,
					Messages.Login_Dialog_title, null,
					Messages.Login_Dialog_message, MessageDialog.QUESTION,
					new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}
		
		
		@Override
		protected Control createMessageArea(final Composite parent) {
			super.createMessageArea(parent);
			
			LayoutUtil.addGDDummy(parent);
			final Composite inputComposite = new Composite(parent, SWT.NONE);
			inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			inputComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
			
			if (fMessage != null) {
				final Label label = new Label(inputComposite, SWT.WRAP);
				label.setText(fMessage);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
				
				LayoutUtil.addSmallFiller(inputComposite, false);
			}
			
			ITER_CALLBACKS: for (final Callback callback : fCallbacks) {
				if (callback instanceof TextOutputCallback) {
					final TextOutputCallback outputCallback = (TextOutputCallback) callback;
					final Label icon = new Label(inputComposite, SWT.LEFT);
					switch (outputCallback.getMessageType()) {
					case TextOutputCallback.ERROR:
						icon.setImage(Display.getCurrent().getSystemImage(SWT.ICON_ERROR));
						break;
					case TextOutputCallback.WARNING:
						icon.setImage(Display.getCurrent().getSystemImage(SWT.ICON_WARNING));
						break;
					default:
						icon.setImage(Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION));
						break;
					}
					icon.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
					final Label label = new Label(inputComposite, SWT.WRAP);
					label.setText(outputCallback.getMessage());
					label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
					continue ITER_CALLBACKS;
				}
				if (callback instanceof NameCallback) {
					final NameCallback nameCallback = (NameCallback) callback;
					final Label label = new Label(inputComposite, SWT.LEFT);
					label.setText(nameCallback.getPrompt()+':');
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
					final Text field = new Text(inputComposite, SWT.LEFT | SWT.BORDER);
					final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
					gd.widthHint = LayoutUtil.hintWidth(field, 25);
					field.setLayoutData(gd);
					
					String init = nameCallback.getName();
					if (init == null || init.isEmpty()) {
						init = nameCallback.getDefaultName();
					}
					if (init != null) {
						field.setText(init);
					}
					
					fOkRunners.add(new Runnable() {
						@Override
						public void run() {
							if (fUsername == null) {
								fUsername = field.getText();
							}
							nameCallback.setName(field.getText());
						}
					});
					continue ITER_CALLBACKS;
				}
				if (callback instanceof PasswordCallback) {
					final PasswordCallback passwordCallback = (PasswordCallback) callback;
					final Label label = new Label(inputComposite, SWT.LEFT);
					label.setText(passwordCallback.getPrompt()+':');
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
					final Text field = new Text(inputComposite, SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
					final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
					gd.widthHint = LayoutUtil.hintWidth(field, 25);
					field.setLayoutData(gd);
					field.setTextLimit(50);
					
					fOkRunners.add(new Runnable() {
						@Override
						public void run() {
							passwordCallback.setPassword(field.getText().toCharArray());
						}
					});
					continue ITER_CALLBACKS;
				}
				if (callback instanceof TextInputCallback) {
					final TextInputCallback inputCallback = (TextInputCallback) callback;
					final Label label = new Label(inputComposite, SWT.LEFT);
					label.setText(inputCallback.getPrompt()+':');
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
					final Text field = new Text(inputComposite, SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
					final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
					gd.widthHint = LayoutUtil.hintWidth(field, 25);
					field.setLayoutData(gd);
					
					String init = inputCallback.getText();
					if (init == null || init.isEmpty()) {
						init = inputCallback.getDefaultText();
					}
					if (init != null) {
						field.setText(init);
					}
					
					fOkRunners.add(new Runnable() {
						@Override
						public void run() {
							inputCallback.setText(field.getText());
						}
					});
					continue ITER_CALLBACKS;
				}
			}
			
			if (fAllowSave) {
				LayoutUtil.addSmallFiller(inputComposite, false);
				
				fSaveControl = new Button(inputComposite, SWT.CHECK);
				fSaveControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				fSaveControl.setText(Messages.Login_Dialog_Save_label);
				fSaveControl.setSelection(false);
			}
			
			return parent;
		}
		
		@Override
		protected void buttonPressed(final int buttonId) {
			if (buttonId == Dialog.OK) {
				okPressed();
			}
			super.buttonPressed(buttonId);
		}
		
		@Override
		protected void okPressed() {
			if (fSaveControl != null) {
				fSave = fSaveControl.getSelection();
			}
			for (final Runnable runnable : fOkRunners) {
				runnable.run();
			}
			super.okPressed();
		}
		
	}
	
	
	@Override
	public IStatus handle(final String id, final IConsoleService tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		final boolean saveAllowed = ToolEventHandlerUtil.getCheckedData(data, "save.allowed", Boolean.TRUE); //$NON-NLS-1$
		final boolean saveActivated = ToolEventHandlerUtil.getCheckedData(data, "save.activated", Boolean.FALSE); //$NON-NLS-1$
		final Callback[] callbacks = ToolEventHandlerUtil.getCheckedData(data, LOGIN_CALLBACKS_DATA_KEY, Callback[].class, true); 
		
		ITER_CALLBACKS: for (final Callback callback : callbacks) {
			if (callback instanceof TextOutputCallback
					|| callback instanceof NameCallback
					|| callback instanceof PasswordCallback
					|| callback instanceof TextInputCallback) {
				continue ITER_CALLBACKS;
			}
			final IStatus status = new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					Messages.Login_error_UnsupportedOperation_message,
					new UnsupportedCallbackException(callback));
			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
			return status;
		}
		
		if (id.equals(IToolEventHandler.LOGIN_REQUEST_EVENT_ID)) {
			// count login tries
			final int attempt = ToolEventHandlerUtil.getCheckedData(data, "attempt", Integer.valueOf(1)); //$NON-NLS-1$
			data.put("attempt", attempt+1); //$NON-NLS-1$
			if (saveAllowed && attempt == 1) {
				if (readData(callbacks, getDataNode(tools.getTool(), data, false), data)) {
					return Status.OK_STATUS;
				}
			}
			final String message = ToolEventHandlerUtil.getCheckedData(data, LOGIN_MESSAGE_DATA_KEY, String.class, false); 
			if (callbacks.length == 0) {
				return Status.OK_STATUS;
			}
			final AtomicReference<IStatus> result = new AtomicReference<IStatus>(Status.CANCEL_STATUS);
			final ToolProcess process = tools.getTool();
			UIAccess.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					final IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
					final LoginDialog dialog = new LoginDialog(process, window.getShell());
					dialog.fMessage = message;
					dialog.fCallbacks = callbacks;
					dialog.fAllowSave = saveAllowed;
					dialog.fSave = saveActivated;
					if (dialog.open() == Dialog.OK) {
						data.put("save.activated", Boolean.valueOf(dialog.fAllowSave && dialog.fSave)); //$NON-NLS-1$
						data.put(LOGIN_USERNAME_DATA_KEY, dialog.fUsername);
						result.set(Status.OK_STATUS);
					}
					else {
						data.put("save.activated", null); //$NON-NLS-1$
					}
				}
			});
			
			return result.get();
		}
		if (id.equals(IToolEventHandler.LOGIN_OK_EVENT_ID)) {
			if (saveAllowed && saveActivated) {
				if (saveData(callbacks, getDataNode(tools.getTool(), data, true))) {
					return Status.OK_STATUS;
				}
			}
			return Status.OK_STATUS;
		}
		throw new UnsupportedOperationException();
	}
	
	
	private boolean readData(final Callback[] callbacks, final ISecurePreferences node, final Map<String, Object> data) {
		try {
			int nameCount = 0;
			int passwordCount = 0;
			boolean complete = true;
			final Charset charset = Charset.forName(SECURE_PREF_CHARSET);
			ITER_CALLBACKS: for (final Callback callback : callbacks) {
				if (callback instanceof TextOutputCallback) {
					continue ITER_CALLBACKS;
				}
				if (callback instanceof NameCallback) {
					final NameCallback nameCallback = (NameCallback) callback;
					String name = (node != null) ? node.get(SECURE_PREF_NAME_KEY + nameCount++, null) : null;
					if (name == null || name.isEmpty() || Boolean.TRUE.equals(data.get(LOGIN_USERNAME_FORCE_DATA_KEY))) {
						name = (String) data.get(LOGIN_USERNAME_DATA_KEY);
					}
					if (name != null && name.length() > 0) {
						nameCallback.setName(name);
						data.put(LOGIN_USERNAME_DATA_KEY, name);
					}
					else {
						complete = false;
					}
					continue ITER_CALLBACKS;
				}
				if (callback instanceof PasswordCallback) {
					final PasswordCallback passwordCallback = (PasswordCallback) callback;
					final byte[] array = (node != null) ? node.getByteArray(SECURE_PREF_PASSWORD_KEY + passwordCount++, null) : null;
					if (array != null) {
						final char[] password = charset.decode(ByteBuffer.wrap(array)).array();
						passwordCallback.setPassword(password);
						Arrays.fill(array, (byte) 0);
						Arrays.fill(password, (char) 0);
					}
					continue ITER_CALLBACKS;
				}
				if (callback instanceof TextInputCallback) {
	//				final TextInputCallback inputCallback = (TextInputCallback) callback;
					complete = false;
					continue ITER_CALLBACKS;
				}
			}
			return complete;
		}
		catch (final Exception e) {
			NicoUIPlugin.logError(-1, Messages.Login_Safe_error_Loading_message, e);
			return false;
		}
	}
	
	private boolean saveData(final Callback[] callbacks, final ISecurePreferences node) {
		if (node == null) {
			return false;
		}
		try {
			int nameCount = 0;
			int passwordCount = 0;
			boolean complete = true;
			final Charset charset = Charset.forName(SECURE_PREF_CHARSET); 
			ITER_CALLBACKS: for (final Callback callback : callbacks) {
				if (callback instanceof TextOutputCallback) {
					continue ITER_CALLBACKS;
				}
				if (callback instanceof NameCallback) {
					final NameCallback nameCallback = (NameCallback) callback;
					node.put(SECURE_PREF_NAME_KEY + nameCount++, nameCallback.getName(), true); 
					continue ITER_CALLBACKS;
				}
				if (callback instanceof PasswordCallback) {
					final PasswordCallback passwordCallback = (PasswordCallback) callback;
					final char[] password = passwordCallback.getPassword();
					final byte[] array = charset.encode(CharBuffer.wrap(password)).array();
					node.putByteArray(SECURE_PREF_PASSWORD_KEY + passwordCount++, array, true); 
					Arrays.fill(password, (char) 0);
					Arrays.fill(array, (byte) 0);
					continue ITER_CALLBACKS;
				}
				if (callback instanceof TextInputCallback) {
	//				final TextInputCallback inputCallback = (TextInputCallback) callback;
					complete = false;
					continue ITER_CALLBACKS;
				}
			}
			return true;
		}
		catch (final Exception e) {
			NicoUIPlugin.logError(-1, Messages.Login_Safe_error_Saving_message, e);
			return false;
		}
	}
	
	private ISecurePreferences getDataNode(final ToolProcess process, final Map<String, Object> data, final boolean create) {
		final String id = ToolEventHandlerUtil.getCheckedData(data, LOGIN_ADDRESS_DATA_KEY, String.class, false); 
		if (id == null) {
			return null;
		}
		final ISecurePreferences store = SecurePreferencesFactory.getDefault();
		if (store == null) {
			return null;
		}
		
		final String path = SECURE_PREF_ROOT + '/' +
				EncodingUtils.encodeSlashes(process.getMainType()) + '/' +
				EncodingUtils.encodeSlashes(id);
		if (!create && !store.nodeExists(path)) {
			return null;
		}
		return store.node(path);
	}
	
}
