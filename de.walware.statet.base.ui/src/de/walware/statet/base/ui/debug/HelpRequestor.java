/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.debug;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ui.util.PixelConverter;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetMessages;
import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Shows the help for a command line tool in a dialog tray.
 */
public class HelpRequestor implements IRunnableWithProgress {

	
	private static class InfoTray extends DialogTray{
		
		private TrayDialog fDialog;
		private Text fTextControl;
		private Label fCmdInfo;
		
		private InfoTray(TrayDialog dialog) {
			
			fDialog = dialog;
		}
		
		@Override
		protected Control createContents(Composite parent) {
			
			final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
			
			Composite container = new Composite(parent, SWT.NONE);
			container.addListener(SWT.Dispose, new Listener() {
				public void handleEvent(Event event) {
					toolkit.dispose();
				}
			});
			container.setLayout(new FillLayout(SWT.VERTICAL));

			Form form = toolkit.createForm(container);
			toolkit.decorateFormHeading(form);
			form.setText("'--help'"); //$NON-NLS-1$
			form.getToolBarManager().add(new ContributionItem() {
				@Override
				public void fill(ToolBar parent, int index) {
					ToolItem item = new ToolItem(parent, SWT.PUSH);
					item.setText(StatetMessages.HelpRequestor_Close_name);
					item.setToolTipText(StatetMessages.HelpRequestor_Close_tooltip);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							fDialog.closeTray();
							fDialog.getShell().setFocus();
						}
					});
				}
			});
			form.getToolBarManager().update(true);
			Composite content = form.getBody();
			content.setLayout(new GridLayout());
			
			fCmdInfo = toolkit.createLabel(content, ">"); //$NON-NLS-1$
			fCmdInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			fTextControl = toolkit.createText(content, "", SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL); //$NON-NLS-1$
			fTextControl.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.widthHint = new PixelConverter(fTextControl).convertWidthInCharsToPixels(50);
			fTextControl.setLayoutData(gd);
			
			return container;
		}
		
		public void update(String cmdInfo, String text) {
			
			fCmdInfo.setText(cmdInfo);
			fTextControl.setText(text);
		}
		
	}
	
	public static void closeHelpTray(TrayDialog dialog) {
		if (dialog.getTray() instanceof HelpRequestor.InfoTray) {
			dialog.closeTray();
		}
	}

	
	private class HelpReader extends Thread {
		
		private InputStreamReader fOutputInput;
		private StringBuilder fBuffer;
		private Exception fReadException;

		public HelpReader() {
			super("'--help'-Output Monitor"); //$NON-NLS-1$
			fOutputInput = new InputStreamReader(fProcess.getInputStream());
			fBuffer = new StringBuilder();
		}
		
		@Override
		public void run() {
			try {
				boolean canRead;
				char[] b = new char[512];
				while (fIsRunning | (canRead = fOutputInput.ready())) {
					if (fMonitor.isCanceled()) {
						fProcess.destroy();
						return;
					}
					if (canRead) {
						int n = fOutputInput.read(b);
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
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
				}
			}
			catch (IOException e) {
			}
			finally {
				try {
					fOutputInput.close();
				} catch (IOException e1) {
				}
			}
		}
		
		public String getText() throws CoreException {
			while (true) {
				try {
					join();
					if (fReadException != null) {
						throw new CoreException(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
								StatetMessages.HelpRequestor_error_WhenReadOutput_message, fReadException));
					}
					return fBuffer.toString();
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		}
	}
	

	private TrayDialog fDialog;
	private IProgressMonitor fMonitor;
	private ProcessBuilder fBuilder;
	private Process fProcess;
	private boolean fIsRunning;
	
	private List<String> fCmdLine;
	
	
	public HelpRequestor(List<String> cmdLine, TrayDialog dialog) {

		fCmdLine = cmdLine;
		fBuilder = new ProcessBuilder(cmdLine);
		fDialog = dialog;
	}

	public ProcessBuilder getProcessBuilder() {
		
		return fBuilder;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		
		final String cmdInfo = LaunchConfigUtil.generateCommandLine(fCmdLine);
		fMonitor = monitor;
		fMonitor.beginTask(StatetMessages.HelpRequestor_Task_name+cmdInfo, 10);
		if (fMonitor.isCanceled()) {
			return;
		}
		try {
			fBuilder.redirectErrorStream(true);
			fMonitor.worked(1);
			try {
				fProcess = fBuilder.start();
			}
			catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING_ERROR,
						NLS.bind(StatetMessages.HelpRequestor_error_WhenRunProcess_message, cmdInfo), e));
			}
			fIsRunning = true;
			fMonitor.worked(2);
			
			HelpReader reader = new HelpReader();
			reader.start();
			fMonitor.worked(1);
			while (fIsRunning) {
				try {
					fProcess.waitFor();
					fIsRunning = false;
				}
				catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
			fMonitor.worked(2);

			final String helpText = reader.getText();
			fMonitor.worked(2);
			
			UIAccess.getDisplay().asyncExec(new Runnable() {
				public void run() {
					InfoTray tray = null;
					if (fDialog.getTray() instanceof InfoTray) {
						tray = (InfoTray) fDialog.getTray();
					}
					else {
						if (fDialog.getTray() != null) {
							fDialog.closeTray();
						}
						tray = new InfoTray(fDialog);
						fDialog.openTray(tray);
					}
					tray.update(cmdInfo, helpText);
				}
			});
		}
		catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
		finally {
			fMonitor.done();
		}
	}
	
}
