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

package de.walware.statet.nico.ui.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.jface.databinding.swt.WidgetProperties;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
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
	
	
	private static final String FILE_HISTORY_SETTINGSKEY= "FileLocation_history";
	
	
	protected static class ConfigurationPage extends WizardPage {
		
		
		private final NIConsolePage consolePage;
		
		private final TrackingConfiguration config;
		private final WritableValue openValue;
		
		private TrackingConfigurationComposite configControl;
		private Button openControl;
		
		private DataBindingSupport dataBinding;
		
		
		
		public ConfigurationPage(final NIConsolePage page, final TrackingConfiguration config, final boolean selectionMode) {
			super("ConfigureConsoleExportPage"); //$NON-NLS-1$
			this.consolePage= page;
			setTitle(selectionMode ? "Export Selected Output" : "Export Current Output");
			setDescription("Select the content to export and the destination file");
			
			this.config= config;
			
			final Realm realm= Realm.getDefault();
			this.openValue= new WritableValue(realm, false, Boolean.class);
		}
		
		@Override
		public void createControl(final Composite parent) {
			initializeDialogUnits(parent);
			
			final Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.createContentGrid(1));
			
			this.configControl= createTrackingControl(composite);
			this.configControl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			this.configControl.getPathInput().getValidator().setOnLateResolve(IStatus.ERROR);
			this.configControl.getPathInput().setShowInsertVariable(true,
					DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS,
					this.consolePage.getTool().getWorkspaceData().getStringVariables() );
			this.configControl.setInput(this.config);
			
			final Composite additionalOptions= createAdditionalOptions(composite);
			additionalOptions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			LayoutUtil.addSmallFiller(composite, true);
			final ToolInfoGroup info= new ToolInfoGroup(composite, this.consolePage.getTool());
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			Dialog.applyDialogFont(composite);
			setControl(composite);
			
			this.configControl.getPathInput().setHistory(getDialogSettings().getArray(FILE_HISTORY_SETTINGSKEY));
			final Realm realm= Realm.getDefault();
			this.dataBinding= new DataBindingSupport(composite);
			addBindings(this.dataBinding);
			WizardPageSupport.create(this, this.dataBinding.getContext());
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
				@Override
				protected EnumSet<SubmitType> getEditableSubmitTypes() {
					return EnumSet.of(SubmitType.OTHER);
				}
			};
		}
		
		protected Composite createAdditionalOptions(final Composite parent) {
			final Group composite= new Group(parent, SWT.NONE);
			composite.setText("Actions:");
			composite.setLayout(LayoutUtil.createGroupGrid(1));
			
			this.openControl= new Button(composite, SWT.CHECK);
			this.openControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			this.openControl.setText("Open in &Editor");
			
			return composite;
		}
		
		protected void addBindings(final DataBindingSupport db) {
			this.configControl.addBindings(db);
			
			db.getContext().bindValue(
					WidgetProperties.selection().observe(this.openControl),
					this.openValue );
		}
		
		public boolean getOpenInEditor() {
			return ((Boolean) this.openValue.getValue()).booleanValue();
		}
		
		protected void saveSettings() {
			final IDialogSettings settings= getDialogSettings();
			DialogUtil.saveHistorySettings(settings, FILE_HISTORY_SETTINGSKEY, this.config.getFilePath());
		}
		
	}
	
	
	private TrackingConfiguration config;
	private final int selectionLength;
	
	private final NIConsolePage consolePage;
	
	private ConfigurationPage configPage;
	
	
	public ExportConsoleOutputWizard(final NIConsolePage consolePage) {
		this.consolePage= consolePage;
		this.selectionLength= ((ITextSelection) consolePage.getOutputViewer().getSelection()).getLength();
		
		setWindowTitle("Export Console Output");
		setNeedsProgressMonitor(true);
		
		setDialogSettings(DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), "tools/ExportConsoleOutputWizard"));
	}
	
	protected TrackingConfiguration createTrackingConfiguration() {
		final TrackingConfiguration config= new TrackingConfiguration(""); //$NON-NLS-1$
		config.getSubmitTypes().remove(SubmitType.OTHER);
		return config;
	}
	
	@Override
	public void addPages() {
		this.config= createTrackingConfiguration();
		this.configPage= new ConfigurationPage(this.consolePage, this.config, this.selectionLength > 0);
		addPage(this.configPage);
	}
	
	
	@Override
	public boolean performFinish() {
		this.configPage.saveSettings();
		final boolean openInEditor= this.configPage.getOpenInEditor();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final SubMonitor progress= SubMonitor.convert(monitor, "Export Output", 100);
					final TextConsoleViewer outputViewer= ExportConsoleOutputWizard.this.consolePage.getOutputViewer();
					final AbstractDocument document= (AbstractDocument) outputViewer.getDocument();
					
					final IJobManager jobManager= Job.getJobManager();
					final ISchedulingRule schedulingRule= ExportConsoleOutputWizard.this.consolePage.getConsole().getSchedulingRule();
					jobManager.beginRule(schedulingRule, progress.newChild(1));
					try {
						if (ExportConsoleOutputWizard.this.selectionLength > 0) {
							final AtomicReference<ITextSelection> currentSelection= new AtomicReference<>();
							getShell().getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									final ITextSelection selection= (ITextSelection) outputViewer.getSelection();
									if (selection.getLength() != ExportConsoleOutputWizard.this.selectionLength) {
										final boolean continueExport= MessageDialog.openQuestion(getShell(), "Export Output",
												"The selection is changed due to updates in the console. Do you want to continue nevertheless?");
										if (!continueExport) {
											return;
										}
									}
									currentSelection.set(selection);
								}
							});
							final ITextSelection selection= currentSelection.get();
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
			final Throwable cause= e.getCause();
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
		OutputStream outputStream= null;
		Writer outputWriter= null;
		try {
			String filePath= this.config.getFilePath();
			filePath= TrackWriter.resolveVariables(filePath, this.consolePage.getTool().getWorkspaceData());
			final IFileStore fileStore= FileUtil.getFileStore(filePath);
			
			outputStream= fileStore.openOutputStream(this.config.getFileMode(), progress.newChild(1));
			if (fileStore.fetchInfo().getLength() <= 0L) {
				FileUtil.prepareTextOutput(outputStream, this.config.getFileEncoding());
			}
			outputWriter= new BufferedWriter(new OutputStreamWriter(outputStream, this.config.getFileEncoding()));
			
			if (this.config.getPrependTimestamp()) {
				final ToolProcess process= this.consolePage.getConsole().getProcess();
				outputWriter.append(process.createTimestampComment(process.getConnectionTimestamp()));
			}
			
			if (this.config.getTrackStreamInfo() && this.config.getTrackStreamInput()
					&& this.config.getTrackStreamOutput() && !this.config.getTrackStreamOutputTruncate()
					&& this.config.getSubmitTypes().contains(SubmitType.OTHER) ) {
				int pOffset= offset;
				int pLength= length;
				while (pLength > 0) {
					final int currentLength= Math.min(pLength, 32768);
					outputWriter.append(document.get(pOffset, currentLength));
					pOffset+= currentLength;
					pLength-= currentLength;
				}
			}
			else {
				final ITypedRegion[] partitions= document.getDocumentPartitioner().computePartitioning(offset, length);
				final SubMonitor exportProgress= progress.newChild(90);
				int counter= partitions.length;
				for (final ITypedRegion partition : partitions) {
					exportProgress.setWorkRemaining(counter--);
					final String type= partition.getType();
					String text2= null;
					
					if (type == null) {
						continue;
					}
					
					boolean truncate= false;
					if (type == NIConsoleOutputStream.INFO_STREAM_ID) {
						if (!this.config.getTrackStreamInfo()) {
							continue;
						}
					}
					else if (type == NIConsoleOutputStream.OTHER_TASKS_INFO_STREAM_ID) {
						if (!this.config.getTrackStreamInfo()
								|| !this.config.getSubmitTypes().contains(SubmitType.OTHER)) {
							continue;
						}
					}
					else if (type == NIConsoleOutputStream.STD_INPUT_STREAM_ID) {
						if (!this.config.getTrackStreamInput()) {
							continue;
						}
					}
					else if (type == NIConsoleOutputStream.OTHER_TASKS_STD_INPUT_STREAM_ID) {
						if (!this.config.getTrackStreamInput()
								|| !this.config.getSubmitTypes().contains(SubmitType.OTHER)) {
							continue;
						}
					}
					else if (type == NIConsoleOutputStream.STD_OUTPUT_STREAM_ID) {
						if (!this.config.getTrackStreamOutput()) {
							continue;
						}
						truncate= this.config.getTrackStreamOutputTruncate();
					}
					else if (type == NIConsoleOutputStream.OTHER_TASKS_INFO_STREAM_ID) {
						if (!this.config.getTrackStreamOutput()
								|| !this.config.getSubmitTypes().contains(SubmitType.OTHER)) {
							continue;
						}
						truncate= this.config.getTrackStreamOutputTruncate();
					}
					else if (type == NIConsoleOutputStream.STD_ERROR_STREAM_ID) {
						if (!this.config.getTrackStreamOutput()) {
							continue;
						}
					}
					else if (type == NIConsoleOutputStream.OTHER_TASKS_STD_ERROR_STREAM_ID) {
						if (!this.config.getTrackStreamOutput()
								|| !this.config.getSubmitTypes().contains(SubmitType.OTHER)) {
							continue;
						}
					}
					else {
					}
					
					int pOffset= Math.max(offset, partition.getOffset());
					int pLength= Math.min(offset+length, partition.getOffset()+partition.getLength()) - pOffset;
					if (truncate) {
						final int firstLine= document.getLineOfOffset(pOffset);
						final int lastLine= document.getLineOfOffset(pOffset+pLength);
						if (lastLine - firstLine + 1 > this.config.getTrackStreamOutputTruncateLines()) {
							pLength= document.getLineOffset(firstLine + this.config.getTrackStreamOutputTruncateLines()) - pOffset;
							text2= "[...] (truncated)\n\n";
						}
					}
					
					while (pLength > 0) {
						final int currentLength= Math.min(pLength, 32768);
						outputWriter.append(document.get(pOffset, currentLength));
						pOffset+= currentLength;
						pLength-= currentLength;
					}
					if (text2 != null) {
						outputWriter.append(text2);
					}
				}
			}
			
			outputWriter.close();
			outputWriter= null;
			
			if (openInEditor) {
				UIAccess.getDisplay().asyncExec(new Runnable() {
					@Override
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
