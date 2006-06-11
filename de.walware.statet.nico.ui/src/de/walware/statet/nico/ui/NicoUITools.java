/**
 * 
 */
package de.walware.statet.nico.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.ui.util.ExceptionHandler;


/**
 * 
 * @author Stephan Wahlbrink
 */
public class NicoUITools {

	
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
