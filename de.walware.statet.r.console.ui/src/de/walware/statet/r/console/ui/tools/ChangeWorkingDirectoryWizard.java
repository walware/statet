/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.console.ui.tools;

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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.ui.actions.ToolAction;
import de.walware.statet.nico.ui.util.NicoWizardDialog;
import de.walware.statet.nico.ui.util.ToolInfoGroup;

import de.walware.statet.r.internal.console.ui.RConsoleMessages;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


/**
 * Wizard to change the working directory of R.
 */
public class ChangeWorkingDirectoryWizard extends Wizard {
	
	
	public static class ChangeAction extends ToolAction {
		
		public ChangeAction(final IToolProvider support) {
			super(support, true);
			
			setId("de.walware.statet.r.tools.ChangeWorkingDirectory"); //$NON-NLS-1$
			setText(RConsoleMessages.ChangeWorkingDir_Action_label);
		}
		
		@Override
		public void run() {
			final ToolProcess tool = getTool();
			if (tool == null) {
				return;
			}
			final ChangeWorkingDirectoryWizard wizard = new ChangeWorkingDirectoryWizard(tool);
			final WizardDialog dialog = new NicoWizardDialog(UIAccess.getActiveWorkbenchShell(true), wizard);
			dialog.setBlockOnOpen(false);
			dialog.open();
		}
	}
	
	
	private class SelectWDDialog extends WizardPage {
		
		private static final String SETTINGS_HISTORY = "statet:location.workingdir"; //$NON-NLS-1$
		
		
		private ResourceInputComposite fLocationGroup;
		private WritableValue fNewLocationString;
		
		private DataBindingContext fDbc;
		
		
		public SelectWDDialog() {
			super("ChangeWorkingDirectory"); //$NON-NLS-1$
			
			setTitle(RConsoleMessages.ChangeWorkingDir_SelectDialog_title);
			setDescription(RConsoleMessages.ChangeWorkingDir_SelectDialog_message);
		}
		
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
					ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
					RConsoleMessages.ChangeWorkingDir_Resource_label);
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY));
			
			final IFileStore current = fTool.getWorkspaceData().getWorkspaceDir();
			String dir = ""; //$NON-NLS-1$
			if (current != null) {
				final IPath path = URIUtil.toPath(current.toURI());
				if (path != null) {
					dir = path.toOSString();
				}
			}
			
			final Realm realm = Realm.getDefault();
			fDbc = new DataBindingContext(realm);
			fNewLocationString = new WritableValue(dir, String.class);
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
	SelectWDDialog fPage;
	
	
	public ChangeWorkingDirectoryWizard(final ToolProcess tool) {
		fTool = tool;
		
		setDialogSettings(DialogUtil.getDialogSettings(RConsoleUIPlugin.getDefault(), ChangeWDRunnable.TYPE_ID+"-Wizard")); //$NON-NLS-1$
		setWindowTitle(RConsoleMessages.ChangeWorkingDir_Task_label);
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
		
		final ToolController controller = fTool.getController();
		if (controller != null) {
			final ChangeWDRunnable runnable = new ChangeWDRunnable(fPage.getResource());
			controller.submit(runnable); // check return status?
		}
		return true;
	}
	
}
