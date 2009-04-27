/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_PREVIEW;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_TEX;
import static de.walware.statet.r.internal.sweave.processing.RweaveTexLaunchDelegate.STEP_WEAVE;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.core.OverlayLaunchConfiguration;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;


/**
 * Manages profiles of a launchconfiguration type.
 */
public class SweaveProcessing implements ILaunchConfigurationListener {
	
	
	public static final String ATT_BUILDSTEPS = "buildSteps"; //$NON-NLS-1$
	
	private static final Comparator<ILaunchConfiguration> CONFIG_COMPARATOR = new LaunchConfigUtil.LaunchConfigurationComparator();
	
	
	public static boolean isEnabled(final int expectedFlag, final int currentFlags) {
		return ((currentFlags & 0xf) == 0 || (currentFlags & expectedFlag) != 0);
	}
	
	
	public static interface IProcessingListener {
		
		public void activeProfileChanged(ILaunchConfiguration config);
		public void availableProfileChanged(ILaunchConfiguration[] configs);
		
	}
	
	
	private FastList<IProcessingListener> fListenerList = new FastList<IProcessingListener>(IProcessingListener.class);
	
	private ILaunchConfigurationType fConfigType;
	private ILaunchConfiguration[] fCurrentConfigs;
	private ILaunchConfiguration fActiveSweaveConfig;
	
	
	public SweaveProcessing(final String id) {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		fConfigType = launchManager.getLaunchConfigurationType(id);
		launchManager.addLaunchConfigurationListener(this);
		
		final IDialogSettings settings = DialogUtil.getDialogSettings(SweavePlugin.getDefault(), fConfigType.getIdentifier());
		final String s = settings.get("activeProfile"); //$NON-NLS-1$
		if (s != null && s.length() > 0) {
			final ILaunchConfiguration[] configs = getAvailableProfiles();
			for (int i = 0; i < configs.length; i++) {
				if (s.equals(configs[i].getName())) {
					setActiveProfile(configs[i]);
					break;
				}
			}
		}
	}
	
	public void dispose() {
		final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchConfigurationListener(this);
		
		final IDialogSettings settings = DialogUtil.getDialogSettings(SweavePlugin.getDefault(), fConfigType.getIdentifier());
		settings.put("activeProfile", (fActiveSweaveConfig != null) ? fActiveSweaveConfig.getName() : null); //$NON-NLS-1$
	}
	
	
	public void launchConfigurationAdded(final ILaunchConfiguration configuration) {
		try {
			if (configuration.getType() == fConfigType) {
				if (DebugPlugin.getDefault().getLaunchManager().getMovedFrom(configuration) == fActiveSweaveConfig) {
					update(true, true, configuration);
				}
				else {
					update(true, false, null);
				}
			}
		} catch (final CoreException e) {
		}
	}
	
	public void launchConfigurationChanged(final ILaunchConfiguration configuration) {
		try {
			if (configuration.getType() == fConfigType && !configuration.isWorkingCopy()) {
				if (DebugPlugin.getDefault().getLaunchManager().getMovedFrom(configuration) == fActiveSweaveConfig) {
					update(true, true, configuration);
				}
				else {
					update(true, false, null);
				}
			}
		} catch (final CoreException e) {
		}
	}
	
	public void launchConfigurationRemoved(final ILaunchConfiguration configuration) {
		try {
			// no possible to test for type (exception)
			if (configuration == fActiveSweaveConfig) {
				update(true, true, null);
			}
			else {
				final ILaunchConfiguration[] configs = fCurrentConfigs;
				if (configs != null) {
					for (int i = 0; i < configs.length; i++) {
						if (configs[i] == configuration) {
							update(true, false, null);
							break;
						}
					}
				}
			}
		} catch (final CoreException e) {
		}
	}
	
	private synchronized void update(final boolean updateList, boolean updateActive, final ILaunchConfiguration newActive) throws CoreException {
		if (updateActive && fActiveSweaveConfig == newActive) {
			updateActive = false;
		}
		
		final IProcessingListener[] listeners = fListenerList.toArray();
		if (updateActive) {
			fActiveSweaveConfig = newActive;
		}
		if (updateList) {
			if (listeners.length > 0) {
				fCurrentConfigs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(fConfigType);
				for (int i = 0; i < listeners.length; i++) {
					listeners[i].availableProfileChanged(fCurrentConfigs);
				}
			}
			else {
				fCurrentConfigs = null;
			}
		}
		if (updateActive) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].activeProfileChanged(newActive);
			}
		}
	}
	
	public void addProcessingListener(final IProcessingListener listener) {
		fListenerList.add(listener);
	}
	
	public void removeProcessingListener(final IProcessingListener listener) {
		fListenerList.remove(listener);
	}
	
	public ILaunchConfigurationType getConfigurationType() {
		return fConfigType;
	}
	
	public ILaunchConfiguration[] getAvailableProfiles() {
		ILaunchConfiguration[] configs = fCurrentConfigs;
		if (configs == null) {
			try {
				configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(fConfigType);
				Arrays.sort(configs, CONFIG_COMPARATOR);
				fCurrentConfigs = configs;
			} catch (final CoreException e) {
			}
		}
		return configs;
	}
	
	public void setActiveProfile(final ILaunchConfiguration configuration) {
		try {
			update(false, true, configuration);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
					-1, "Setting Sweave profile as default failed.", e)); //$NON-NLS-1$
		}
	}
	
	public ILaunchConfiguration getActiveProfile() {
		return fActiveSweaveConfig;
	}
	
	
	public void openConfigurationDialog(final Shell shell, IStructuredSelection selection) {
		if (selection == null || selection.isEmpty()) {
			selection = new StructuredSelection(fConfigType);
		}
		DebugUITools.openLaunchConfigurationDialogOnGroup(shell,
				selection, "org.eclipse.ui.externaltools.launchGroup"); //$NON-NLS-1$
	}
	
	public void launch(final ILaunchConfiguration configuration, final int flags) {
		final String label = getLabelForLaunch(configuration, flags, true);
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
				page.activate(page.getActiveEditor());
			}
		});
		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException {
				monitor.beginTask(label, 1);
				try {
					final Map<String, Object> attributes = Collections.singletonMap(SweaveProcessing.ATT_BUILDSTEPS, (Object) new Integer(flags));
					final ILaunchConfiguration config = new OverlayLaunchConfiguration(configuration, attributes);
					final String mode = ILaunchManager.RUN_MODE;
					// TODO E-3.5 bug #200997, set register to false
					if (isEnabled(0x1, flags)) {
						config.launch(mode, new SubProgressMonitor(monitor, 1), true, true);
					}
					else {
						config.launch(mode, new SubProgressMonitor(monitor, 1), false, true);
					}
				}
				catch (final CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					Messages.Processing_Launch_error_message+label, e.getTargetException()),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
		}
	}
	
	// Rweave Tex Specific
	public String getLabelForLaunch(final ILaunchConfiguration configuration, final int flags, final boolean noMnemonics) {
		String label;
		switch (flags & 0xf) {
		case 0:
		case STEP_WEAVE | STEP_TEX | STEP_PREVIEW:
			label = Messages.ProcessingAction_BuildAndPreview_label;
			break;
		case STEP_WEAVE | STEP_TEX:
			label = Messages.ProcessingAction_BuildDoc_label;
			break;
		case STEP_WEAVE:
			label = Messages.ProcessingAction_Sweave_label;
			break;
		case STEP_TEX:
			label = Messages.ProcessingAction_Tex_label;
			break;
		case STEP_PREVIEW:
			label = Messages.ProcessingAction_Preview_label;
			break;
		default:
			throw new IllegalArgumentException();
		}
		if (configuration != null) {
			label += " '" + configuration.getName() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return (noMnemonics) ? MessageUtil.removeMnemonics(label) : label;
	}
	
}
