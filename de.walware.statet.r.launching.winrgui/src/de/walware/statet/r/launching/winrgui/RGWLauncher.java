/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.launching.winrgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
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
import de.walware.ecommons.preferences.core.Preference.BooleanPref;
import de.walware.ecommons.ui.util.DNDUtil;

import de.walware.statet.r.launching.IRCodeSubmitConnector;


public class RGWLauncher implements IRCodeSubmitConnector {
	
	
	private static BooleanPref PREF_SUBMIT_DIRECTLY_ENABLED = new BooleanPref(
			WinRGuiConnectorPlugin.PLUGIN_ID, "submit_directly.enabled"); //$NON-NLS-1$
	
	
	private Clipboard fClipboard;
	private boolean fSubmitDirectly;
	private String fExecutable;
	
	
	public RGWLauncher() throws CoreException {
		
		final URL dir = WinRGuiConnectorPlugin.getDefault().getBundle().getEntry("/win32/RGWConnector.exe"); //$NON-NLS-1$
		fSubmitDirectly = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_SUBMIT_DIRECTLY_ENABLED);
		try {
			final String local = FileLocator.toFileURL(dir).getPath();
			final File file = new File(local);
			if (!file.exists()) {
				throw new IOException("Missing File '"+file.getAbsolutePath() + "'.");
			}
			fExecutable = file.getAbsolutePath();
		} catch (final IOException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					WinRGuiConnectorPlugin.PLUGIN_ID,
					-1,
					"Error Loading R-GUI-Windows-Connector:",
					e));
		}
	}
	
	private enum SubmitType { DONOTHING, SUBMITINPUT, PASTECLIPBOARD };
	
	@Override
	public boolean submit(final List<String> lines, final boolean gotoConsole) throws CoreException {
		// goto option not implemented (requires extension of .net-code)
		final SubmitType type;
		if (lines.isEmpty()) {
			type = SubmitType.DONOTHING;
		}
		else if (fSubmitDirectly && lines.size() == 1) {
			type = SubmitType.SUBMITINPUT;
		}
		else {
			if (!copyToClipboard(lines)) {
				return false;
			}
			type = SubmitType.PASTECLIPBOARD;
		}
		
		doRunConnector(type, (type == SubmitType.SUBMITINPUT) ? lines : null);
		return true;
//		StringBuilder rCmd = new StringBuilder();
//		for (int i = 0; i < rCommands.length; i++) {
//			rCmd.append(rCommands[i].replace("\"", "\\\""));
//			rCmd.append("\n");
//		}
//		cmd[1]  = rCmd.toString();
	}
	
	@Override
	public void gotoConsole() throws CoreException {
		doRunConnector(SubmitType.DONOTHING, null);
	}
	
	private void doRunConnector(final SubmitType connectorCmd, final List<String> writeToProcess) throws CoreException {
		final String[] processCmd = new String[] {
			fExecutable, connectorCmd.toString().toLowerCase() };
		final AtomicReference<Process> process= new AtomicReference<>();
		
		final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		
		final IRunnableWithProgress runnable = new IRunnableWithProgress(){
			@Override
			public void run(final IProgressMonitor monitor) throws InvocationTargetException {
				try {
					final Process p = DebugPlugin.exec(processCmd, null);
					process.set(p);
					
					if (writeToProcess != null) {
						writeTextToProcess(p, writeToProcess);
					}
					
					final int exitCode = p.waitFor();
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
							if (message == null) {
								message = "Unable to detect Error";
							}
						}
						catch (final Exception e) {
							message = "Unable to detect Error";
						}
						finally {
							if (reader != null) {
								try {
									reader.close();
								}
								catch (final Exception e) {}
							}
						}
						break; }
					
					default:
						message = "Unknown Error";
						break;
						
					}
					
					if (message != null) {
						throw new CoreException(new Status(
								IStatus.ERROR,
								WinRGuiConnectorPlugin.PLUGIN_ID,
								-1,
								"Error when running RGui-Connector: \n"+message,
								null));
					}
				}
				catch (final Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		
		try {
			progressService.busyCursorWhile(runnable);
		}
		catch (final InvocationTargetException e1) {
			final Throwable cause = e1.getCause();
			if (cause instanceof CoreException) {
				throw (CoreException) cause;
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, WinRGuiConnectorPlugin.PLUGIN_ID, -1,
						"Unknown Error occured when running R-Gui-Connector", e1));
			}
		}
		catch (final InterruptedException e1) {
			final Process p = process.get();
			if (p != null) {
				try {
					p.destroy();
				}
				catch (final Exception e) { }
			}
		}
	}
	
	
	private void writeTextToProcess(final Process process, final List<String> text) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
			for (int i = 0; i < text.size(); i++) {
				writer.println(text.get(i));
			}
		}
		catch (final Exception e) {
			
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final Exception e) {}
			}
		}
	}
	
	private boolean copyToClipboard(final List<String> lines) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			builder.append(lines.get(i));
			builder.append("\n"); //$NON-NLS-1$
		}
		
		if (fClipboard == null) {
			fClipboard = new Clipboard(Display.getCurrent());
		}
		
		return DNDUtil.setContent(fClipboard,
				new String[] { builder.toString() },
				new Transfer[] { TextTransfer.getInstance() } );
	}
	
}
