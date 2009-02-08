/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching.winrgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.ui.util.DNDUtil;

import de.walware.statet.r.launching.IRCodeLaunchConnector;


public class RGWLauncher implements IRCodeLaunchConnector {

	
	private static BooleanPref PREF_SUBMIT_DIRECTLY_ENABLED = new BooleanPref(
			WinRGuiConnectorPlugin.ID, "submit_directly.enabled"); //$NON-NLS-1$
	
	
	private Clipboard fClipboard;
	private boolean fSubmitDirectly;
	private String fExecutable;
	
	public RGWLauncher() throws CoreException {
		
		URL dir = WinRGuiConnectorPlugin.getDefault().getBundle().getEntry("/win32/RGWConnector.exe"); //$NON-NLS-1$
		fSubmitDirectly = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_SUBMIT_DIRECTLY_ENABLED);
		try {
			String local = FileLocator.toFileURL(dir).getPath();
			File file = new File(local);
			if (!file.exists())
				throw new IOException("Missing File '"+file.getAbsolutePath() + "'.");
			fExecutable = file.getAbsolutePath();
		} catch (IOException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					WinRGuiConnectorPlugin.ID,
					-1,
					"Error Loading R-GUI-Windows-Connector:",
					e));
		}
	}

	private enum SubmitType { DONOTHING, SUBMITINPUT, PASTECLIPBOARD };
	
	public boolean submit(final String[] rCommands, boolean gotoConsole) throws CoreException {
		// goto option not implemented (requires extension of .net-code)
		
		final SubmitType type;
		if (rCommands.length == 0)
			type = SubmitType.DONOTHING;
		else if (fSubmitDirectly && rCommands.length == 1)
			type = SubmitType.SUBMITINPUT;
		else {
			if (!copyToClipboard(rCommands))
				return false;
			type = SubmitType.PASTECLIPBOARD;
		}

		doRunConnector(type, (type == SubmitType.SUBMITINPUT) ? rCommands : null);
		return true;
//		StringBuilder rCmd = new StringBuilder();
//		for (int i = 0; i < rCommands.length; i++) {
//			rCmd.append(rCommands[i].replace("\"", "\\\""));
//			rCmd.append("\n");
//		}
//		cmd[1]  = rCmd.toString();
	}

	public void gotoConsole() throws CoreException {
		
		doRunConnector(SubmitType.DONOTHING, null);
	}
	
	private void doRunConnector(SubmitType connectorCmd, final String[] writeToProcess) throws CoreException {

		final String[] processCmd = new String[] {
			fExecutable, connectorCmd.toString().toLowerCase() };
		final AtomicReference<Process> process = new AtomicReference<Process>();
		
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		
		IRunnableWithProgress runnable = new IRunnableWithProgress(){
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					Process p = DebugPlugin.exec(processCmd, null);
					process.set(p);
					
					if (writeToProcess != null) {
						writeTextToProcess(p, writeToProcess);
					}
					
					int exitCode = p.waitFor();
					String message = null;
					switch (exitCode) {
					case 0:
						// ok
						break;
					
					case 100: {
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							message = reader.readLine();
							if (message == null)
								message = "Unable to detect Error";
						}
						catch (Exception e) {
							message = "Unable to detect Error";
						}
						finally {
							if (reader != null)
								try {
									reader.close();
								} catch (Exception e) {};
						}
						}
						break;
					
					default:
						message = "Unknown Error";
						break;
						
					}
					
					if (message != null)
						throw new CoreException(new Status(
								IStatus.ERROR,
								WinRGuiConnectorPlugin.ID,
								-1,
								"Error when running RGui-Connector: \n"+message,
								null));
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		
		try {
			progressService.busyCursorWhile(runnable);

		} catch (InvocationTargetException e1) {
			Throwable cause = e1.getCause();
			if (cause instanceof CoreException)
				throw (CoreException) cause;
			else
				throw new CoreException(new Status(
						IStatus.ERROR,
						WinRGuiConnectorPlugin.ID,
						-1,
						"Unknown Error occured when running R-Gui-Connector",
						e1));
		} catch (InterruptedException e1) {
			Process p = process.get();
			if (p != null) {
				try {
					p.destroy();
				} catch (Exception e) { }
			}
		}
	}
	
	
	private void writeTextToProcess(Process process, String[] text) {
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
			for (int i = 0; i < text.length; i++) {
				writer.println(text[i]);
			}
		}
		catch (Exception e) {
			
		}
		finally {
			if (writer != null)
				try {
					writer.close();
				} catch (Exception e) {};
		}
	}
	
	private boolean copyToClipboard(String[] text) {
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length; i++) {
			builder.append(text[i]);
			builder.append("\n");
		}
		
		if (fClipboard == null)
			fClipboard = new Clipboard(Display.getCurrent());
		
		return DNDUtil.setContent(fClipboard,
				new String[] { builder.toString() },
				new Transfer[] { TextTransfer.getInstance() } );
	}
}
