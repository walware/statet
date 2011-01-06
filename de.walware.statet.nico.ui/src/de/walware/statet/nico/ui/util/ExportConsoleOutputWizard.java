/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.NicoVariables;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.TrackWriter;
import de.walware.statet.nico.core.util.TrackingConfiguration;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.console.NIConsoleOutputStream;
import de.walware.statet.nico.ui.console.NIConsolePage;


/**
 * Wizard to export the console output
 */
public class ExportConsoleOutputWizard extends Wizard {
	
	
	private static final String FILE_HISTORY_SETTINGSKEY = "FileLocation_history";
	
	
	protected static class ConfigurationPage extends WizardPage {
		
		
		private NIConsolePage fConsolePage;
		
		private TrackingConfigurationComposite fConfigControl;
		private Button fOpenControl;
		
		private TrackingConfiguration fConfig;
		private WritableValue fOpenValue;
		
		private DataBindingContext fDbc;
		
		
		
		public ConfigurationPage(final NIConsolePage page, final TrackingConfiguration config, final boolean selectionMode) {
			super("ConfigureConsoleExportPage"); //$NON-NLS-1$
			fConsolePage = page;
			setTitle(selectionMode ? "Export Selected Output" : "Export Current Output");
			setDescription("Select the content to export and the destination file");
			
			fConfig = config;
			fOpenValue = new WritableValue(false, Boolean.class);
		}
		
		public void createControl(final Composite parent) {
			initializeDialogUnits(parent);
			
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyContentDefaults(new GridLayout(), 1));
			
			fConfigControl = createTrackingControl(composite);
			fConfigControl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			fConfigControl.getPathInput().getValidator().setOnLateResolve(IStatus.ERROR);
			fConfigControl.getPathInput().setShowInsertVariable(true,
					DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS,
					fConsolePage.getTool().getWorkspaceData().getStringVariables() );
			fConfigControl.setInput(fConfig);
			
			final Composite additionalOptions = createAdditionalOptions(composite);
			additionalOptions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			LayoutUtil.addSmallFiller(composite, true);
			final ToolInfoGroup info = new ToolInfoGroup(composite, fConsolePage.getTool());
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			Dialog.applyDialogFont(composite);
			setControl(composite);
			
			fConfigControl.getPathInput().setHistory(getDialogSettings().getArray(FILE_HISTORY_SETTINGSKEY));
			final Realm realm = Realm.getDefault();
			fDbc = new DataBindingContext(realm);
			addBindings(fDbc, realm);
			WizardPageSupport.create(this, fDbc);
		}
		
		protected TrackingConfigurationComposite createTrackingControl(final Composite parent) {
			return new TrackingConfigurationComposite(parent) {
				@Override
				protected boolean enableFullMode() {
					return false;
				}
				@Override
				protected boolean enableFilePathAsCombo() {
					return true;
				}
			};
		}
		
		protected Composite createAdditionalOptions(final Composite parent) {
			final Group composite = new Group(parent, SWT.NONE);
			composite.setText("Actions:");
			composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
			
			fOpenControl = new Button(composite, SWT.CHECK);
			fOpenControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fOpenControl.setText("Open in &Editor");
			
			return composite;
		}
		
		protected void addBindings(final DataBindingContext dbc, final Realm realm) {
			fConfigControl.addBindings(dbc, realm);
			
			dbc.bindValue(SWTObservables.observeSelection(fOpenControl), fOpenValue);
		}
		
		public boolean getOpenInEditor() {
			return ((Boolean) fOpenValue.getValue()).booleanValue();
		}
		
		protected void saveSettings() {
			final IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, FILE_HISTORY_SETTINGSKEY, fConfig.getFilePath());
		}
		
		@Override
		public void dispose() {
			if (fDbc != null) {
				fDbc.dispose();
				fDbc = null;
			}
			super.dispose();
		}
		
	}
	
	
	private TrackingConfiguration fConfig;
	private int fSelectionLength;
	
	private NIConsolePage fConsolePage;
	
	private ConfigurationPage fConfigPage;
	
	
	public ExportConsoleOutputWizard(final NIConsolePage consolePage) {
		fConsolePage = consolePage;
		fSelectionLength = ((ITextSelection) consolePage.getOutputViewer().getSelection()).getLength();
		
		setWindowTitle("Export Console Output");
		setNeedsProgressMonitor(true);
		
		setDialogSettings(DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), "tools/ExportConsoleOutputWizard"));
	}
	
	protected TrackingConfiguration createTrackingConfiguration() {
		return new TrackingConfiguration(""); //$NON-NLS-1$
	}
	
	@Override
	public void addPages() {
		fConfig = createTrackingConfiguration();
		fConfigPage = new ConfigurationPage(fConsolePage, fConfig, fSelectionLength > 0);
		addPage(fConfigPage);
	}
	
	
	@Override
	public boolean performFinish() {
		fConfigPage.saveSettings();
		final boolean openInEditor = fConfigPage.getOpenInEditor();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final SubMonitor progress = SubMonitor.convert(monitor, "Export Output", 100);
					final TextConsoleViewer outputViewer = fConsolePage.getOutputViewer();
					final AbstractDocument document = (AbstractDocument) outputViewer.getDocument();
					
					final IJobManager jobManager = Job.getJobManager();
					final ISchedulingRule schedulingRule = fConsolePage.getConsole().getSchedulingRule();
					jobManager.beginRule(schedulingRule, progress.newChild(1));
					try {
						if (fSelectionLength > 0) {
							final AtomicReference<ITextSelection> currentSelection = new AtomicReference<ITextSelection>();
							getShell().getDisplay().syncExec(new Runnable() {
								public void run() {
									final ITextSelection selection = (ITextSelection) outputViewer.getSelection();
									if (selection.getLength() != fSelectionLength) {
										final boolean continueExport = MessageDialog.openQuestion(getShell(), "Export Output",
												"The selection is changed due to updates in the console. Do you want to continue nevertheless?");
										if (!continueExport) {
											return;
										}
									}
									currentSelection.set(selection);
								}
							});
							final ITextSelection selection = currentSelection.get();
							if (selection == null) {
								return;
							}
							
							progress.setWorkRemaining(95);
							export(document, selection.getOffset(), selection.getLength(), openInEditor, progress);
						}
						else {
							progress.setWorkRemaining(95);
							export(document, 0, document.getLength(), openInEditor, progress);
						}
					}
					finally {
						jobManager.endRule(schedulingRule);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			StatusManager.getManager().handle(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
					"An error occurred when exporting console output to file.", cause),
					StatusManager.LOG | StatusManager.SHOW);
			return !(cause instanceof CoreException || cause instanceof IOException);
		}
		catch (final InterruptedException e) {
		}
		return true;
	}
	
	private void export(final AbstractDocument document, final int offset, final int length, final boolean openInEditor, final SubMonitor progress) throws InvocationTargetException {
		OutputStream outputStream = null;
		Writer outputWriter = null;
		try {
			String filePath = fConfig.getFilePath();
			filePath = TrackWriter.resolveVariables(filePath, fConsolePage.getTool().getWorkspaceData());
			final IFileStore fileStore = FileUtil.getFileStore(filePath);
			
			outputStream = fileStore.openOutputStream(fConfig.getFileMode(), progress.newChild(1));
			if (fileStore.fetchInfo().getLength() <= 0L) {
				FileUtil.prepareTextOutput(outputStream, fConfig.getFileEncoding());
			}
			outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream, fConfig.getFileEncoding()));
			
			if (fConfig.getPrependTimestamp()) {
				final ToolProcess process = fConsolePage.getConsole().getProcess();
				outputWriter.append(process.createTimestampComment(process.getConnectionTimestamp()));
			}
			
			if (fConfig.getTrackStreamInfo() && fConfig.getTrackStreamInput()
					&& fConfig.getTrackStreamOutput() && !fConfig.getTrackStreamOutputTruncate()) {
				int pOffset = offset;
				int pLength = length;
				while (pLength > 0) {
					final int currentLength = Math.min(pLength, 32768);
					outputWriter.append(document.get(pOffset, currentLength));
					pOffset += currentLength;
					pLength -= currentLength;
				}
			}
			else {
				final ITypedRegion[] partitions = document.getDocumentPartitioner().computePartitioning(offset, length);
				final SubMonitor exportProgress = progress.newChild(90);
				int counter = partitions.length;
				for (final ITypedRegion partition : partitions) {
					exportProgress.setWorkRemaining(counter--);
					final String type = partition.getType();
					String text2 = null;
					
					if (type == null) {
						continue;
					}
					
					int pOffset;
					int pLength;
					if (type.equals(NIConsoleOutputStream.INFO_STREAM_ID)) {
						if (!fConfig.getTrackStreamInfo()) {
							continue;
						}
						pOffset = Math.max(offset, partition.getOffset());
						pLength = Math.min(offset+length, partition.getOffset()+partition.getLength()) - pOffset;
					}
					else if (type.equals(NIConsoleOutputStream.INPUT_STREAM_ID)) {
						if (!fConfig.getTrackStreamInput()) {
							continue;
						}
						pOffset = Math.max(offset, partition.getOffset());
						pLength = Math.min(offset+length, partition.getOffset()+partition.getLength()) - pOffset;
					}
					else if (type.equals(NIConsoleOutputStream.OUTPUT_STREAM_ID)) {
						if (!fConfig.getTrackStreamOutput()) {
							continue;
						}
						pOffset = Math.max(offset, partition.getOffset());
						pLength = Math.min(offset+length, partition.getOffset()+partition.getLength()) - pOffset;
						if (fConfig.getTrackStreamOutputTruncate()) {
							final int firstLine = document.getLineOfOffset(pOffset);
							final int lastLine = document.getLineOfOffset(pOffset+pLength);
							if (lastLine - firstLine + 1 > fConfig.getTrackStreamOutputTruncateLines()) {
								pLength = document.getLineOffset(firstLine + fConfig.getTrackStreamOutputTruncateLines()) - pOffset;
								text2 = "[...] (truncated)\n\n";
							}
						}
					}
					else if (type.equals(NIConsoleOutputStream.ERROR_STREAM_ID)) {
						if (!fConfig.getTrackStreamOutput()) {
							continue;
						}
						pOffset = Math.max(offset, partition.getOffset());
						pLength = Math.min(offset+length, partition.getOffset()+partition.getLength()) - pOffset;
					}
					else {
						pOffset = Math.max(offset, partition.getOffset());
						pLength = Math.min(offset+length, partition.getOffset()+partition.getLength()) - pOffset;
					}
					
					while (pLength > 0) {
						final int currentLength = Math.min(pLength, 32768);
						outputWriter.append(document.get(pOffset, currentLength));
						pOffset += currentLength;
						pLength -= currentLength;
					}
					if (text2 != null) {
						outputWriter.append(text2);
					}
				}
			}
			
			outputWriter.close();
			outputWriter = null;
			
			if (openInEditor) {
				UIAccess.getDisplay().asyncExec(new Runnable() {
					public void run() {
						OpenTrackingFilesContributionItem.open("export", fileStore);
					}
				});
			}
		}
		catch (final Exception e) {
			throw new InvocationTargetException(e);
		}
		finally {
			if (outputWriter != null) {
				try {
					outputWriter.close();
				} catch (final IOException ignore) {}
			}
			else if (outputStream != null) {
				try {
					outputStream.close();
				} catch (final IOException ignore) {}
			}
		}
	}
	
}
