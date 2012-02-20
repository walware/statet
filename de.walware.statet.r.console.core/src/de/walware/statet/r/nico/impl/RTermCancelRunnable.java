/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.console.core.RConsoleCorePlugin;
import de.walware.statet.r.internal.nico.RNicoMessages;


/**
 * Cancel support for windows/rterm.
 */
class RTermCancelRunnable implements IToolRunnable {
	
	
	RTermCancelRunnable() {
	}
	
	
	@Override
	public String getLabel() {
		return RNicoMessages.RTerm_CancelTask_label;
	}
	
	@Override
	public String getTypeId() {
		return null; // not a real runnable
	}
	
	@Override
	public boolean changed(final int event, final ITool process) {
		return true;
	}
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return true;
	}
	
	@Override
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final IRBasicAdapter r = (IRBasicAdapter) service;
		final IREnvConfiguration config = (IREnvConfiguration) r.getTool().getAdapter(IREnvConfiguration.class);
		String arch = config.getSubArch();
		if (arch == null) {
			arch = Platform.getOSArch();
		}
		try {
			monitor.beginTask(RNicoMessages.RTerm_CancelTask_SendSignal_label, 10);
			URL url = RConsoleCorePlugin.getDefault().getBundle().getEntry(
					"/win32/" + arch + "/sendsignal.exe"); //$NON-NLS-1$ //$NON-NLS-2$
			if (url == null) {
				throw new IOException("Missing 'sendsignal' tool for arch '" +  arch + "'."); //$NON-NLS-1$
			}
			url = FileLocator.toFileURL(url);
			final File file = new File(url.getPath());
			if (!file.exists()) {
				throw new IOException("Missing file '"+url.toExternalForm()+ "'."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			monitor.worked(1);
			final RTermController controller = (RTermController) r.getController();
			final Long processId = controller.fProcessId;
			if (processId == null) {
				RConsoleCorePlugin.log(new Status(IStatus.WARNING, RConsoleCorePlugin.PLUGIN_ID,
						"Cannot run cancel command: process-id of Rterm process is missing." )); //$NON-NLS-1$
				return;
			}
			final String[] cmd = new String[] {
					file.getAbsolutePath(), processId.toString()
					// the tool usually does not print output
			};
			final Process process = Runtime.getRuntime().exec(cmd);
			monitor.worked(1);
			while (true) {
				try {
					final int code = process.exitValue();
					if (code != 0) {
						final StringBuilder detail = new StringBuilder("Command failed:"); //$NON-NLS-1$
						detail.append("\n command = "); //$NON-NLS-1$
						detail.append(Arrays.toString(cmd));
						detail.append("\n os.name = "); //$NON-NLS-1$
						detail.append(System.getProperty("os.name")); //$NON-NLS-1$
						detail.append("\n os.version = "); //$NON-NLS-1$
						detail.append(System.getProperty("os.version")); //$NON-NLS-1$
						detail.append("\n os.arch = "); //$NON-NLS-1$
						detail.append(System.getProperty("os.arch")); //$NON-NLS-1$
						detail.append("\n r.arch = "); //$NON-NLS-1$
						detail.append(arch); //$NON-NLS-1$
						detail.append("\n exit.code = 0x"); //$NON-NLS-1$
						detail.append(Integer.toHexString(code));
						throw new IOException(detail.toString());
					}
					break;
				}
				catch (final IllegalThreadStateException e) {
				}
				if (monitor.isCanceled()) {
					process.destroy();
					RConsoleCorePlugin.log(new Status(IStatus.WARNING, RConsoleCorePlugin.PLUGIN_ID, -1,
							"Sending CTRL+C to R process canceled, command: " + Arrays.toString(cmd), null )); //$NON-NLS-1$
					break;
				}
				try {
					Thread.sleep(50);
				}
				catch (final InterruptedException e) {
					// continue directly
				}
			}
		}
		catch (final IOException e) {
			throw new CoreException(new Status(IStatus.WARNING, RConsoleCorePlugin.PLUGIN_ID, -1,
					"Error Sending CTRL+C to R process.", e) ); //$NON-NLS-1$
		}
		finally {
			monitor.done();
		}
	}
	
}
