/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import static de.walware.statet.r.core.RProject.PREF_BASE_FOLDER;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.preferences.ui.PropertyAndPreferencePage;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.ui.RUI;


public class RProjectPropertyPage extends PropertyAndPreferencePage<RProjectConfigurationBlock> {
	
	
	public RProjectPropertyPage() {
	}
	
	
	@Override
	protected RProjectConfigurationBlock createConfigurationBlock() throws CoreException {
		return new RProjectConfigurationBlock(getProject(), createStatusChangedListener());
	}
	
	@Override
	protected String getPreferencePageID() {
		return null;
	}
	
	@Override
	protected String getPropertyPageID() {
		return "de.walware.statet.r.propertyPages.RProject"; //$NON-NLS-1$
	}
	
	@Override
	protected boolean hasProjectSpecificSettings(final IProject project) {
		return true;
	}
	
}


class RProjectConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private RProjectContainerComposite fProjectComposite;
	
	private Text fPkgNameControl;
	
	private RProject fRProject;
	
	
	public RProjectConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project, statusListener);
		
		fRProject = RProject.getRProject(project);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		prefs.put(PREF_BASE_FOLDER, null);
		setupPreferenceManager(prefs);
		
		final Composite composite = new Composite(pageComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
		{	final Composite packageComposite = new Composite(composite, SWT.NONE);
			packageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			packageComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			final Label label = new Label(packageComposite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Package &Name:");
			
			fPkgNameControl = new Text(packageComposite, SWT.BORDER);
			fPkgNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fPkgNameControl.setFont(JFaceResources.getTextFont());
			
			LayoutUtil.addGDDummy(composite);
		}
		
		fProjectComposite = new RProjectContainerComposite(composite, getProject());
		fProjectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		
		final Button setBase = new Button(buttons, SWT.PUSH);
		setBase.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setBase.setText("R &Base Folder");
		setBase.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				fProjectComposite.toggleBaseContainer();
			}
		});
		
		updateControls();
	}
	
	
	@Override
	protected void updateControls() {
		final String value = getPreferenceValue(PREF_BASE_FOLDER);
		if (value != null) {
			final IPath basePath = Path.fromPortableString(value);
			fProjectComposite.setBaseContainer(basePath);
		}
		else {
			fProjectComposite.setBaseContainer(null);
		}
		
		final String packageName = fRProject.getPackageName();
		fPkgNameControl.setText((packageName != null) ? packageName : "");
		super.updateControls();
	}
	
	@Override
	protected void updatePreferences() {
		final IPath basePath = fProjectComposite.getBaseContainer();
		if (basePath != null) {
			setPrefValue(PREF_BASE_FOLDER, basePath.toPortableString());
		}
		else {
			setPrefValue(PREF_BASE_FOLDER, null);
		}
		
		super.updatePreferences();
	}
	
	@Override
	public void performApply() {
		saveProjectConfig();
		super.performApply();
	}
	
	@Override
	public boolean performOk() {
		saveProjectConfig();
		return super.performOk();
	}
	
	private void saveProjectConfig() {
		try {
			final String packageName = fPkgNameControl.getText();
			fRProject.setPackageConfig((packageName.trim().length() != 0) ? packageName : null);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, "Failed to apply R project configuration."), StatusManager.SHOW);
		}
	}
	
}