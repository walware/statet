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

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.ToolRegistry;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.ui.util.ExceptionHandler;


/**
 * 
 */
public class NicoUITools {

	
	public static IToolRegistry getRegistry() {
		
		return NicoUIPlugin.getDefault().getToolRegistry();
	}
	
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
