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

package de.walware.statet.r.internal.debug.ui.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;


/**
 * Dialog for a remote {@link IREnvConfiguration} (<code>user-remote</code>)
 */
public class REnvRemoteConfigDialog extends ExtStatusDialog {
	
	
	private final IREnvConfiguration.WorkingCopy fConfigModel;
	private final boolean fIsNewConfig;
	private final Set<String> fExistingNames;
	
	private Text fNameControl;
	
	private ResourceInputComposite fIndexDirectoryControl;
	
	
	public REnvRemoteConfigDialog(final Shell parent, 
			final IREnvConfiguration.WorkingCopy config, final boolean isNewConfig, 
			final Collection<IREnvConfiguration> existingConfigs) {
		super(parent, true);
		
		fConfigModel = config;
		fIsNewConfig = isNewConfig;
		fExistingNames = new HashSet<String>();
		for (final IREnvConfiguration ec : existingConfigs) {
			fExistingNames.add(ec.getName());
		}
		setTitle(fIsNewConfig ?
				Messages.REnv_Detail_AddDialog_title : 
				Messages.REnv_Detail_Edit_Dialog_title );
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogArea.setLayout(LayoutUtil.createDialogGrid(2));
		
		{	final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText("R Environment configuration for remote R installations (consoles).");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		}
		
		LayoutUtil.addSmallFiller(dialogArea, false);
		
		{	// Name:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Name_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			fNameControl = new Text(dialogArea, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 60);
			fNameControl.setLayoutData(gd);
		}
		
		LayoutUtil.addSmallFiller(dialogArea, false);
		
		{	// Index:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText("Index directory"+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final ResourceInputComposite text = new ResourceInputComposite(dialogArea, ResourceInputComposite.STYLE_TEXT,
					(ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN), "R_DOC_DIR");
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			text.setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
			fIndexDirectoryControl = text;
		}
		
		LayoutUtil.addSmallFiller(dialogArea, true);
		applyDialogFont(dialogArea);
		
		initBindings();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), IRUIHelpContextIds.R_ENV);
		
		return dialogArea;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(SWTObservables.observeText(fNameControl, SWT.Modify), 
				BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_NAME), 
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
					@Override
					public IStatus validate(final Object value) {
						String s = (String) value;
						s = s.trim();
						if (s.isEmpty()) {
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_Missing_message);
						}
						if (fExistingNames.contains(s)) {
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_Duplicate_message);
						}
						if (s.contains("/")) {  //$NON-NLS-1$
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_InvalidChar_message);
						}
						return ValidationStatus.ok();
					}
				}), null);
		db.getContext().bindValue(fIndexDirectoryControl.getObservable(),
				BeansObservables.observeValue(fConfigModel, IREnvConfiguration.PROP_INDEX_DIRECTORY) );
	}
	
}
