/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.preferences;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ibm.icu.text.Collator;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.debug.ui.VariableFilter;
import de.walware.ecommons.ui.dialogs.DatabindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;
import de.walware.ecommons.ui.workbench.ChooseResourceComposite;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Dialog for an {@link REnvConfiguration}
 */
public class REnvConfigDialog extends ExtStatusDialog {
	
	
	private static final Integer T_64 = Integer.valueOf(64);
	private static final Integer T_32 = Integer.valueOf(32);
	
	
	private class RHomeComposite extends ChooseResourceComposite {
		
		public RHomeComposite(final Composite parent) {
			super (parent, 
					ChooseResourceComposite.STYLE_TEXT,
					ChooseResourceComposite.MODE_DIRECTORY | ChooseResourceComposite.MODE_OPEN, 
					Messages.REnv_Detail_Location_label);
			showInsertVariable(true, new VariableFilter[] {
					VariableFilter.EXCLUDE_BUILD_FILTER,
					VariableFilter.EXCLUDE_INTERACTIVE_FILTER,
					VariableFilter.EXCLUDE_JAVA_FILTER
			});
		}
		
		@Override
		protected void fillMenu(final Menu menu) {
			super.fillMenu(menu);
			
			final MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(Messages.REnv_Detail_FindAuto_label);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					final String[] rhome = searchRHOME();
					if (rhome != null) {
						setText(rhome[0], true);
						fRBitViewer.setSelection(new StructuredSelection(
								rhome[0].contains("64") ? T_64 : T_32)); //$NON-NLS-1$
						final String current = fNameControl.getText().trim();
						if ((current.length() == 0 || current.equals("R")) && rhome[1] != null) { //$NON-NLS-1$
							fNameControl.setText(rhome[1]);
						}
					}
					else {
						final String name = Messages.REnv_Detail_Location_label;
						MessageDialog.openInformation(getShell(), 
								MessageUtil.removeMnemonics(name), 
								NLS.bind(Messages.REnv_Detail_FindAuto_Failed_message, name));
					}
					getTextControl().setFocus();
				}
			});
			
		}
	}
	
	
	private final REnvPreferencePage.REnvConfig fConfigModel;
	private final boolean fIsNewConfig;
	private final Set<String> fExistingNames;
	
	private Text fNameControl;
	private ChooseResourceComposite fRHomeControl;
	private ComboViewer fRBitViewer;
	
	
	public REnvConfigDialog(final Shell parent, 
			final REnvPreferencePage.REnvConfig config, final boolean isNewConfig, 
			final Collection<REnvConfiguration> existingConfigs) {
		super(parent);
		
		fConfigModel = config;
		fIsNewConfig = isNewConfig;
		fExistingNames = new HashSet<String>();
		for (final REnvConfiguration ec : existingConfigs) {
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
		dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		
		{	// Name:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Name_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			fNameControl = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.widthHint = LayoutUtil.hintWidth(fNameControl, 60);
			fNameControl.setLayoutData(gd);
		}
		{	// Location:
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Location_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			fRHomeControl = new RHomeComposite(dialogArea);
			fRHomeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		{	// Type (Bits):
			final Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(Messages.REnv_Detail_Bits_label+':');
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			fRBitViewer = new ComboViewer(dialogArea);
			fRBitViewer.setContentProvider(new ArrayContentProvider());
			fRBitViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(final Object element) {
					return ((Integer) element).toString() + "-bit"; //$NON-NLS-1$
				}
			});
			fRBitViewer.setInput(new Integer[] { T_32, T_64 });
		}
		
		LayoutUtil.addSmallFiller(dialogArea, true);
		applyDialogFont(dialogArea);
		
		final DatabindingSupport databinding = new DatabindingSupport(dialogArea);
		addBindings(databinding);
		databinding.installStatusListener(new StatusUpdater());
		
		return dialogArea;
	}
	
	protected void addBindings(final DatabindingSupport db) {
		db.getContext().bindValue(SWTObservables.observeText(fNameControl, SWT.Modify), 
				BeansObservables.observeValue(fConfigModel, REnvConfiguration.PROP_NAME), 
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
					public IStatus validate(final Object value) {
						String s = (String) value;
						s = s.trim();
						if (s.length() == 0) {
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_Missing_message);
						}
						if (fExistingNames.contains(s)) {
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_Duplicate_message);
						}
						if (s.contains("/")) { //$NON-NLS-1$
							return ValidationStatus.error(Messages.REnv_Detail_Name_error_InvalidChar_message);
						}
						return ValidationStatus.ok();
					}
				}), null);
		db.getContext().bindValue(fRHomeControl.createObservable(), 
				BeansObservables.observeValue(fConfigModel, REnvConfiguration.PROP_RHOME), 
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
					public IStatus validate(final Object value) {
						final IStatus status = fRHomeControl.getValidator().validate(value);
						if (!status.isOK()) {
							return status;
						}
						if (!REnvConfiguration.isValidRHomeLocation(fRHomeControl.getResourceAsFileStore())) {
							return ValidationStatus.error(Messages.REnv_Detail_Location_error_NoRHome_message);
						}
						return ValidationStatus.ok();
					}
				}), null);
		db.getContext().bindValue(ViewersObservables.observeSingleSelection(fRBitViewer), 
				BeansObservables.observeValue(fConfigModel, REnvConfiguration.PROP_RBITS), 
				null, null);
	}
	
	private String[] searchRHOME() {
		try {
			final IStringVariableManager variables = VariablesPlugin.getDefault().getStringVariableManager();
			
			String loc = variables.performStringSubstitution("${env_var:R_HOME}", false); //$NON-NLS-1$
			if (loc != null && loc.length() > 0) {
				if (EFS.getLocalFileSystem().getStore(
						new Path(loc)).fetchInfo().exists()) {
					return new String[] { loc, Messages.REnv_SystemRHome_name };
				}
			}
			if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
				loc = "${env_var:PROGRAMFILES}\\R"; //$NON-NLS-1$
				final IFileStore res = EFS.getLocalFileSystem().getStore(
						new Path(variables.performStringSubstitution(loc)));
				if (!res.fetchInfo().exists()) {
					return null;
				}
				final String[] childNames = res.childNames(EFS.NONE, null);
				Arrays.sort(childNames, 0, childNames.length, Collator.getInstance());
				for (int i = childNames.length-1; i >= 0; i--) {
					if (REnvConfiguration.isValidRHomeLocation(res.getChild(childNames[i]))) {
						return new String[] { loc + '\\' + childNames[i], childNames[i] };
					}
				}
			}
			else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
				loc = "/Library/Frameworks/R.framework/Resources"; //$NON-NLS-1$
				if (REnvConfiguration.isValidRHomeLocation(EFS.getLocalFileSystem().getStore(new Path(loc)))) {
					return new String[] { loc, null };
				}
			}
			else {
				final String[] defLocations = new String[] {
						"/usr/lib/R", //$NON-NLS-1$
						"/usr/lib64/R", //$NON-NLS-1$
				};
				for (int i = 0; i < defLocations.length; i++) {
					loc = defLocations[i];
					if (REnvConfiguration.isValidRHomeLocation(EFS.getLocalFileSystem().getStore(new Path(loc)))) {
						return new String[] { loc, null };
					}
				}
			}
		}
		catch (final Exception e) {
			RUIPlugin.logError(-1, "Error when searching R_HOME location", e); //$NON-NLS-1$
		}
		return null;
	}
	
}
