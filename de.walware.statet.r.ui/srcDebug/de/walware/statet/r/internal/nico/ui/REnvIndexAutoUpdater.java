/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.nico.ui;

import static de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants.AUTO;
import static de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants.DISABLED;
import static de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants.PREF_RENV_CHECK_UPDATE;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.util.ToolMessageDialog;

import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.nico.IRDataAdapter;
import de.walware.statet.r.nico.RJREnvIndexChecker;
import de.walware.statet.r.nico.RJREnvIndexUpdater;
import de.walware.statet.r.nico.RProcess;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.ui.RUI;


public class REnvIndexAutoUpdater {
	
	
	public static final class UpdateRunnable implements IToolRunnable {
		
		
		private final boolean fCompletely;
		
		
		public UpdateRunnable(final boolean completely) {
			fCompletely = completely;
		}
		
		public String getTypeId() {
			return "r/index/update"; //$NON-NLS-1$
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.TOOLS;
		}
		
		public String getLabel() {
			return RNicoMessages.REnvIndex_Update_task;
		}
		
		public void changed(final int event, final ToolProcess process) {
		}
		
		public void run(final IToolRunnableControllerAdapter adapter,
				final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			final IRDataAdapter r = (IRDataAdapter) adapter;
			IREnvConfiguration rEnvConfig = (IREnvConfiguration) r.getProcess().getAdapter(IREnvConfiguration.class);
			if (rEnvConfig != null) {
				rEnvConfig = rEnvConfig.getReference().getConfig();
				if (rEnvConfig != null) {
					final String remoteAddress = r.getWorkspaceData().getRemoteAddress();
					final Map<String, String> properties = new HashMap<String, String>();
					if (remoteAddress != null) {
						properties.put("renv.hostname", remoteAddress); //$NON-NLS-1$
					}
					
					r.handleStatus(new Status(IStatus.INFO, RUI.PLUGIN_ID, RNicoMessages.REnvIndex_Update_Started_message), monitor);
					final RJREnvIndexUpdater updater = new RJREnvIndexUpdater(rEnvConfig);
					IStatus status = updater.update(r, fCompletely, properties, monitor);
					r.handleStatus(status, monitor);
				}
			}
		}
	}
	
	
	private static class AskDialog extends ToolMessageDialog {
		
		
		private Button fRememberSessionControl;
		private Button fRememberGloballyControl;
		
		private boolean fRememberSession;
		private boolean fRememberGlobally;
		
		
		public AskDialog(final ToolProcess tool, final String message) {
			super(tool, null, RNicoMessages.REnvIndex_CheckDialog_title, null, message, QUESTION,
					new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
		}
		
		
		@Override
		protected Control createMessageArea(final Composite parent) {
			super.createMessageArea(parent);
			
			LayoutUtil.addGDDummy(parent);
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				label.setText(RNicoMessages.REnvIndex_CheckDialog_Remember_label);
			}
			{	final Button button = new Button(composite, SWT.CHECK);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
				gd.horizontalIndent = LayoutUtil.defaultIndent();
				button.setLayoutData(gd);
				button.setText(RNicoMessages.REnvIndex_CheckDialog_RememberSession_label);
				fRememberSessionControl = button;
			}
			{	final Button button = new Button(composite, SWT.CHECK);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
				gd.horizontalIndent = LayoutUtil.defaultIndent();
				button.setLayoutData(gd);
				button.setText(RNicoMessages.REnvIndex_CheckDialog_RememberGlobally_label);
				fRememberGloballyControl = button;
			}
			
			fRememberGloballyControl.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent e) {
					if (fRememberGloballyControl.getSelection()) {
						fRememberSessionControl.setSelection(false);
					}
				}
				public void widgetDefaultSelected(final SelectionEvent e) {
				}
			});
			fRememberSessionControl.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent e) {
					if (fRememberSessionControl.getSelection()) {
						fRememberGloballyControl.setSelection(false);
					}
				}
				public void widgetDefaultSelected(final SelectionEvent e) {
				}
			});
			
			return composite;
		}
		
		
		@Override
		public boolean close() {
			fRememberGlobally = fRememberGloballyControl.getSelection();
			fRememberSession = fRememberSessionControl.getSelection();
			
			return super.close();
		}
		
	}
	
	
	private class CheckRunnable implements IToolRunnable {
		
		
		private String fSessionSetting;
		
		private boolean fRJMissing;
		
		
		public String getTypeId() {
			return "r/index/check"; //$NON-NLS-1$
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.OTHER;
		}
		
		public String getLabel() {
			return RNicoMessages.REnvIndex_Check_task;
		}
		
		public void changed(final int event, final ToolProcess process) {
		}
		
		public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			final RjsController r = (RjsController) adapter;
			if (r.isBusy() || !r.isDefaultPrompt() || r.getBriefedChanges() == 0) {
				return;
			}
			try {
				final String global = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_RENV_CHECK_UPDATE).intern();
				
				if (global == DISABLED
						|| (global == AUTO && fSessionSetting == DISABLED)
						|| fChecker == null) {
					return;
				}
				final int check = fChecker.check(r, monitor);
				final String message;
				
				if (fChecker.wasAlreadyReported()) {
					if (!fRJMissing) {
						return;
					}
				}
				switch (check) {
				case RJREnvIndexChecker.NOT_AVAILABLE:
				case RJREnvIndexChecker.UP_TO_DATE:
					return;
				case RJREnvIndexChecker.PACKAGES:
					message = NLS.bind(((fChecker.getNewPackageCount() + fChecker.getChangedPackageCount()) == 1) ?
									RNicoMessages.REnvIndex_Check_Changed_singular_message :
									RNicoMessages.REnvIndex_Check_Changed_plural_message,
							fChecker.getNewPackageCount(), fChecker.getChangedPackageCount());
					break;
				case RJREnvIndexChecker.COMPLETE:
					message = RNicoMessages.REnvIndex_Check_NoIndex_message;
					break;
				default:
					return;
				}
				
				if (!fChecker.isRJPackageInstalled()) {
					fRJMissing = true;
					return;
				}
				fRJMissing = false;
				
				if (global != AUTO && fSessionSetting == null) {
					final AtomicBoolean update = new AtomicBoolean();
					UIAccess.getDisplay().syncExec(new Runnable() {
						public void run() {
							final AskDialog dialog = new AskDialog(fProcess, message);
							update.set(dialog.open() == 0);
							if (dialog.fRememberGlobally) {
								PreferencesUtil.setPrefValue(new InstanceScope(),
										PREF_RENV_CHECK_UPDATE, update.get() ? AUTO : DISABLED);
							}
							else if (dialog.fRememberSession) {
								fSessionSetting = update.get() ? AUTO : DISABLED;
							}
						}
					});
					if (!update.get()) {
						return;
					}
				}
				
				// schedule update
				adapter.getController().submit(new UpdateRunnable(false));
			}
			catch (final CoreException e) {
				if (e.getStatus().getSeverity() == IStatus.CANCEL) {
					throw e;
				}
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						RNicoMessages.REnvIndex_Check_error_message, e ));
			}
		}
		
	}
	
	
	private final RProcess fProcess;
	
	private final RJREnvIndexChecker fChecker;
	
	
	public REnvIndexAutoUpdater(final RProcess process) {
		fProcess = process;
		final IREnvConfiguration rEnvConfig = (IREnvConfiguration) process.getAdapter(IREnvConfiguration.class);
		if (rEnvConfig != null) {
			fChecker = new RJREnvIndexChecker(rEnvConfig);
			fProcess.getQueue().addOnIdle(new CheckRunnable());
			return;
		}
		else {
			fChecker = null;
		}
	}
	
	
}
