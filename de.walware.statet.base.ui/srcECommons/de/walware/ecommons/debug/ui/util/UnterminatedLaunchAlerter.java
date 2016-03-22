/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.ui.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.debug.internal.ui.Messages;
import de.walware.ecommons.ui.util.UIAccess;


/**
 * A workbench listener that warns the user about any running tools when
 * the workbench closes. Programs are killed when the VM exits.
 * 
 * Register your type on launch with {@link #registerLaunchType(String)}
 */
public class UnterminatedLaunchAlerter implements IWorkbenchListener {
	
	
	private static Object gMutex = new Object();
	private static UnterminatedLaunchAlerter gInstance;
	
	public static void registerLaunchType(final String id) {
		synchronized (gMutex) {
			if (gInstance == null) {
				gInstance = new UnterminatedLaunchAlerter();
			}
			gInstance.fLauchTypeIds.add(id);
		}
	}
	
	
	private final Set<String> fLauchTypeIds= new HashSet<>();
	
	private UnterminatedLaunchAlerter() { 
		PlatformUI.getWorkbench().addWorkbenchListener(this);
	}
	
	
	@Override
	public boolean preShutdown(final IWorkbench workbench, final boolean forced) {
		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final List<ILaunchConfigurationType> programTypes= new LinkedList<>();
		synchronized (gMutex) {
			for (final String id : fLauchTypeIds) {
				final ILaunchConfigurationType programType = manager.getLaunchConfigurationType(id);
				if (programType != null) {
					programTypes.add(programType);
				}
			}
		}
		if (programTypes.isEmpty()) {
			return true;
		}
		
		final Set<ILaunchConfigurationType> stillRunningTypes= new HashSet<>();
		int count = 0;
		final ILaunch launches[] = manager.getLaunches();
		for (final ILaunch launch : launches) {
			ILaunchConfigurationType configType;
			ILaunchConfiguration config;
			try {
				config = launch.getLaunchConfiguration();
				if (config == null) {
					continue;
				}
				configType = config.getType();
			} catch (final CoreException e) {
				continue;
			}
			if (programTypes.contains(configType) && !launch.isTerminated()) {
				count++;
				stillRunningTypes.add(configType);
			}
		}
		if (stillRunningTypes.isEmpty()) {
			return true;
		}
		
		final StringBuilder names = new StringBuilder(stillRunningTypes.size()*20);
		names.append('\n');
		for (final ILaunchConfigurationType type : stillRunningTypes) {
			names.append("- "); //$NON-NLS-1$
			names.append(type.getName());
			names.append('\n');
		}
		final String message = NLS.bind(Messages.UnterminatedLaunchAlerter_WorkbenchClosing_message, 
				new Object[] { count, names });
		if (forced) {
			MessageDialog.openWarning(UIAccess.getDisplay().getActiveShell(),
					Messages.UnterminatedLaunchAlerter_WorkbenchClosing_title, message);
			return true;
		}
		else {
			final MessageDialog dialog = new MessageDialog(UIAccess.getDisplay().getActiveShell(),
					Messages.UnterminatedLaunchAlerter_WorkbenchClosing_title, null, message,
					MessageDialog.WARNING,
					new String[] { 
						Messages.UnterminatedLaunchAlerter_WorkbenchClosing_button_Continue,
						Messages.UnterminatedLaunchAlerter_WorkbenchClosing_button_Cancel, 
					}, 1);
			final int answer = dialog.open();
			if (answer == 1) {
				return false;
			}
			return true;
		}
	}
	
	@Override
	public void postShutdown(final IWorkbench workbench) {
	}
	
}
