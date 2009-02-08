/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.ui;

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

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.internal.ui.Messages;
import de.walware.ecommons.ui.util.PixelConverter;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.StatetImages;


/**
 * Shows the help for a command line tool in a dialog tray.
 */
public class HelpRequestor implements IRunnableWithProgress {
	
	
	private static class InfoTray extends DialogTray{
		
		private TrayDialog fDialog;
		private Text fTextControl;
		private Label fCmdInfo;
		
		private InfoTray(final TrayDialog dialog) {
			fDialog = dialog;
		}
		
		@Override
		protected Control createContents(final Composite parent) {
			final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
			
			final Composite container = new Composite(parent, SWT.NONE);
			container.addListener(SWT.Dispose, new Listener() {
				public void handleEvent(final Event event) {
					toolkit.dispose();
				}
			});
			container.setLayout(new FillLayout(SWT.VERTICAL));
			
			final Form form = toolkit.createForm(container);
			toolkit.decorateFormHeading(form);
			form.setText("'--help'"); //$NON-NLS-1$
			form.getToolBarManager().add(new ContributionItem() {
				@Override
				public void fill(final ToolBar parent, final int index) {
					final ToolItem item = new ToolItem(parent, SWT.PUSH);
					item.setImage(StatetImages.getImage(StatetImages.LOCTOOL_CLOSETRAY));
					item.setHotImage(StatetImages.getImage(StatetImages.LOCTOOL_CLOSETRAY_H));
					item.setToolTipText(Messages.HelpRequestor_Close_tooltip);
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							fDialog.closeTray();
							fDialog.getShell().setFocus();
						}
					});
				}
			});
			form.getToolBarManager().update(true);
			final Composite content = form.getBody();
			content.setLayout(new GridLayout());
			
			fCmdInfo = toolkit.createLabel(content, ">"); //$NON-NLS-1$
			fCmdInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			fTextControl = toolkit.createText(content, "", SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL); //$NON-NLS-1$
			fTextControl.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.widthHint = new PixelConverter(fTextControl).convertWidthInCharsToPixels(50);
			fTextControl.setLayoutData(gd);
			
			return container;
		}
		
		public void update(final String cmdInfo, final String text) {
			fCmdInfo.setText(cmdInfo);
			fTextControl.setText(text);
		}
		
	}
	
	public static void closeHelpTray(final TrayDialog dialog) {
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
					} catch (final InterruptedException e) {
						Thread.interrupted();
					}
				}
			}
			catch (final IOException e) {
			}
			finally {
				try {
					fOutputInput.close();
				} catch (final IOException e1) {
				}
			}
		}
		
		public String getText() throws CoreException {
			while (true) {
				try {
					join();
					if (fReadException != null) {
						throw new CoreException(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
								Messages.HelpRequestor_error_WhenReadOutput_message, fReadException));
					}
					return fBuffer.toString();
				} catch (final InterruptedException e) {
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
	
	
	public HelpRequestor(final List<String> cmdLine, final TrayDialog dialog) {
		fCmdLine = cmdLine;
		fBuilder = new ProcessBuilder(cmdLine);
		fDialog = dialog;
	}
	
	
	public ProcessBuilder getProcessBuilder() {
		return fBuilder;
	}
	
	public void run(final IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		final String cmdInfo = LaunchConfigUtil.generateCommandLine(fCmdLine);
		fMonitor = monitor;
		fMonitor.beginTask(Messages.HelpRequestor_Task_name+cmdInfo, 10);
		if (fMonitor.isCanceled()) {
			return;
		}
		try {
			fBuilder.redirectErrorStream(true);
			fMonitor.worked(1);
			try {
				fProcess = fBuilder.start();
			}
			catch (final IOException e) {
				throw new CoreException(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
						NLS.bind(Messages.HelpRequestor_error_WhenRunProcess_message, cmdInfo), e));
			}
			fIsRunning = true;
			fMonitor.worked(2);
			
			final HelpReader reader = new HelpReader();
			reader.start();
			fMonitor.worked(1);
			while (fIsRunning) {
				try {
					fProcess.waitFor();
					fIsRunning = false;
				}
				catch (final InterruptedException e) {
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
		catch (final CoreException e) {
			throw new InvocationTargetException(e);
		}
		finally {
			fMonitor.done();
		}
	}
	
}
