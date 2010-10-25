/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.debug.core.OverlayLaunchConfiguration;

import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.actions.ToolRetargetableHandler;


public class ReconnectEngineHandler extends ToolRetargetableHandler {
	
	
	public ReconnectEngineHandler(final IToolProvider toolProvider, final IServiceLocator serviceLocator) {
		super(toolProvider, serviceLocator);
		init();
	}
	
	
	@Override
	protected boolean evaluateEnabled() {
		final ToolProcess tool = getTool();
		try {
			return ((tool != null)
					&& tool.isProvidingFeatureSet(IRemoteEngineController.FEATURE_SET_ID)
					&& tool.isTerminated()
					&& (tool.getExitValue() == ToolProcess.EXITCODE_DISCONNECTED) );
		}
		catch (final DebugException e) {
			return false;
		}
	}
	
	@Override
	protected Object doExecute(final ExecutionEvent event) {
		final ToolProcess tool = getCheckedTool();
		
		final IProgressService progressService = getProgressService();
		try {
			progressService.busyCursorWhile(createRunnable(tool));
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					"Reconnecting failed.", e.getCause()), StatusManager.SHOW | StatusManager.LOG);
		}
		catch (final InterruptedException e) {
		}
		return null;
	}
	
	private IRunnableWithProgress createRunnable(final ToolProcess process) {
		return new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final ILaunch originallaunch = process.getLaunch();
				ILaunchConfiguration originalConfig = originallaunch.getLaunchConfiguration();
				if (originalConfig instanceof OverlayLaunchConfiguration) {
					originalConfig = ((OverlayLaunchConfiguration) originalConfig).getOriginal();
				}
				
				final Map<String, Object> reconnect = new HashMap<String, Object>();
				process.prepareRestart(reconnect);
				
				final Map<String, Object> add = new HashMap<String, Object>();
				add.put(IRemoteEngineController.LAUNCH_RECONNECT_ATTRIBUTE, reconnect);
				final ILaunchConfiguration reconnectConfig = new OverlayLaunchConfiguration(originalConfig, add);
				try {
					final ILaunch reconnectLaunch = reconnectConfig.launch(originallaunch.getLaunchMode(), monitor, false);
					
//					if (dispose != null) {
//						final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
//						final ILaunchesListener2 launchListener = new ILaunchesListener2() {
//							public void launchesAdded(final ILaunch[] launches) {
//							}
//							public void launchesChanged(final ILaunch[] launches) {
//								check(launches);
//							}
//							public void launchesTerminated(final ILaunch[] launches) {
//								check(launches);
//							}
//							public void launchesRemoved(final ILaunch[] launches) {
//								check(launches);
//							}
//							private void check(final ILaunch[] launches) {
//								if (contains(launches, reconnectLaunch)) {
//									process.approveDispose(dispose);
//									DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
//								}
//							}
//						};
//						launchManager.addLaunchListener(launchListener);
//						if (!contains(launchManager.getLaunches(), reconnectLaunch)) {
//							process.approveDispose(dispose);
//							launchManager.removeLaunchListener(launchListener);
//						}
//					}
				}
				catch (final CoreException e) {
					if (reconnect != null) {
						process.restartCompleted(reconnect);
					}
					throw new InvocationTargetException(e);
				}
				
			}
		};
	}
	
	private boolean contains(final ILaunch[] launches, final ILaunch search) {
		for (final ILaunch launch : launches) {
			if (search == launch) {
				return true;
			}
		}
		return false;
	}
	
}
