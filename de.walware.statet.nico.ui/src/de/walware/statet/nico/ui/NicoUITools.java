/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
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
	 * @param tool the tool to check or <code>null</code>
	 * @throws CoreException if tool is missing or the wrong type
	 */
	public static void accessTool(final String type, final ITool tool) throws CoreException {
		if (tool == null || (type != null && !type.equals(tool.getMainType()))) {
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
	 * @param type the id of the expected feature set (optional)
	 * @param tool the tool to check or <code>null</code>
	 * @return the controller of the tool
	 */
	public static boolean isToolReady(final String type, final String featureSetID, final ITool tool) {
		return (tool != null && !tool.isTerminated()
				&& tool.getMainType() == type
				&& tool.isProvidingFeatureSet(featureSetID) );
	}
	
	/**
	 * 
	 * @param type the expected main type (optional)
	 * @param process the tool to check or <code>null</code>
	 * @return the controller of the tool
	 * @throws CoreException if tool is missing, wrong type or terminated
	 */
	public static <W extends ToolWorkspace> ToolController accessController(final String type, final ToolProcess process) throws CoreException {
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
	public static <W extends ToolWorkspace> ToolController accessController(final String type, final String featureSetId, final ToolProcess process) throws CoreException {
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
	public static <W extends ToolWorkspace> ToolController getController(final String type, final String featureSetId, final ToolProcess process) {
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
	
	public static IConsoleView getConsoleView(final NIConsole console, final IWorkbenchPage page) {
		final ToolRegistry registry = NicoUIPlugin.getDefault().getToolRegistry();
		return registry.getConsoleView(console, page);
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
		return NLS.bind(NicoUIMessages.SubmitTask_name, process.getLabel(ITool.DEFAULT_LABEL));
	}
	
	public static void runSubmitInBackground(final ToolProcess process, final IRunnableWithProgress runnable, final Shell shell) {
		try {
			// would busycursor or job be better?
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					NLS.bind(NicoUIMessages.Submit_error_message, process.getLabel(ITool.LONG_LABEL)), e.getCause()),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final InterruptedException e) {
			// something to do?
		}
	}
	
	/**
	 * Computes and returns the image for a tool
	 * (e.g. for console or in dialogs).
	 * 
	 * @return an image for this tool or <code>null</code>
	 */
	public static Image getImage(final ToolProcess process) {
		final ILaunchConfiguration configuration = process.getLaunch().getLaunchConfiguration();
		if (configuration != null) {
			ILaunchConfigurationType type;
			try {
				type = configuration.getType();
				return DebugUITools.getImage(type.getIdentifier());
			}
			catch (final CoreException e) {
				NicoUIPlugin.logError(-1, "An error occurred when loading images", e); //$NON-NLS-1$
			}
		}
		return null;
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
	public static Image getImage(final IToolRunnable runnable) {
		if (runnable == null) {
			return null;
		}
		Image image = null;
		if (runnable instanceof IToolRunnableDecorator) {
			image = ((IToolRunnableDecorator) runnable).getImage();
		}
		if (image == null) {
			final NicoUIPlugin plugin = NicoUIPlugin.getDefault();
			final IToolRunnableDecorator decorator = plugin.getUIDecoratorsRegistry()
					.getDecoratorForRunnable(runnable.getTypeId());
			if (decorator != null) {
				image = decorator.getImage();
			}
			if (image == null) {
				image = plugin.getImageRegistry().get(NicoUI.OBJ_TASK_DUMMY_IMAGE_ID);
			}
		}
		return image;
	}
	
	
	/**
	 * 
	 * @param viewId the view id
	 * @param tool the tool process
	 * @param createNew if a new view should be created when none found
	 * @return
	 * @throws PartInitException 
	 */
	public static IViewPart getView(final String viewId, final ToolProcess tool,
			final boolean createNew) throws PartInitException {
		IViewPart view = null;
		
		final IWorkbenchPage toolPage = NicoUI.getToolRegistry().findWorkbenchPage(tool);
		view = toolPage.findView(viewId);
		if (view != null) {
			view.getViewSite().getPage().activate(view);
			return view;
		}
		final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			final IWorkbenchPage page = windows[i].getActivePage();
			if (page == toolPage) {
				continue;
			}
			final ToolSessionUIData session = NicoUI.getToolRegistry().getActiveToolSession(page);
			if (session.getProcess() == tool) {
				view = page.findView(viewId);
				if (view != null) {
					view.getViewSite().getPage().activate(view);
					return view;
				}
			}
		}
		
		if (!createNew) {
			return null;
		}
		return toolPage.showView(viewId);
	}
	
	
}
