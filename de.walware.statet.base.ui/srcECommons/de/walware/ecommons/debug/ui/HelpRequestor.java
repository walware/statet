/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
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

import de.walware.ecommons.debug.internal.ui.Messages;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.UIAccess;


/**
 * Shows the help for a command line tool in a dialog tray.
 */
public class HelpRequestor implements IRunnableWithProgress {
	
	
	private static class InfoTray extends DialogTray{
		
		private final TrayDialog fDialog;
		
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
				@Override
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
					item.setImage(SharedUIResources.getImages().get(SharedUIResources.LOCTOOL_CLOSETRAY_IMAGE_ID));
					item.setHotImage(SharedUIResources.getImages().get(SharedUIResources.LOCTOOL_CLOSETRAY_H_IMAGE_ID));
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
	
	
	private final TrayDialog fDialog;
	
	private final ProcessBuilder fBuilder;
	
	boolean fIsRunning;
	
	
	public HelpRequestor(final ProcessBuilder processBuilder, final TrayDialog dialog) {
		fBuilder = processBuilder;
		fDialog = dialog;
	}
	
	
	public ProcessBuilder getProcessBuilder() {
		return fBuilder;
	}
	
	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException {
		final String cmdInfo = LaunchConfigUtil.generateCommandLine(fBuilder.command());
		monitor.beginTask(Messages.HelpRequestor_Task_name+cmdInfo, 10);
		if (monitor.isCanceled()) {
			return;
		}
		try {
			fBuilder.redirectErrorStream(true);
			monitor.worked(1);
			
			final ProcessOutputCollector reader = new ProcessOutputCollector(fBuilder, "'--help'", monitor);
			final String helpText = reader.collect();
			
			UIAccess.getDisplay().asyncExec(new Runnable() {
				@Override
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
			monitor.done();
		}
	}
	
}
