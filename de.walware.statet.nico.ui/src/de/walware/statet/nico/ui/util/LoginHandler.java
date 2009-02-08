/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;

import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;


/**
 * Default login handler prompting dialog for user input.
 */
public class LoginHandler implements IToolEventHandler {
	
	
	private class LoginDialog extends MessageDialog {
		
		private Text fNameField;
		private Text fPassField;
		private LoginEventData fData;
		private ToolProcess fProcess;
		
		public LoginDialog(final Shell shell) {
			super(shell,
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
			inputComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			Label label;
			label = new Label(inputComposite, SWT.LEFT);
			label.setText(Messages.Login_Dialog_Name_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			fNameField = new Text(inputComposite, SWT.LEFT | SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fNameField, 25);
			fNameField.setLayoutData(gd);
			fNameField.setText(fData.name);
			
			label = new Label(inputComposite, SWT.LEFT);
			label.setText(Messages.Login_Dialog_Password_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			fPassField = new Text(inputComposite, SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
			fPassField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fPassField.setText(fData.password);
			
			return parent;
		}
		
		@Override
		protected Control createCustomArea(final Composite parent) {
			LayoutUtil.addSmallFiller(parent, true);
			
			final ToolInfoGroup info = new ToolInfoGroup(parent, fProcess);
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			applyDialogFont(parent);
			return parent;
		}
		
		@Override
		protected void okPressed() {
			fData.name = fNameField.getText();
			fData.password = fPassField.getText();
			super.okPressed();
		}
		
	}
	
	
	public int handle(final IToolRunnableControllerAdapter tools, final Object contextData) {
		final LoginEventData loginData = (LoginEventData) contextData;
		final AtomicInteger result = new AtomicInteger(CANCEL);
		final ToolProcess process = tools.getController().getProcess();
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				final IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
				final LoginDialog dialog = new LoginDialog(window.getShell());
				dialog.fData = loginData;
				dialog.fProcess = process;
				if (dialog.open() == Dialog.OK) {
					result.set(OK);
				}
			}
		});
		return result.get();
	}
	
}
