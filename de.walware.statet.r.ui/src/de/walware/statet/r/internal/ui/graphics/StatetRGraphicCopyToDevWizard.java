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

package de.walware.statet.r.internal.ui.graphics;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.ui.util.ToolInfoGroup;

import de.walware.rj.eclient.graphics.IERGraphic;
import de.walware.rj.eclient.graphics.utils.CopyToDevRunnable;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Wizard to save R graphic to file.
 */
public class StatetRGraphicCopyToDevWizard extends Wizard {
	
	
	private class ConfigPage extends WizardPage {
		
		private static final String SETTINGS_HISTORY = "todev.file"; //$NON-NLS-1$
		
		
		private ResourceInputComposite fLocationGroup;
		private WritableValue fNewLocationString;
		
		private DataBindingContext fDbc;
		
		
		public ConfigPage() {
			super("Config"); //$NON-NLS-1$
			
			setTitle(NLS.bind("Save Graphic as {0} using R", fDevAbbr.toUpperCase()));
			setDescription("Select the file to save the graphic to.");
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
					ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_SAVE,
					"Graphic File");
			fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY));
			
			final Realm realm = Realm.getDefault();
			fDbc = new DataBindingContext(realm);
			fNewLocationString = new WritableValue("", String.class);
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
		final ToolController controller = fTool.getController();
		if (controller != null) {
			final ToolWorkspace workspace = controller.getWorkspaceData();
			try {
				final String path = workspace.toToolPath(fPage.getResource());
				fGraphic.getRHandle().getQueue().add(new CopyToDevRunnable(
						fGraphic, fDevCmd, RUtil.escapeCompletely(path), "onefile=TRUE,paper=\"special\""));
			}
			catch (final CoreException e) {
				StatusManager.getManager().handle(e.getStatus());
				return false;
			}
		}
		
		fPage.saveSettings();
		return true;
	}
	
}
