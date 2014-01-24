/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.pkgmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.util.ToolMessageDialog;

import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.ui.pkgmanager.RPkgManagerDialog;


public class RPkgManagerUI {
	
	private static final Map<IREnv, RPkgManagerDialog> DIALOGS = new HashMap<IREnv, RPkgManagerDialog>();
	
	private static Shell getShell(final IShellProvider shellProvider) {
		Shell shell = null;
		if (shellProvider != null) {
			shell = shellProvider.getShell();
		}
		if (shell == null) {
			shell = UIAccess.getActiveWorkbenchShell(false);
		}
		return shell;
	}
	
	public static RPkgManagerDialog openDialog(final IRPkgManager.Ext manager,
			final RProcess tool, final Shell parentShell, final StartAction startAction) {
		final IREnv rEnv = manager.getREnv();
		
		RPkgManagerDialog dialog = DIALOGS.get(rEnv);
		if (dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed()) {
			dialog.close();
		}
		
		dialog = new RPkgManagerDialog(manager, tool, parentShell);
		dialog.setBlockOnOpen(false);
		DIALOGS.put(rEnv, dialog);
		
		dialog.open();
		dialog.getShell().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				final RPkgManagerDialog d = DIALOGS.get(rEnv);
				if (d != null && d.getShell() == e.getSource()) {
					DIALOGS.remove(rEnv);
				}
			}
		});
		
		if (startAction != null) {
			dialog.start(startAction);
		}
		
		return dialog;
	}
	
	public static boolean requestRequiredRPkgs(final IRPkgManager.Ext manager,
			final List<String> pkgNames,
			final IRToolService r, final IProgressMonitor monitor,
			final IShellProvider shellProvider, final String message,
			final Runnable okRunnable, final Runnable cancelRunnable) throws CoreException {
		if (manager.requiresUpdate()) {
			manager.update(r, monitor);
		}
		final IRPkgSet rPkgSet = manager.getRPkgSet();
		final List<String> missingPkgs = new ArrayList<String>(pkgNames.size());
		final StringBuilder sb = new StringBuilder();
		for (final String pkgName : pkgNames) {
			if (rPkgSet.getInstalled().containsByName(pkgName)) {
				continue;
			}
			missingPkgs.add(pkgName);
			sb.append("\n\t"); //$NON-NLS-1$
			sb.append(pkgName);
		}
		if (sb.length() == 0) {
			return true;
		}
		sb.insert(0, message);
		sb.append("\n\nDo you want to install the packages now?");
		final RProcess tool = (RProcess) r.getTool();
		final Shell shell = getShell(shellProvider);
		final Display display = (shell != null) ? shell.getDisplay() : UIAccess.getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				final boolean yes = ToolMessageDialog.openQuestion(tool, shell,
						"Required R Packages", sb.toString());
				if (yes) {
					final RPkgManagerDialog dialog = openDialog(manager, tool, shell,
							new StartAction(StartAction.INSTALL, missingPkgs) );
					if (okRunnable != null) {
						dialog.getShell().addListener(SWT.Close, new Listener() {
							@Override
							public void handleEvent(final Event event) {
								display.asyncExec(okRunnable);
							}
						});
					}
					return;
				}
				if (cancelRunnable != null) {
					cancelRunnable.run();
				}
			}
		});
		return false;
	}
	
}
