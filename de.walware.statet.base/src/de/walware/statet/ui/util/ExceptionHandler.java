/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ui.util;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.base.StatetPlugin;
import de.walware.statet.internal.ui.StatetMessages;


public class ExceptionHandler {

	
	/**
	 * Handles the given <code>CoreException</code>. The workbench shell is used as a parent
	 * for the dialog window.
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, String title, String message) {
		handle(e, StatetPlugin.getActiveWorkbenchShell(), title, message);
	}
	
	/**
	 * Handles the given <code>CoreException</code>. 
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param parent the dialog window's parent shell
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, Shell parent, String title, String message) {
		perform(e, parent, title, message);
	}
	
	
	public static void handle(InvocationTargetException e, Shell shell, String title, String message) {
		
		Throwable target = e.getTargetException();
		if (target instanceof CoreException) {
			perform((CoreException) target, shell, title, message);
		} else {
			// CoreExceptions are handled above, but unexpected runtime
			// exceptions and errors may still occur.
			StatetPlugin.logUnexpectedError(target);
			displayMessageDialog(target.getMessage(), shell, title, message);
		}
	}
	
	
	protected static void perform(CoreException e, Shell shell, String title, String message) {

		StatetPlugin.log(new Status(
				IStatus.INFO, 
				StatetPlugin.ID, 
				IStatetStatusConstants.INTERNAL_ERROR, 
				StatetMessages.InternalError_HandledProblem, 
				e));
		IStatus status = e.getStatus();
		if (status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			displayMessageDialog(e.getMessage(), shell, title, message);
		}
	}

	
	private static void displayMessageDialog(String exceptionMessage, Shell shell, String title, String message) {
		
		StringWriter msg = new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
		}
		if (exceptionMessage == null || exceptionMessage.length() == 0)
			msg.write("No details available. See error log."); 
		else
			msg.write(exceptionMessage);
		
		MessageDialog.openError(shell, title, msg.toString());			
	}	

}
