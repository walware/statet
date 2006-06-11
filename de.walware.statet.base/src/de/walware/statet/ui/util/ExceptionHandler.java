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
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.internal.ui.StatetMessages;


public class ExceptionHandler {

	
	/**
	 * Handles the given <code>CoreException</code>. The workbench shell is used as a parent
	 * for the dialog window.
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, String message) {
		
		handle(e, null, message);
	}
	
	/**
	 * Handles the given <code>CoreException</code>. 
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param parent the dialog window's parent shell
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, Shell shell, String message) {
		
		perform(e, shell, message);
	}
	
	/**
	 * Handles the given <code>InvocationTargetException</code>. 
	 * 
	 * @param e the <code>InvocationTargetException</code> to be handled
	 * @param parent the dialog window's parent shell or <code>null</code>.
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(InvocationTargetException e, Shell shell, String message) {
		
		Throwable target = e.getTargetException();
		if (target instanceof CoreException) {
			perform((CoreException) target, shell, message);
		} else {
			// CoreExceptions are handled above, but unexpected runtime
			// exceptions and errors may still occur.
			StatetPlugin.logUnexpectedError(target);
			displayMessageDialog(target.getMessage(), shell, message);
		}
	}
	
	/**
	 * Handles the given <code>IStatus</code>, which describes the error.
	 * 
	 * @param status a status-object representing an error.
	 */
	public static void handle(IStatus status) {
		
		perform(status, null, status.getMessage());
	}
	
	
	private static void perform(CoreException e, Shell shell, String message) {

		IStatus status = e.getStatus();
		if (status != null) {
			perform(status, shell, message);
		} 
		else {
			StatetPlugin.log(new Status(Status.ERROR, StatetPlugin.ID, 0, 
					"No status of CoreException available.", e));
			displayMessageDialog(e.getMessage(), shell, message);
		}
	}
	
	private static void perform(final IStatus status, final Shell shell, final String message) {
		
		StatetPlugin.log(status);
		StatetPlugin.getDisplay(shell).asyncExec(new Runnable() {
			public void run() {
				Shell s = shell;
				if (s == null) {
					s = StatetPlugin.getActiveWorkbenchShell();
				}
				ErrorDialog.openError(s, StatetMessages.ErrorDialog_title, message, status);
			}
		});
	}

	private static void displayMessageDialog(String exceptionMessage, final Shell shell, String message) {
		
		StringWriter msg = new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
		}
		if (exceptionMessage == null || exceptionMessage.length() == 0) {
			msg.write("No details available. See error log."); 
		} 
		else {
			msg.write(exceptionMessage);
		}
		final String finalMessage = msg.toString();
		
		StatetPlugin.getDisplay(shell).asyncExec(new Runnable() {
			public void run() {
				Shell s = shell;
				if (s == null) {
					s = StatetPlugin.getActiveWorkbenchShell();
				}
				MessageDialog.openError(s, StatetMessages.ErrorDialog_title, finalMessage);			
			}
		});
	}

	
	public static class StatusHandler implements IStatusHandler {

		
		public Object handleStatus(IStatus status, Object source) throws CoreException {
			
			handle(status);
			return Boolean.TRUE;
		}

	}
}
