/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.ui.tools;

import static de.walware.statet.r.launching.RRunDebugPreferenceConstants.ASK;
import static de.walware.statet.r.launching.RRunDebugPreferenceConstants.AUTO;
import static de.walware.statet.r.launching.RRunDebugPreferenceConstants.DISABLED;
import static de.walware.statet.r.launching.RRunDebugPreferenceConstants.PREF_RENV_CHECK_UPDATE;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ts.ISystemReadRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.util.ToolMessageDialog;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgChangeSet;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgManager.Event;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.rj.RJREnvIndexChecker;
import de.walware.statet.r.core.rhelp.rj.RJREnvIndexUpdater;
import de.walware.statet.r.internal.console.ui.Messages;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.nico.impl.RjsController;


public class REnvIndexAutoUpdater {
	
	
	public static void connect(final RProcess process, final IRPkgManager manager) {
		final IREnvConfiguration rEnvConfig = (IREnvConfiguration) process
				.getAdapter(IREnvConfiguration.class);
		if (rEnvConfig != null) {
			final CheckRunnable checker = new CheckRunnable(process, manager,
					new RJREnvIndexChecker(rEnvConfig) );
			process.getQueue().addOnIdle(checker, 1100);
			return;
		}
	}
	
	
	public static final class UpdateRunnable extends AbstractRDataRunnable {
		
		
		private final boolean fCompletely;
		
		
		public UpdateRunnable(final boolean completely) {
			super("r/index/update", Messages.REnvIndex_Update_task); //$NON-NLS-1$
			fCompletely = completely;
		}
		
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void run(final IRDataAdapter r,
				final IProgressMonitor monitor) throws CoreException {
			IREnvConfiguration rEnvConfig = (IREnvConfiguration) r.getTool().getAdapter(IREnvConfiguration.class);
			if (rEnvConfig != null) {
				rEnvConfig = rEnvConfig.getReference().getConfig();
				if (rEnvConfig != null) {
					final String remoteAddress = r.getWorkspaceData().getRemoteAddress();
					final Map<String, String> properties = new HashMap<>();
					if (remoteAddress != null) {
						properties.put("renv.hostname", remoteAddress); //$NON-NLS-1$
					}
					
					final IRPkgManager rPkgManager= RCore.getRPkgManager(rEnvConfig.getReference());
					rPkgManager.check(IRPkgManager.NONE, r, monitor);
					
					r.handleStatus(new Status(IStatus.INFO, RConsoleUIPlugin.PLUGIN_ID, -1,
							Messages.REnvIndex_Update_Started_message, null ), monitor);
					final RJREnvIndexUpdater updater = new RJREnvIndexUpdater(rEnvConfig);
					final IStatus status = updater.update(r, fCompletely, properties, monitor);
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
			super(tool, null, Messages.REnvIndex_CheckDialog_title, null, message, QUESTION,
					new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
		}
		
		
		@Override
		protected Control createMessageArea(final Composite parent) {
			super.createMessageArea(parent);
			
			LayoutUtil.addGDDummy(parent);
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.createCompositeGrid(1));
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				label.setText(Messages.REnvIndex_CheckDialog_Remember_label);
			}
			{	final Button button = new Button(composite, SWT.CHECK);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
				gd.horizontalIndent = LayoutUtil.defaultIndent();
				button.setLayoutData(gd);
				button.setText(Messages.REnvIndex_CheckDialog_RememberSession_label);
				fRememberSessionControl = button;
			}
			{	final Button button = new Button(composite, SWT.CHECK);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
				gd.horizontalIndent = LayoutUtil.defaultIndent();
				button.setLayoutData(gd);
				button.setText(Messages.REnvIndex_CheckDialog_RememberGlobally_label);
				fRememberGloballyControl = button;
			}
			
			fRememberGloballyControl.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (fRememberGloballyControl.getSelection()) {
						fRememberSessionControl.setSelection(false);
					}
				}
				@Override
				public void widgetDefaultSelected(final SelectionEvent e) {
				}
			});
			fRememberSessionControl.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (fRememberSessionControl.getSelection()) {
						fRememberGloballyControl.setSelection(false);
					}
				}
				@Override
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
	
	private static class CheckRunnable implements ISystemReadRunnable, IRPkgManager.Listener {
		
		
		private final RProcess fProcess;
		
		private final RJREnvIndexChecker fChecker;
		
		private final IRPkgManager fManager;
		private boolean fChanged;
		
		private String fSessionSetting;
		
		
		public CheckRunnable(final RProcess process, final IRPkgManager manager, final RJREnvIndexChecker checker) {
			fProcess = process;
			fManager = manager;
			fChecker = checker;
			fChanged = true;
			
			fManager.addListener(this);
		}
		
		
		@Override
		public String getTypeId() {
			return "r/index/check"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return Messages.REnvIndex_Check_task;
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool.isProvidingFeatureSet(RConsoleTool.R_DATA_FEATURESET_ID));
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			if (event == MOVING_FROM) {
				return false;
			}
			if (event == BEING_ABANDONED) {
				fManager.removeListener(this);
			}
			return true;
		}
		
		@Override
		public void handleChange(final Event event) {
			if ((event.pkgsChanged() & IRPkgManager.INSTALLED) != 0) {
				final IRPkgChangeSet changeSet= event.getInstalledPkgChangeSet();
				if (changeSet != null && !changeSet.getNames().isEmpty()) {
					fChanged = true;
				}
			}
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final RjsController r = (RjsController) service; // interface?
			if (r.isBusy() || !r.isDefaultPrompt() || !fChanged ) {
				return;
			}
			fChanged = false;
			try {
				final String global = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_RENV_CHECK_UPDATE).intern();
				
				if (global == DISABLED
						|| (global == ASK && fSessionSetting == DISABLED) ) {
					return;
				}
				final int check = fChecker.check(r, monitor);
				final String message;
				
				if (fChecker.wasAlreadyReported()) {
					return;
				}
				switch (check) {
				case RJREnvIndexChecker.NOT_AVAILABLE:
				case RJREnvIndexChecker.UP_TO_DATE:
					return;
				case RJREnvIndexChecker.PACKAGES:
					message = NLS.bind(((fChecker.getNewPackageCount() + fChecker.getChangedPackageCount()) == 1) ?
									Messages.REnvIndex_Check_Changed_singular_message :
									Messages.REnvIndex_Check_Changed_plural_message,
							fChecker.getNewPackageCount(), fChecker.getChangedPackageCount());
					break;
				case RJREnvIndexChecker.COMPLETE:
					message = Messages.REnvIndex_Check_NoIndex_message;
					break;
				default:
					return;
				}
				
				if (global != AUTO && fSessionSetting == null) {
					final AtomicBoolean update = new AtomicBoolean();
					UIAccess.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							final AskDialog dialog = new AskDialog(fProcess, message);
							update.set(dialog.open() == 0);
							if (dialog.fRememberGlobally) {
								PreferencesUtil.setPrefValue(InstanceScope.INSTANCE,
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
				service.getTool().getQueue().add(new UpdateRunnable(false));
			}
			catch (final CoreException e) {
				if (e.getStatus().getSeverity() == IStatus.CANCEL) {
					throw e;
				}
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID, -1,
						Messages.REnvIndex_Check_error_message, e ));
			}
		}
	}
	
	
}
