/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.nico.core.ITool;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.ToolRegistry;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 * Public Nico-UI tools.
 * <p>
 * Access via static methods.
 */
public class NicoUITools {
	
	
//	public static List<IConsoleView> getConsoleViews(IWorkbenchPage page) {
//		List<IConsoleView> consoleViews = new ArrayList<IConsoleView>();
//
//		IViewReference[] allReferences = page.getViewReferences();
//		for (IViewReference reference : allReferences) {
//			if (reference.getId().equals(IConsoleConstants.ID_CONSOLE_VIEW)) {
//				IViewPart view = reference.getView(false);
//				if (view != null) {
//					consoleViews.add((IConsoleView) view);
//				}
//			}
//		}
//		return consoleViews;
//	}
	
	
	public static void startConsoleLazy(final NIConsole console, final IWorkbenchPage page, final boolean pin) {
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			public void handleDebugEvents(final DebugEvent[] events) {
				final ToolProcess process = console.getProcess();
				for (final DebugEvent event : events) {
					if (event.getSource() == process) {
						switch (event.getKind()) {
						case DebugEvent.TERMINATE:
							DebugPlugin.getDefault().removeDebugEventListener(this);
							break;
						case DebugEvent.MODEL_SPECIFIC:
							// register and open console
							DebugPlugin.getDefault().removeDebugEventListener(this);
							ConsolePlugin.getDefault().getConsoleManager().addConsoles(
									new IConsole[] { console });
							final ToolRegistry registry = NicoUIPlugin.getDefault().getToolRegistry();
							registry.showConsoleExplicitly(console, page, pin);
							break;
						}
					}
				}
			}
		});
	}
	
	public static ToolProcess getTool(final IWorkbenchPart part) {
		final ToolProcess tool = (ToolProcess) part.getAdapter(ITool.class);
		if (tool != null) {
			return tool;
		}
		return NicoUIPlugin.getDefault().getToolRegistry().getActiveToolSession(
				part.getSite().getPage()).getProcess();
	}
	
	/**
	 * 
	 * Note: getting console does not affects UI.
	 * @param process
	 * @return the console of the process.
	 */
	public static NIConsole getConsole(final ToolProcess process) {
		final IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (final IConsole console : consoles) {
			if (console instanceof NIConsole) {
				final NIConsole nico = (NIConsole) console;
				if (process.equals(nico.getProcess())) {
					return nico;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param type an optional expected main type
	 * @param process the tool to check or <code>null</code>
	 * @throws CoreException if tool is missing or the wrong type
	 */
	public static void accessTool(final String type, final ToolProcess process) throws CoreException {
		if (process == null || (type != null && !type.equals(process.getMainType()))) {
			throw new CoreException(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					(type != null) ?
							NLS.bind("No session of {0} is active in the current workbench window.", type) :
							"No tool session is active in the current workbench window.", null)
			);
		}
	}
	
	/**
	 * 
	 * @param type the expected main type (optional)
	 * @param process the tool to check or <code>null</code>
	 * @return the controller of the tool
	 * @throws CoreException if tool is missing, wrong type or terminated
	 */
	public static ToolController accessController(final String type, final ToolProcess process) throws CoreException {
		return accessController(type, null, process);
	}
	
	/**
	 * 
	 * @param type the expected main type (optional)
	 * @param type the id of the expected feature set (optional)
	 * @param process the tool to check or <code>null</code>
	 * @return the controller of the tool
	 * @throws CoreException if tool is missing, wrong type or terminated
	 */
	public static ToolController accessController(final String type, final String featureSetId, final ToolProcess process) throws CoreException {
		accessTool(type, process);
		final ToolController controller = process.getController();
		if (controller == null) {
			throw new CoreException(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					NLS.bind("The active session of {0} ''{1}'' was terminated.", type, process.getLabel()), null));
		}
		if (featureSetId != null && !process.isProvidingFeatureSet(featureSetId)) {
			throw new CoreException(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					NLS.bind("The active session of {0} ''{1}'' doesn't support allow required features.", type, process.getLabel()), null));
		}
		return controller;
	}
	
	/**
	 * 
	 * @param type the expected main type (optional)
	 * @param type the id of the expected feature set (optional)
	 * @param process the tool to check or <code>null</code>
	 * @return the controller of the tool
	 * @throws CoreException if tool is missing, wrong type or terminated
	 */
	public static ToolController getController(final String type, final String featureSetId, final ToolProcess process) {
		if (process == null || (type != null && !type.equals(process.getMainType()))) {
			return null;
		}
		final ToolController controller = process.getController();
		if (controller == null) {
			return null;
		}
		if (featureSetId != null && !process.isProvidingFeatureSet(featureSetId)) {
			return null;
		}
		return controller;
	}
	
	/**
	 * 
	 * Note: task is always scheduled as Job, so thread (e.g. UI thread) of
	 * caller is irrelevant.
	 * @param console
	 * @param page
	 * @param activate
	 */
	public static void showConsole(final NIConsole console, final IWorkbenchPage page,
			final boolean activate) {
		final ToolRegistry registry = NicoUIPlugin.getDefault().getToolRegistry();
		registry.showConsole(console, page, activate);
	}
	
	
	public static String createSubmitMessage(final ToolProcess process) {
		return NLS.bind(NicoUIMessages.SubmitTask_name, process.getToolLabel(false));
	}
	
	public static void runSubmitInBackground(final ToolProcess process, final IRunnableWithProgress runnable, final Shell shell) {
		try {
			// would busycursor or job be better?
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, NicoUI.PLUGIN_ID, -1,
					NLS.bind(NicoUIMessages.Submit_error_message, process.getToolLabel(true)), e),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
			// something to do?
		}
	}
	
	/**
	 * Computes and returns the image descriptor for a tool
	 * (e.g. for console or in dialogs).
	 * 
	 * @return an image descriptor for this tool or <code>null</code>
	 */
	public static ImageDescriptor getImageDescriptor(final ToolProcess process) {
		final ILaunchConfiguration configuration = process.getLaunch().getLaunchConfiguration();
		if (configuration != null) {
			ILaunchConfigurationType type;
			try {
				type = configuration.getType();
				return DebugUITools.getImageDescriptor(type.getIdentifier());
			}
			catch (final CoreException e) {
				NicoUIPlugin.logError(-1, "An error occurred when loading images", e); //$NON-NLS-1$
			}
		}
		return null;
	}
	
	/**
	 * Computes and returns the image descriptor for a runnable
	 * 
	 * @return an image descriptor for this runnable or <code>null</code>
	 */
	public static ImageDescriptor getImageDescriptor(final IToolRunnable runnable) {
		final IToolRunnableAdapter adapter = getRunnableAdapter(runnable);
		if (adapter != null) {
			return adapter.getImageDescriptor();
		}
		return null;
	}
	
	
	private static IToolRunnableAdapter getRunnableAdapter(final IToolRunnable runnable) {
		if (!(runnable instanceof IAdaptable)) {
			return null;
		}
		return (IToolRunnableAdapter) ((IAdaptable) runnable)
				.getAdapter(IToolRunnableAdapter.class);
	}
	
}
