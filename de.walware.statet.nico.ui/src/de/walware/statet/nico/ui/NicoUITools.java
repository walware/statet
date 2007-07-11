/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.ToolRegistry;
import de.walware.statet.nico.ui.console.NIConsole;


/**
 * 
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

	
	public static void startConsoleLazy(final NIConsole console, final IWorkbenchPage page) {
    	DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
    		public void handleDebugEvents(DebugEvent[] events) {
    			ToolProcess process = console.getProcess();
    			for (DebugEvent event : events) {
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
							showConsole(console, page, true);
							break;
						}
					}
				}
    		}
    	});
	}
	
	/**
	 * 
	 * Note: getting console does not affects UI.
	 * @param process
	 * @return the console of the process.
	 */
	public static NIConsole getConsole(ToolProcess process) {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (IConsole console : consoles) {
			if (console instanceof NIConsole) {
				NIConsole nico = (NIConsole) console;
				if (process.equals(nico.getProcess())) {
					return nico;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * Note: task is always scheduled as Job, so thread (e.g. UI thread) of
	 * caller is irrelevant.
	 * @param console
	 * @param page
	 * @param activate
	 */
	public static void showConsole(NIConsole console, IWorkbenchPage page,
			boolean activate) {
		ToolRegistry registry = NicoUIPlugin.getDefault().getToolRegistry();
		registry.showConsole(console, page, activate);
	}

	
	public static String createSubmitMessage(ToolProcess process) {
		return NLS.bind(NicoUIMessages.SubmitTask_name, process.getToolLabel(false));
	}
	
	public static void runSubmitInBackground(ToolProcess process, IRunnableWithProgress runnable, Shell shell) {
		try {
			// would busycursor or job be better?
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, shell,
					NLS.bind(NicoUIMessages.Submit_error_message, process.getToolLabel(true))
			);
		} catch (InterruptedException e) {
			// something to do?
		}
	}
	
    /**
     * Computes and returns the image descriptor for a tool
     * (e.g. for console or in dialogs).
     * 
     * @return an image descriptor for this tool or <code>null</code>
     */
    public static ImageDescriptor getImageDescriptor(ToolProcess process) {
        ILaunchConfiguration configuration = process.getLaunch().getLaunchConfiguration();
        if (configuration != null) {
            ILaunchConfigurationType type;
            try {
                type = configuration.getType();
                return DebugUITools.getImageDescriptor(type.getIdentifier());
            } catch (CoreException e) {
                NicoUIPlugin.log(e.getStatus());
            }
        }
        return null;
    }

    /**
     * Computes and returns the image descriptor for a runnable
     * 
     * @return an image descriptor for this runnable or <code>null</code>
     */
    public static ImageDescriptor getImageDescriptor(IToolRunnable runnable) {
		IToolRunnableAdapter adapter = getRunnableAdapter(runnable);
		if (adapter != null) {
			return adapter.getImageDescriptor();
		}
		return null;
    }

    
    private static IToolRunnableAdapter getRunnableAdapter(IToolRunnable runnable) {
        if (!(runnable instanceof IAdaptable)) {
            return null;
        }
        return (IToolRunnableAdapter) ((IAdaptable) runnable)
                .getAdapter(IToolRunnableAdapter.class);
    }
	
}
