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

package de.walware.statet.r.internal.ui.tools;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.util.ToolInfoGroup;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class LoadRImageWizard extends Wizard {
	
	
	private static final List<String[]> RIMAGE_FILE_FILTERS = new ConstList<String[]>(
			new String[] { "*.RData", Messages.LoadData_Wizard_File_RImages_name } ); //$NON-NLS-1$
	
	private class SelectFileDialog extends WizardPage {
		
		private static final String SETTINGS_HISTORY = "statet:location.rdata"; //$NON-NLS-1$
		
		
		private ResourceInputComposite fLocationGroup;
		private WritableValue fNewLocationString;
		
		private DataBindingContext fDbc;
		
		
		public SelectFileDialog() {
			super("LoadData.SelectFile"); //$NON-NLS-1$
			
			setTitle(Messages.LoadData_Wizard_SelectPage_title);
			setDescription(Messages.LoadData_Wizard_SelectPage_description);
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
					ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN,
					Messages.LoadData_Wizard_File_label);
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY));
			
			String dir = ""; //$NON-NLS-1$
			final IFileStore current = fTool.getWorkspaceData().getWorkspaceDir();
			if (current != null) {
				final IPath path = URIUtil.toPath(current.toURI());
				if (path != null) {
					dir = path.toOSString();
				}
			}
			fLocationGroup.setDefaultFilesystemPath(dir);
			
			fLocationGroup.setFileFilters(RIMAGE_FILE_FILTERS);
			
			final Realm realm = Realm.getDefault();
			fDbc = new DataBindingContext(realm);
			fNewLocationString = new WritableValue("", String.class); //$NON-NLS-1$
			fDbc.bindValue(fLocationGroup.getObservable(), fNewLocationString,
					new UpdateValueStrategy().setAfterGetValidator(fLocationGroup.getValidator()), null);
			
			WizardPageSupport.create(this, fDbc);
		}
		
		public void saveSettings() {
			final IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, SETTINGS_HISTORY, (String) fNewLocationString.getValue());
		}
		
		public IFileStore getResource() {
			return fLocationGroup.getResourceAsFileStore();
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
	
	
	ToolProcess fTool;
	SelectFileDialog fPage;
	
	
	public LoadRImageWizard(final ToolProcess tool) {
		fTool = tool;
		
		setDialogSettings(DialogUtil.getDialogSettings(RUIPlugin.getDefault(), LoadRImageRunnable.TYPE_ID+"-Wizard")); //$NON-NLS-1$
		setWindowTitle(Messages.LoadData_Wizard_title);
		setNeedsProgressMonitor(false);
	}
	
	@Override
	public void addPages() {
		fPage = new SelectFileDialog();
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		fPage.saveSettings();
		
		final LoadRImageRunnable runnable = new LoadRImageRunnable(fPage.getResource());
		fTool.getQueue().add(runnable);
		return true;
	}
	
}
