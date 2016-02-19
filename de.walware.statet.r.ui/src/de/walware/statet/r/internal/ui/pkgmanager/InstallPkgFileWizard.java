/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.rj.renv.RPkgType;

import de.walware.statet.r.core.pkgmanager.IRLibPaths;
import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.RPkgAction;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;
import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


public class InstallPkgFileWizard extends Wizard {
	
	
	class Page extends WizardPage {
		
		
		private static final String FILE_HISTORY = "RPkgFile.history"; //$NON-NLS-1$
		
		
		private ResourceInputComposite fFileControl;
		private Label fTypeControl;
		private RLibrarySelectionComposite fTargetControl;
		
		private WritableValue fFileValue;
		private WritableValue fTypeValue;
		private WritableValue fTargetValue;
		
		
		public Page() {
			super("InstallPkgFilePage"); //$NON-NLS-1$
			
			setTitle("Install R Package from File");
			setDescription("Select the package file and target location.");
		}
		
		
		@Override
		public void createControl(final Composite parent) {
			initializeDialogUnits(parent);
			
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(LayoutUtil.createContentGrid(1));
			
			fFileControl = new ResourceInputComposite(composite, 
					ResourceInputComposite.STYLE_COMBO | ResourceInputComposite.STYLE_LABEL,
					ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN, 
					"Package &file");
			
			fFileControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fFileControl.setHistory(getDialogSettings().getArray(FILE_HISTORY));
			
			fTypeControl = new Label(composite, SWT.NONE);
			fTypeControl.setText("Type: ");
			fTypeControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final IRLibPaths rLibPaths = fPkgManager.getRLibPaths();
			{	final Group group = new Group(composite, SWT.NONE);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				group.setText("Target Library:");
				group.setLayout(LayoutUtil.createGroupGrid(1));
				
				fTargetControl = new RLibrarySelectionComposite(group);
				fTargetControl.setLayoutData(fTargetControl.createGD());
				fTargetControl.getValidator().setRequired(IRLibPaths.WRITABLE);
				fTargetControl.setInput(rLibPaths);
			}
			
			Dialog.applyDialogFont(composite);
			setControl(composite);
			
			final DataBindingSupport databinding = new DataBindingSupport(composite);
			addBindings(databinding);
			
			validateType();
			fTargetValue.setValue(RPkgUtil.getDefaultInstallLocation(rLibPaths));
			WizardPageSupport.create(this, databinding.getContext());
		}
		
		protected void addBindings(final DataBindingSupport databinding) {
			fFileValue = new WritableValue(databinding.getRealm(), String.class);
			fFileControl.getValidator().setOnNotLocal(IStatus.OK);
			fFileControl.getValidator().setFileStoreValidator(new IValidator() {
				@Override
				public IStatus validate(final Object value) {
					final IFileStore store = (IFileStore) value;
					if (store.getName().indexOf('_') < 0
							|| RPkgUtil.getPkgType(store.getName(), fPkgManager.getRPlatform()) == null ) {
						return ValidationStatus.error("File name must follow the pattern '<package_name>_<version>.<ext>'.");
					}
					validateType();
					return ValidationStatus.ok();
				}
			});
			databinding.getContext().bindValue(fFileControl.getObservable(), fFileValue,
					new UpdateValueStrategy().setAfterGetValidator(fFileControl.getValidator()),
					null );
			
			fTypeValue = new WritableValue(databinding.getRealm(), -1, Integer.class);
			fFileControl.getTextControl().addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					validateType();
				}
			});
			
			fTargetValue = new WritableValue(databinding.getRealm(), null, IRLibraryLocation.class);
			databinding.getContext().bindValue(
					ViewersObservables.observeSingleSelection(fTargetControl.getSelectionViewer()),
					fTargetValue,
					new UpdateValueStrategy().setAfterGetValidator(fTargetControl.getValidator()),
					null );
		}
		
		void validateType() {
			RPkgType type = null;
			final IFileStore store = fFileControl.getResourceAsFileStore();
			if (store != null) {
				type = RPkgUtil.getPkgType(store.getName(), fPkgManager.getRPlatform());
			}
			String text = "Type: ";
			if (type != null) {
				text += type.getLabel() +
						" (" + RPkgUtil.getPkgTypeInstallKey(fPkgManager.getRPlatform(), type) + ")";
			}
			fTypeControl.setText(text);
			fTypeValue.setValue(type);
		}
		
		
		public IFileStore getFile() {
			return fFileControl.getResourceAsFileStore();
		}
		
		public int getType() {
			return (Integer) fTypeValue.getValue();
		}
		
		public IRLibraryLocation getTargetLocation() {
			return (IRLibraryLocation) fTargetValue.getValue();
		}
		
		public void saveSettings() {
			final IDialogSettings settings = getDialogSettings();
			DialogUtil.saveHistorySettings(settings, FILE_HISTORY, (String) fFileValue.getValue());
		}
		
	}
	
	
	private final ITool fRTool;
	
	private final IRPkgManager.Ext fPkgManager;
	
	private Page fPage;
	
	
	public InstallPkgFileWizard(final ITool rTool, final IRPkgManager.Ext manager) {
		fRTool = rTool;
		fPkgManager = manager;
		
		setWindowTitle("R Package Manager");
		setNeedsProgressMonitor(true);
		
		setDialogSettings(DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "pkgmanager/InstallPkgFileWizard")); //$NON-NLS-1$
	}
	
	@Override
	public void addPages() {
		fPage = new Page();
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		fPage.saveSettings();
		
		final IFileStore store = fPage.getFile();
		final IRLibraryLocation location = fPage.getTargetLocation();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						final IRPkgData pkgData = fPkgManager.addToCache(store, monitor);
						final RPkgAction action = new RPkgAction.Install(pkgData, location, null);
						fPkgManager.perform(fRTool, Collections.singletonList(action));
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
					"An error occurred when preparing the R package installation from file.", e.getCause()));
		}
		catch (final InterruptedException e) {
		}
		return true;
	}
	
}
