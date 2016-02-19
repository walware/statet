/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.ui;

import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.core.util.LaunchUtils;
import de.walware.ecommons.debug.internal.ui.Messages;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Utility class collecting the output of a process.
 */
public class ProcessOutputCollector extends Thread {
	
	
	private final String fName;
	
	private final ProcessBuilder fProcessBuilder;
	private Process fProcess;
	
	private boolean fIsRunning;
	private final IProgressMonitor fMonitor;
	
	private InputStreamReader fOutputInput;
	private final StringBuilder fBuffer;
	private Exception fReadException;
	
	
	public ProcessOutputCollector(final ProcessBuilder processBuilder, final String name, final IProgressMonitor monitor) {
		super(name+"-Output Monitor"); //$NON-NLS-1$
		fProcessBuilder = processBuilder;
		fName = name;
		fMonitor = monitor;
		fBuffer = new StringBuilder();
	}
	
	
	public String collect() throws CoreException {
		try {
			fProcessBuilder.redirectErrorStream(true);
			fProcess = fProcessBuilder.start();
			fOutputInput = new InputStreamReader(fProcess.getInputStream());
		}
		catch (final IOException e) {
			final String cmdInfo = LaunchUtils.generateCommandLine(fProcessBuilder.command());
			throw new CoreException(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
					NLS.bind(Messages.HelpRequestor_error_WhenRunProcess_message, fName, cmdInfo), e));
		}
		fIsRunning = true;
		fMonitor.worked(2);
		
		start();
		fMonitor.worked(1);
		
		while (fIsRunning) {
			try {
				fProcess.waitFor();
				fIsRunning = false;
			}
			catch (final InterruptedException e) {
				// forward to reader
				interrupt();
			}
		}
		fMonitor.worked(2);
		
		while (true) {
			try {
				join();
				if (fReadException != null) {
					throw new CoreException(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
							NLS.bind(Messages.HelpRequestor_error_WhenReadOutput_message, fName), fReadException));
				}
				fMonitor.worked(2);
				return fBuffer.toString();
			}
			catch (final InterruptedException e) {
				// forward to reader
				interrupt();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			boolean canRead;
			final char[] b = new char[512];
			while (fIsRunning | (canRead = fOutputInput.ready())) {
				if (fMonitor.isCanceled()) {
					fProcess.destroy();
					return;
				}
				if (canRead) {
					final int n = fOutputInput.read(b);
					if (n > 0) {
						fBuffer.append(b, 0, n);
						continue;
					}
					if (n < 0) {
						return;
					}
				}
				try {
					Thread.sleep(50);
				}
				catch (final InterruptedException e) {
					// continue loop, monitor is checked
				}
			}
		}
		catch (final IOException e) {
			fReadException = e;
		}
		finally {
			try {
				fOutputInput.close();
			} catch (final IOException e1) {}
		}
	}
	
}
