/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.pkgmanager;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.console.core.AbstractRDataRunnable;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.renv.IREnv;


public class OpenRPkgManagerHandler extends AbstractHandler {
	
	
	private final RProcess fProcess;
	
	private final Shell fShell;
	
	
	public OpenRPkgManagerHandler(final RProcess process, final Shell shell) {
		fProcess = process;
		fShell = shell;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		if (fProcess != null) {
			setBaseEnabled(!fProcess.isTerminated());
		}
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IRPkgManager.Ext manager = (IRPkgManager.Ext) getPackageManager();
		if (manager == null) {
			return null;
		}
		if (manager.requiresUpdate()) {
			fProcess.getQueue().add(new AbstractRDataRunnable("r/pkgmanager/open", //$NON-NLS-1$
					"Open R Package Manager") {
				@Override
				protected void run(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
					manager.update(r, monitor);
					UIAccess.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							RPkgManagerUI.openDialog(manager, fProcess, fShell, getStartAction());
						}
					});
				}
			});
		}
		else {
			RPkgManagerUI.openDialog(manager, fProcess, fShell, getStartAction());
		}
		return null;
	}
	
	protected IRPkgManager getPackageManager() {
		final IREnv env = (IREnv) fProcess.getAdapter(IREnv.class);
		if (env != null) {
			return RCore.getRPkgManager(env);
		}
		return null;
	}
	
	protected StartAction getStartAction() {
		return null;
	}
	
}
