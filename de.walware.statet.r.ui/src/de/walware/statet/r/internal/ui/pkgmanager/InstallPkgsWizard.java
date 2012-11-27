/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.pkgmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgActionHelper;
import de.walware.statet.r.core.pkgmanager.RPkgResolver;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class InstallPkgsWizard extends Wizard {
	
	
	static final int MODE_INSTALL = 1;
	static final int MODE_UPDATE = 2;
	static final int MODE_REINSTALL = 3;
	
	
	class Page extends WizardPage {
		
		
		private Button fSameTargetControl;
		private RLibrarySelectionComposite fSelectTargetControl;
		
		private WritableValue fSameTargetValue;
		private WritableValue fTargetLibraryValue;
		
		
		public Page() {
			super("InstallPkgsTargetPage"); //$NON-NLS-1$
			
			setTitle(fTitle);
			setDescription("Select the target location.");
		}
		
		
		@Override
		public void createControl(final Composite parent) {
			initializeDialogUnits(parent);
			
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.createContentGrid(1));
			
			final IRLibPaths rLibPaths = fRPkgManager.getRLibPaths();
			{	final Group group = new Group(composite, SWT.NONE);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				group.setText("Target Library:");
				group.setLayout(LayoutUtil.createGroupGrid(1));
				
				fSelectTargetControl = new RLibrarySelectionComposite(group);
				fSelectTargetControl.setLayoutData(fSelectTargetControl.createGD());
				fSelectTargetControl.getValidator().setRequired(IRLibPaths.WRITABLE);
				fSelectTargetControl.setInput(rLibPaths);
				
				if (fMode == MODE_UPDATE) {
					fSameTargetControl = new Button(group, SWT.CHECK);
					fSameTargetControl.setText("Install updates to the library of the installed package, if possible.");
					fSameTargetControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				}
			}
			
			Dialog.applyDialogFont(composite);
			setControl(composite);
			
			final DataBindingSupport databinding = new DataBindingSupport(composite);
			addBindings(databinding);
			
			fTargetLibraryValue.setValue(RPkgUtil.getDefaultInstallLocation(rLibPaths));
			WizardPageSupport.create(this, databinding.getContext());
		}
		
		protected void addBindings(final DataBindingSupport databinding) {
			if (fSameTargetControl != null) {
				fSameTargetValue = new WritableValue(databinding.getRealm(), Boolean.FALSE, Boolean.class);
				databinding.getContext().bindValue(
						SWTObservables.observeSelection(fSameTargetControl),
						fSameTargetValue,
						null,
						null );
			}
			fTargetLibraryValue = new WritableValue(databinding.getRealm(), null, IRLibraryLocation.class);
			databinding.getContext().bindValue(
					ViewersObservables.observeSingleSelection(fSelectTargetControl.getSelectionViewer()),
					fTargetLibraryValue,
					new UpdateValueStrategy().setAfterGetValidator(fSelectTargetControl.getValidator()),
					null );
		}
		
		public boolean getInstallSameLocation() {
			return (fSameTargetValue != null && ((Boolean) fSameTargetValue.getValue()).booleanValue());
		}
		
		public IRLibraryLocation getInstallTargetLocation() {
			return (IRLibraryLocation) fTargetLibraryValue.getValue();
		}
		
	}
	
	private final ITool fRTool;
	
	final IRPkgManager.Ext fRPkgManager;
	
	private Page fPage;
	private StatusPage fStatusPage;
	private SummaryPage fSummaryPage;
	
	private final RPkgResolver fResolver;
	
	private List<RPkgAction.Install> fActions;
	private RPkgActionHelper fActionsHelper;
	
	private final int fMode;
	private final String fTitle;
	
	
	public InstallPkgsWizard(final ITool rTool, final IRPkgManager.Ext manager,
			final int mode, final RPkgResolver plan) {
		fRTool = rTool;
		fRPkgManager = manager;
		
		fMode = mode;
		switch (fMode) {
		case MODE_INSTALL:
			fTitle = "Install Selected R Packages";
			break;
		case MODE_UPDATE:
			fTitle = "Update Selected R Packages";
			break;
		case MODE_REINSTALL:
			fTitle = "Reinstall R Packages";
			break;
		default:
			throw new IllegalArgumentException("mode"); //$NON-NLS-1$
		}
		fResolver = plan;
		
		setWindowTitle("R Package Manager");
		setNeedsProgressMonitor(true);
		
		setDialogSettings(DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "pkgmanager/InstallPkgsWizard"));
	}
	
	@Override
	public void addPages() {
		if (fResolver.getStatus().getSeverity() >= IStatus.WARNING) {
			fStatusPage = new StatusPage(fTitle, true);
			fStatusPage.setStatus(fResolver.getStatus());
			addPage(fStatusPage);
		}
		if (fMode != MODE_REINSTALL) {
			fPage = new Page();
			addPage(fPage);
		}
		
		fSummaryPage = new SummaryPage(fRPkgManager, fResolver, fTitle) {
			@Override
			public void updateInput() {
				setActions(getActions(createHelper()));
			}
		};
		addPage(fSummaryPage);
	}
	
	
	private List<? extends RPkgAction> getActions(final RPkgActionHelper helper) {
		if (fActions == null) {
			fActions = fResolver.createActions();
		}
		if (fActionsHelper == null || !fActionsHelper.equals(helper)) {
			fActionsHelper = helper;
			helper.update(fActions);
		}
		return fActions;
	}
	
	private RPkgActionHelper createHelper() {
		switch (fMode) {
		case MODE_REINSTALL:
			return new RPkgActionHelper(true, null, fRPkgManager.getRLibPaths());
		default:
			return new RPkgActionHelper(
					fPage.getInstallSameLocation(),
					fPage.getInstallTargetLocation(),
					fRPkgManager.getRLibPaths() );
		}
	}
	
	@Override
	public boolean performFinish() {
		final RPkgActionHelper helper = createHelper();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final List<? extends RPkgAction> actions = getActions(helper);
					fRPkgManager.perform(fRTool, actions);
				}
			});
		}
		catch (final InvocationTargetException e) {
		}
		catch (final InterruptedException e) {
		}
		return true;
	}
	
}
