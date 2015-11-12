/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.preferences.ui.PropertyAndPreferencePage;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.ui.RUI;


public class RProjectPropertyPage extends PropertyAndPreferencePage {
	
	
	public RProjectPropertyPage() {
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
	protected ConfigurationBlock createConfigurationBlock() throws CoreException {
		return new RProjectConfigurationBlock(getProject(), createStatusChangedListener());
	}
	
	@Override
	protected boolean hasProjectSpecificSettings(final IProject project) {
		return true;
	}
	
}


class RProjectConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private final IRProject rProject;
	
	private Text pkgNameControl;
	
	private RProjectContainerComposite projectComposite;
	
	private REnvSelectionComposite rEnvControl;
	
	
	public RProjectConfigurationBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project, statusListener);
		
		this.rProject= RProjects.getRProject(project);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs= new HashMap<>();
		
		prefs.put(IRProject.BASE_FOLDER_PATH_PREF, null);
		prefs.put(IRProject.RENV_CODE_PREF, null);
		
		setupPreferenceManager(prefs);
		
		final Composite mainComposite= new Composite(pageComposite, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(LayoutUtil.createCompositeGrid(2));
		
		final TabFolder folder = new TabFolder(mainComposite, SWT.NONE);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		{	final TabItem item = new TabItem(folder, SWT.NONE);
			item.setText("&Main");
			item.setControl(createMainTab(folder));
		}
		{	final TabItem item = new TabItem(folder, SWT.NONE);
			item.setImage(RUI.getImage(RUI.IMG_OBJ_R_RUNTIME_ENV));
			item.setText("R &Environment");
			item.setControl(createREnvTab(folder));
		}
		
		initBindings();
		updateControls();
	}
	
	private Control createMainTab(final Composite parent) {
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createTabGrid(2));
		
		{	final Composite packageComposite= new Composite(composite, SWT.NONE);
			packageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			packageComposite.setLayout(LayoutUtil.createCompositeGrid(2));
			
			final Label label= new Label(packageComposite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label.setText("Package &Name:");
			
			this.pkgNameControl= new Text(packageComposite, SWT.BORDER);
			this.pkgNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			this.pkgNameControl.setFont(JFaceResources.getTextFont());
			
			LayoutUtil.addGDDummy(composite);
		}
		
		this.projectComposite= new RProjectContainerComposite(composite, getProject());
		this.projectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.projectComposite.setBackground(this.projectComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		final Composite buttons= new Composite(composite, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(LayoutUtil.createCompositeGrid(1));
		
		final Button setBase= new Button(buttons, SWT.PUSH);
		setBase.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setBase.setText("R &Base Folder");
		setBase.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				RProjectConfigurationBlock.this.projectComposite.toggleBaseContainer();
			}
		});
		
		return composite;
	}
	
	private Control createREnvTab(final Composite parent) {
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createTabGrid(1));
		
		{	final Label label= new Label(composite, SWT.NONE);
			label.setText("R Envir&onment:");
		}
		this.rEnvControl= new REnvSelectionComposite(composite);
		this.rEnvControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		return composite;
	}
	
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(
				this.rEnvControl.createObservable(Realm.getDefault()),
				createObservable(IRProject.RENV_CODE_PREF),
				new UpdateValueStrategy().setAfterGetValidator(
						this.rEnvControl.createValidator(db.getContext()) ),
				null );
	}
	
	@Override
	protected void updateControls() {
		final String value= getPreferenceValue(IRProject.BASE_FOLDER_PATH_PREF);
		if (value != null) {
			final IPath basePath= Path.fromPortableString(value);
			this.projectComposite.setBaseContainer(basePath);
		}
		else {
			this.projectComposite.setBaseContainer(null);
		}
		
		final String packageName= this.rProject.getPackageName();
		this.pkgNameControl.setText((packageName != null) ? packageName : "");
		super.updateControls();
	}
	
	@Override
	protected void updatePreferences() {
		final IPath basePath= this.projectComposite.getBaseContainer();
		if (basePath != null) {
			setPrefValue(IRProject.BASE_FOLDER_PATH_PREF, basePath.toPortableString());
		}
		else {
			setPrefValue(IRProject.BASE_FOLDER_PATH_PREF, null);
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
			final String packageName= this.pkgNameControl.getText();
			this.rProject.setPackageConfig((packageName.trim().length() != 0) ? packageName : null);
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, "Failed to apply R project configuration."), StatusManager.SHOW);
		}
	}
	
}
