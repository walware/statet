/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.graphics;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.util.ToolInfoGroup;

import de.walware.rj.eclient.IRToolService;
import de.walware.rj.eclient.graphics.IERGraphic;
import de.walware.rj.eclient.graphics.utils.CopyToDevRunnable;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


/**
 * Wizard to save R graphic to file.
 */
public class StatetRGraphicCopyToDevWizard extends Wizard {
	
	
	private class ConfigPage extends WizardPage {
		
		private static final String SETTINGS_HISTORY = "todev.file-"; //$NON-NLS-1$
		private static final String SETTINGS_OPEN = "open.file-"; //$NON-NLS-1$
		
		
		private final String fSettingType;
		
		private ResourceInputComposite fLocationGroup;
		private WritableValue fNewLocationString;
		
		private Button fOpenFileControl;
		private WritableValue fOpenFileValue;
		
		private DataBindingContext fDbc;
		
		
		public ConfigPage() {
			super("Config"); //$NON-NLS-1$
			
			fSettingType = fDevAbbr.toLowerCase();
			setTitle(NLS.bind("Save Graphic as {0} using R", fDevAbbr.toUpperCase()));
			setDescription("Select the file to save the graphic to.");
		}
		
		@Override
		public void createControl(final Composite parent) {
			initializeDialogUnits(parent);
			
			final Composite container = new Composite(parent, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			container.setLayout(new GridLayout());
			setControl(container);
			
			createContents(container);
			LayoutUtil.addSmallFiller(container, true);
			final ToolInfoGroup info = new ToolInfoGroup(container, fTool);
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			Dialog.applyDialogFont(container);
			
//			initFields();
//			validate();
			setErrorMessage(null);
			setMessage(null);
		}
		
		protected void createContents(final Composite container) {
			fLocationGroup = new ResourceInputComposite(container,
					ResourceInputComposite.STYLE_COMBO,
					ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_SAVE,
					"Graphic File");
			final IFileStore wd = fTool.getWorkspaceData().getWorkspaceDir();
			if (wd != null) {
				fLocationGroup.getValidator().setRelative(wd.toString(), IStatus.WARNING);
			}
			else {
				fLocationGroup.getValidator().setIgnoreRelative(true);
			}
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY+fSettingType));
			
			{	final Group group = new Group(container, SWT.DEFAULT);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
				
				fOpenFileControl = new Button(group, SWT.CHECK);
				fOpenFileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				fOpenFileControl.setText("&Open file when finished");
			}
			
			final Realm realm = Realm.getDefault();
			fDbc = new DataBindingContext(realm);
			fNewLocationString = new WritableValue("", String.class);
			fDbc.bindValue(fLocationGroup.getObservable(), fNewLocationString,
					new UpdateValueStrategy().setAfterGetValidator(fLocationGroup.getValidator()), null);
			fOpenFileValue = new WritableValue(realm, Boolean.FALSE, Boolean.class);
			fDbc.bindValue(fOpenFileValue, SWTObservables.observeSelection(fOpenFileControl));
			fOpenFileValue.setValue(getDialogSettings().getBoolean(SETTINGS_OPEN+fSettingType));
			
			WizardPageSupport.create(this, fDbc);
		}
		
		public void saveSettings() {
			final IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, SETTINGS_HISTORY+fSettingType, (String) fNewLocationString.getValue());
			settings.put(SETTINGS_OPEN+fSettingType, ((Boolean) fOpenFileValue.getValue()).booleanValue());
		}
		
		public FileValidator getValidator() throws CoreException {
			return fLocationGroup.getValidator();
		}
		
		public boolean getOpen() {
			return ((Boolean) fOpenFileValue.getValue()).booleanValue();
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
	
	
	private final ToolProcess fTool;
	private final IERGraphic fGraphic;
	private final String fDevCmd;
	private final String fDevAbbr;
	private ConfigPage fPage;
	
	
	public StatetRGraphicCopyToDevWizard(final ToolProcess tool, final IERGraphic graphic,
			final String devCmd, final String devAbbr) {
		fTool = tool;
		fGraphic = graphic;
		fDevCmd = devCmd;
		fDevAbbr = devAbbr;
		
		setDialogSettings(DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "GraphicCopyToDev-Wizard")); //$NON-NLS-1$
		setWindowTitle("Save Graphic using R");
		setNeedsProgressMonitor(false);
	}
	
	@Override
	public void addPages() {
		fPage = new ConfigPage();
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		if (!fTool.isTerminated()) {
			try {
				final FileValidator validator = fPage.getValidator();
				final IPath relative = (validator.isRelativeFile()) ?
						validator.getRelativeFile() : null;
				final IFileStore absolute = (relative == null) ? validator.getFileStore() : null;
				final String path = (relative != null) ? relative.toString() :
						fTool.getWorkspaceData().toToolPath(absolute);
				final boolean open = fPage.getOpen();
				final IWorkbenchPage workbenchPage = UIAccess.getActiveWorkbenchPage(true);
				
				fGraphic.getRHandle().getQueue().add(new CopyToDevRunnable(
						fGraphic, fDevCmd, RUtil.escapeCompletely(path),
						"onefile= TRUE, paper= \"special\"") {
					@Override
					public void run(final IRToolService r,
							final IProgressMonitor monitor) throws CoreException {
						super.run(r, monitor);
						if (open) {
							final IFileStore fileName;
							if (relative != null) {
								((IRBasicAdapter) r).refreshWorkspaceData(0, monitor);
								final IFileStore wd = fTool.getWorkspaceData().getWorkspaceDir();
								if (wd == null) {
									return;
								}
								fileName = wd.getFileStore(relative);
							}
							else {
								fileName = absolute;
							}
							if (fileName != null && fileName.fetchInfo(EFS.NONE, monitor).exists()) {
								UIAccess.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										try {
											IWorkbenchPage page = workbenchPage;
											if (page == null || page.getWorkbenchWindow().getShell() == null) {
												page = UIAccess.getActiveWorkbenchPage(true);
											}
											if (page == null) {
												return;
											}
											IDE.openEditorOnFileStore(page, fileName);
										}
										catch (final CoreException e) {
											StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
													"An error occurred when opening the exported R graphic.", e));
										}
									}
								});
							}
						}
					}
				});
			}
			catch (final CoreException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						NLS.bind("An error occurred when exporting the R graphic (Device {0}).", fGraphic.getDevId()+1), e));
				return false;
			}
		}
		
		fPage.saveSettings();
		return true;
	}
	
}
