/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ICommonStatusConstants;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.nico.ISetupRAdapter;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.ui.tools.RQuitRunnable;
import de.walware.statet.r.ui.RUI;


public class RTermController
		extends AbstractRController<IBasicRAdapter, RWorkspace> {

	
	private class RTermAdapter extends AbstractRAdapter implements IBasicRAdapter, ISetupRAdapter, IAdaptable {
		
		@Override
		protected void doBeforeSubmit(String input) {
			SubmitType type = fCurrentRunnable.getSubmitType();
			
			try {
				fProcessOutputThread.streamLock.lock();
				fInputStream.append(input, type,
						(fPrompt.meta & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) );
				fInputStream.append(fWorkspaceData.getLineSeparator(), type,
						IToolRunnableControllerAdapter.META_HISTORY_DONTADD);
			}
			finally {
				fProcessOutputThread.streamLock.unlock();
			}
		}

		@Override
		protected Prompt doSubmit(String input, IProgressMonitor monitor) {
			String completeInput = input;
			if ((fPrompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
				completeInput = ((IncompleteInputPrompt) fPrompt).previousInput + input;
			}
			monitor.subTask(fDefaultPrompt.text + " " + completeInput);  //$NON-NLS-1$
			
			try {
				fProcessInputWriter.write(input + fLineSeparator);
				fProcessInputWriter.flush();
			}
			catch (IOException e) {
				RUIPlugin.logError(-1, "Rterm IO error", e); //$NON-NLS-1$
				if (!isToolAlive()) {
					markAsTerminated();
					return Prompt.NONE;
				}
			}

			try {
				Thread.sleep(fProcessOutputThread.SYNC_MS*2);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			fProcessOutputThread.streamLock.lock();
			fProcessOutputThread.streamLock.unlock();

			return fDefaultPrompt;
		}
	}
	
	private class ReadThread extends Thread {
		
		volatile int hasNoOutput;
		private int SYNC_COUNT = 2;
		private int SYNC_MS = 33;

		final Lock streamLock = new ReentrantLock();
		
		public ReadThread() {
			super("Rterm-Output Monitor"); //$NON-NLS-1$
		}
		
		@Override
		public void run() {
			boolean locked = false;
			try {
				boolean canRead = false;
				char[] b = new char[1024];
				while (fProcess != null | (canRead = fProcessOutputReader.ready())) {
					fProcessOutputBuffer.available();
					if (canRead || hasNoOutput > SYNC_COUNT) {
						if (!canRead && locked) {
							streamLock.unlock();
							locked = false;
						}
						int n = fProcessOutputReader.read(b);
						if (n > 0) {
							hasNoOutput = 0;
							if (!locked) {
								streamLock.lock();
								locked = true;
							}
							String s = new String(b, 0, n);
							fDefaultOutputStream.append(s, SubmitType.CONSOLE, 0);
							n = s.length();
							if (n >= 2 && s.charAt(--n) == ' ' && (s.charAt(--n) == '>' || s.charAt(n) == '+')) {
								hasNoOutput++;
								getControllerThread().interrupt();
							}
							continue;
						}
						else if (n < 0) {
							onRTerminated();
							return;
						}
					}
					try {
						Thread.sleep(SYNC_MS);
						hasNoOutput++;
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
				}
			}
			catch (IOException e) {
				onRTerminated();
				return;
			}
			finally {
				if (locked) {
					streamLock.unlock();
					locked = false;
				}
				try {
					fProcessOutputReader.close();
				} catch (IOException e1) {
				}
			}
		}
		
		private void onRTerminated() {
			markAsTerminated();
			RTermController.this.resume();
		}
	}
	
	
	private ProcessBuilder fConfig;
	private Charset fCharset;
	private Process fProcess;
	private OutputStreamWriter fProcessInputWriter;
	private BufferedInputStream fProcessOutputBuffer;
	private InputStreamReader fProcessOutputReader;
	private ReadThread fProcessOutputThread;
	

	public RTermController(ToolProcess process, ProcessBuilder config, Charset charset) {
		super(process);
		fConfig = config;
		fCharset = charset;
		
		fWorkspaceData = new RWorkspace(this);
		fRunnableAdapter = new RTermAdapter();
		((ISetupRAdapter) fRunnableAdapter).setWorkspaceDir(EFS.getLocalFileSystem().fromLocalFile(config.directory()));
	}
	
	@Override
	protected void startTool(IProgressMonitor monitor) throws CoreException {
		OutputStream processInput = null;
		InputStream processOutput;
		try {
			fConfig.redirectErrorStream(true);
			fProcess = fConfig.start();
			processOutput = fProcess.getInputStream();
			if (processOutput instanceof BufferedInputStream) {
				fProcessOutputBuffer = (BufferedInputStream) processOutput;
			}
			fProcessOutputReader = new InputStreamReader(processOutput, fCharset);
			fProcessOutputThread = new ReadThread();
			fProcessOutputThread.start();
			processInput = fProcess.getOutputStream();
			fProcessInputWriter = new OutputStreamWriter(processInput, fCharset);
		} catch (IOException e) {
			if (processInput != null) {
				try {
					processInput.close();
				} catch (IOException e1) {
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHING_ERROR,
				RLaunchingMessages.RTerm_error_Starting_message, e));
		}
		
	}
	
	@Override
	protected IToolRunnable createQuitRunnable() {
		return new RQuitRunnable();
	}
	
	@Override
	public boolean cancelTask() {
		// Rterm is stupid, ignore status
		return runSendCtrlC();
	}
	
	@Override
	protected void interruptTool(int hardness) {
		runSendCtrlC();
		if (hardness == 0) {
			return;
		}
		getControllerThread().interrupt();
	}
	
	@Override
	protected void killTool(IProgressMonitor monitor) {
		Process p = fProcess;
		if (p != null) {
			p.destroy();
			fProcess = null;
		}
		markAsTerminated();
	}
	
	@Override
	protected boolean isToolAlive() {
		Process p = fProcess;
		if (p != null) {
			try {
				p.exitValue();
			}
			catch (IllegalStateException e) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void finishTool() {
		fProcess = null;
		// save history
		super.finishTool();
	}
	
	
	private boolean runSendCtrlC() {
		if (!Platform.getOS().startsWith("win") //$NON-NLS-1$
				|| getStatus() == ToolStatus.TERMINATED) { 
			return false;
		}
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				URL dir = RUIPlugin.getDefault().getBundle().getEntry("/win32/SendSignal.exe"); //$NON-NLS-1$
				try {
					monitor.beginTask(RLaunchingMessages.RTerm_CancelTask_label, 10);
					String local = FileLocator.toFileURL(dir).getPath();
					File file = new File(local);
					if (!file.exists())
						throw new IOException("Missing File '"+file.getAbsolutePath() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
					monitor.worked(1);
					String[] cmd = new String[] {
							"cmd", "/S", "/C",  									//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							"\"FOR /F \"tokens=2\" %I IN ('TASKLIST /FI \"IMAGENAME eq Rterm.exe\" /NH') " + //$NON-NLS-1$
							// does not work in all languages: /FI \"STATUS eq running\"
							"DO \"" + file.getCanonicalPath() + "\" %I \"" , 		//$NON-NLS-1$ //$NON-NLS-2$
							">", "NULL" }; 										//$NON-NLS-1$ //$NON-NLS-2$
					Process process = Runtime.getRuntime().exec(cmd);
					monitor.worked(1);
					while (true) {
						try {
							int code = process.exitValue();
							if (code != 0) {
								throw new IOException("Command failed: " + Arrays.toString(cmd));
							}
							break;
						}
						catch (IllegalThreadStateException e) {
						}
						if (monitor.isCanceled()) {
							process.destroy();
							StatusManager.getManager().handle(new Status(
									IStatus.WARNING, RUI.PLUGIN_ID, -1, "Sending CTRL+C to R process canceled, command: " + Arrays.toString(cmd), null)); //$NON-NLS-1$
							break;
						}
						try {
							Thread.sleep(50);
						}
						catch (InterruptedException e) {
							Thread.interrupted();
						}
					}
					runOnIdle(createCommandRunnable("", SubmitType.OTHER)); //$NON-NLS-1$
				} 
				catch (IOException e) {
					StatusManager.getManager().handle(new Status(
							IStatus.ERROR, RUI.PLUGIN_ID, -1, "Error Sending CTRL+C to R process.", e)); //$NON-NLS-1$
					throw new InvocationTargetException(null);
				}
				finally {
					monitor.done();
				}
			}
		};

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(runnable);
			return true;
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
		return false;
	}
	
}
