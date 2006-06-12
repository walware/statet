/**
 * 
 */
package de.walware.statet.nico.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.internal.NicoUIPlugin;
import de.walware.statet.nico.ui.internal.ToolRegistry;
import de.walware.statet.ui.util.ExceptionHandler;


/**
 * 
 * @author Stephan Wahlbrink
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
		
		return NLS.bind(NicoMessages.SubmitTask_name, process.getLabel());
	}
	
	public static void runSubmitInBackground(ToolProcess process, IRunnableWithProgress runnable, Shell shell) {
		
		try {
			// would busycursor or job be better?
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, shell, 
					NLS.bind(NicoMessages.Submit_error_message, process.getLabel()) 
			);
		} catch (InterruptedException e) {
			// something to do?
		}
	}
	
}
