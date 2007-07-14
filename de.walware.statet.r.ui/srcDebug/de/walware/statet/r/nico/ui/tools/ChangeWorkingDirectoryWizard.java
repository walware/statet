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

package de.walware.statet.r.nico.ui.tools;

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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.util.DialogUtil;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.ext.ui.wizards.AbstractWizard;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.actions.IToolActionSupport;
import de.walware.statet.nico.ui.actions.ToolAction;
import de.walware.statet.nico.ui.util.ToolInfoGroup;
import de.walware.statet.r.internal.nico.ui.RNicoMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class ChangeWorkingDirectoryWizard extends AbstractWizard {
	
	
	public static class NonModalWizardDialog extends WizardDialog {
				public NonModalWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			
			setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.MODELESS | SWT.RESIZE | getDefaultOrientation());
		}

	}
	
	public static class ChangeAction extends ToolAction {

		public ChangeAction(IToolActionSupport support) {
			super(support, true);
			
			setId("de.walware.statet.r.tools.ChangeWorkingDirectory"); //$NON-NLS-1$
			setText(RNicoMessages.ChangeWorkingDir_Action_label);
		}
		
		@Override
		public void run() {
			ToolProcess tool = getTool();
			if (tool == null) {
				return;
			}
			ChangeWorkingDirectoryWizard wizard = new ChangeWorkingDirectoryWizard(tool);
			WizardDialog dialog = new NonModalWizardDialog(UIAccess.getActiveWorkbenchShell(true), wizard);
			dialog.setBlockOnOpen(false);
			dialog.open();
		}
	}
	
	
	private class SelectWDDialog extends WizardPage {
			
		private static final String SETTINGS_HISTORY = "statet:location.history"; //$NON-NLS-1$


		private ChooseResourceComposite fLocationGroup;
		private WritableValue fNewLocationString;
		private IFileStore fNewLocationEFS;
		
		private DataBindingContext fDbc;
		
		
		public SelectWDDialog() {
			super("ChangeWorkingDirectory"); //$NON-NLS-1$
			
			setTitle(RNicoMessages.ChangeWorkingDir_SelectDialog_title);
			setDescription(RNicoMessages.ChangeWorkingDir_SelectDialog_message);
		}
		
		public void createControl(Composite parent) {
			initializeDialogUnits(parent);
			
	    	Composite container = new Composite(parent, SWT.NONE);
	    	container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    	container.setLayout(new GridLayout());
			setControl(container);
			
			createContents(container);
			LayoutUtil.addSmallFiller(container, true);
			ToolInfoGroup info = new ToolInfoGroup(container, fTool);
			info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			Dialog.applyDialogFont(container);

//			initFields();
//			validate();
			setErrorMessage(null);
			setMessage(null);
		}
	
		protected void createContents(Composite container) {
			fLocationGroup = new ChooseResourceComposite(container,
					ChooseResourceComposite.STYLE_COMBO,
					ChooseResourceComposite.MODE_DIRECTORY | ChooseResourceComposite.MODE_OPEN,
					RNicoMessages.ChangeWorkingDir_Resource_label);
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY));
			fLocationGroup.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					fNewLocationEFS = fLocationGroup.getResourceAsFileStore();
				}
			});
			
			IFileStore current = fTool.getWorkspaceData().getWorkspaceDir();
			String dir = ""; //$NON-NLS-1$
			if (current != null) {
				IPath path = URIUtil.toPath(current.toURI());
				if (path != null) {
					dir = path.toOSString();
				}
			}

			Realm realm = Realm.getDefault();
			fDbc = new DataBindingContext(realm);
			fNewLocationString = new WritableValue(dir, String.class);
			fDbc.bindValue(fLocationGroup.createObservable(), fNewLocationString,
					new UpdateValueStrategy().setAfterGetValidator(fLocationGroup.getValidator()), null);
			
			WizardPageSupport.create(this, fDbc);
		}
		
		public void saveSettings() {
			IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, SETTINGS_HISTORY, (String) fNewLocationString.getValue());
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
	SelectWDDialog fPage;
	
	
	public ChangeWorkingDirectoryWizard(ToolProcess tool) {
		fTool = tool;
		
		setDialogSettings(RUIPlugin.getDefault(), ChangeWDRunnable.TYPE_ID+"-Wizard"); //$NON-NLS-1$
		setWindowTitle(RNicoMessages.ChangeWorkingDir_Task_label);
		setNeedsProgressMonitor(false);
	}
	
	@Override
	public void addPages() {
		fPage = new SelectWDDialog();
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		fPage.saveSettings();
		
		ToolController controller = fTool.getController();
		if (controller != null) {
			ChangeWDRunnable runnable = new ChangeWDRunnable(fPage.fNewLocationEFS);
			controller.submit(runnable);
		}
		return true;
	}
}
