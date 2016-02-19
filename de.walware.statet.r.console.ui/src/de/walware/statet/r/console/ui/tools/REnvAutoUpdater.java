/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.ui.tools;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.osgi.framework.Version;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.core.Preference.StringPref2;
import de.walware.ecommons.ts.ISystemReadRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.util.ToolMessageDialog;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.pkgmanager.IRPkgInfoAndData;
import de.walware.statet.r.core.pkgmanager.IRPkgList;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.console.ui.Messages;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.ui.pkgmanager.RPkgManagerUI;
import de.walware.statet.r.ui.pkgmanager.StartAction;


public class REnvAutoUpdater extends AbstractRDataRunnable implements ISystemReadRunnable {
	
	
	public static void connect(final RjsController controller, final IRPkgManager manager) {
		if (controller == null || manager == null) {
			return;
		}
		final REnvAutoUpdater checker = new REnvAutoUpdater(manager);
		controller.getTool().getQueue().addOnIdle(checker, 1000);
	}
	
	
	private final IRPkgManager fManager;
	
	private boolean fInitial = true;
	
	private boolean fVersionChecked;
	
	
	private REnvAutoUpdater(final IRPkgManager manager) {
		super("r/renv/check", Messages.REnvIndex_Check_task); //$NON-NLS-1$
		fManager = manager;
	}
	
	
	@Override
	public boolean changed(final int event, final ITool tool) {
		if (event == MOVING_FROM) {
			return false;
		}
		return true;
	}
	
	@Override
	protected void run(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		final AbstractRController rjs = (AbstractRController) r;
		if (rjs.isBusy() || !r.isDefaultPrompt() || r.getBriefedChanges() == 0) {
			return;
		}
		
		int flags = 0;
		if (fInitial) {
			fInitial = false;
			flags |= IRPkgManager.INITIAL;
		}
		fManager.check(flags, r, monitor);
		
		if (!fVersionChecked) {
			fVersionChecked = true;
			
			checkRVersion(r, monitor);
		}
		
	}
	
	private void checkRVersion(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		final Version rVersion = r.getPlatform().getRVersion();
		
		final IREnvConfiguration rConfig = fManager.getREnv().getConfig();
		if (rConfig == null) {
			return;
		}
		final StringPref2 pref= new StringPref2(
				IREnvManager.PREF_QUALIFIER + '/' + rConfig.getReference().getId(),
				"CheckedR.version" ); //$NON-NLS-1$
		final String s = PreferencesUtil.getInstancePrefs().getPreferenceValue(pref);
		if (s != null) {
			final Version checkedVersion = new Version(s);
			if (!checkedVersion.equals(rVersion)) {
				PreferencesUtil.setPrefValue(InstanceScope.INSTANCE, pref, rVersion.toString());
				if (checkedVersion.getMajor() != rVersion.getMajor()
						|| checkedVersion.getMinor() != rVersion.getMinor() ) {
					final IRPkgManager.Ext rPkgManager = (IRPkgManager.Ext) fManager;
					if (rPkgManager.requiresUpdate()) {
						rPkgManager.update(r, monitor);
					}
					if (hasNonBasePackages(rPkgManager.getExtRPkgSet())) {
						handleNewVersion(rConfig, r.getTool(), rPkgManager,
								checkedVersion, rVersion );
					}
				}
			}
		}
		else {
			PreferencesUtil.setPrefValue(InstanceScope.INSTANCE, pref, rVersion.toString());
		}
	}
	
	private static String mainVersionString(final Version version) {
		final StringBuilder sb = new StringBuilder(8);
		sb.append(version.getMajor());
		sb.append('.');
		sb.append(version.getMinor());
		return sb.toString();
	}
	
	private boolean hasNonBasePackages(final IRPkgSet.Ext pkgSet) {
		final List<? extends IRPkgList<? extends IRPkgInfoAndData>> all = pkgSet.getInstalled().getAll();
		for (final IRPkgList<? extends IRPkgInfoAndData> pkgList : all) {
			for (final IRPkgInfoAndData pkg : pkgList) {
				if ((pkg.getLibraryLocation() == null || pkg.getLibraryLocation().getSource() != IRLibraryLocation.EPLUGIN)
						&& !"base".equals(pkg.getPriority())) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}
	
	private void handleNewVersion(final IREnvConfiguration rConfig, final RProcess tool,
			final IRPkgManager.Ext rPkgManager, final Version oldVersion, final Version newVersion) {
		final IWorkbenchPage page = NicoUI.getToolRegistry().findWorkbenchPage(tool);
		final Shell shell = page.getWorkbenchWindow().getShell();
		final Display display = UIAccess.getDisplay(shell);
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (ToolMessageDialog.openQuestion(tool, shell, Messages.REnvIndex_NewVersion_title,
						NLS.bind(Messages.REnvIndex_NewVersion_message, new Object[] {
								rConfig.getName(), mainVersionString(oldVersion),
								mainVersionString(newVersion) }))) {
					RPkgManagerUI.openDialog(rPkgManager, tool, shell,
							new StartAction(StartAction.REINSTALL) );
				}
			}
		});
	}
	
}
