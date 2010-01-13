/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.walware.ecommons.internal.ui.preferences.Messages;
import de.walware.ecommons.ui.dialogs.Layouter;
import de.walware.ecommons.ui.dialogs.StatusInfo;


/**
 * Base for project property and preference pages
 */
public abstract class PropertyAndPreferencePage<Block extends ConfigurationBlock>
		extends ConfigurationBlockPreferencePage<Block>
		implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {
	
	
	public static final String DATA_NO_LINK = "PropertyAndPreferencePage.nolink"; //$NON-NLS-1$
	
	
	// GUI Components
	private Composite fParentComposite;
	protected Button fUseProjectSettings;
	private Link fChangeWorkspaceSettings;
	private ControlEnableState fBlockEnableState;
	
	private IProject fProject; // project or null
	private Map fData; // page data
	
	
	public PropertyAndPreferencePage() {
		fProject = null;
		fData = null;
	}
	
	
	protected abstract String getPreferencePageID();
	protected abstract String getPropertyPageID();
	
	@Override
	protected abstract Block createConfigurationBlock() throws CoreException;
	
	protected abstract boolean hasProjectSpecificSettings(IProject project);
	
	
	protected final boolean supportsProjectSpecificSettings() {
		return getPropertyPageID() != null;
	}
	
	protected final boolean supportsInstanceSettings() {
		return getPreferencePageID() != null;
	}
	
	protected boolean offerLink() {
		return fData == null || !Boolean.TRUE.equals(fData.get(DATA_NO_LINK));
	}
	
	protected boolean isProjectSupported(final IProject project) throws CoreException {
		return true;
	}
	
	protected final boolean useProjectSettings() {
		return isProjectPreferencePage()
				&& (!supportsInstanceSettings() || fUseProjectSettings.getSelection());
	}
	
	protected final boolean isProjectPreferencePage() {
		return fProject != null;
	}
	
	protected final IProject getProject() {
		return fProject;
	}
	
	
	@Override
	protected Label createDescriptionLabel(final Composite parent) {
		fParentComposite = parent;
		
		if (isProjectPreferencePage()) {
			if (supportsInstanceSettings() && offerLink()) {
				final Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), 2);
				layouter.composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				
				fUseProjectSettings = layouter.addCheckBox(Messages.PropertyAndPreference_UseProjectSettings_label, 0, 1);
				fUseProjectSettings.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(final SelectionEvent e) {
					}
					public void widgetSelected(final SelectionEvent e) {
						doEnableProjectSpecificSettings(fUseProjectSettings.getSelection());
					};
				});
				
				if (offerLink()) {
					fChangeWorkspaceSettings = createLink(layouter.composite, Messages.PropertyAndPreference_ShowWorkspaceSettings_label);
				}
				
				layouter.addHorizontalLine();
			}
		}
		else { 
			if (supportsProjectSpecificSettings() && offerLink()) {
				fChangeWorkspaceSettings = createLink(parent, Messages.PropertyAndPreference_ShowProjectSpecificSettings_label);
			}
		}
		
		return super.createDescriptionLabel(parent);
	}
	
	private Link createLink(final Composite composite, final String text) {
		final Link link = new Link(composite, SWT.RIGHT);
		link.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		link.setText("<a>" + text + "</a>"); //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}
		});
		return link;
	}
	
	final void doLinkActivated(final Link link) {
		final Map<String, Object> data = new HashMap<String, Object>();
		data.put(DATA_NO_LINK, Boolean.TRUE);
		
		if (isProjectPreferencePage()) {
			openWorkspacePreferences(data);
		}
		else {
			try {
				final Set<IProject> all = getAllProjects();
				final Set<IProject> projectsWithSpecifics = new HashSet<IProject>();
				for (final IProject proj : all) {
					if (hasProjectSpecificSettings(proj.getProject())) {
						projectsWithSpecifics.add(proj);
					}
				}
				
				final ProjectSelectionDialog dialog = new ProjectSelectionDialog(getShell(), all, projectsWithSpecifics);
				if (dialog.open() == Window.OK) {
					final IProject proj = (IProject) dialog.getFirstResult();
					openProjectProperties(proj, data);
				}
			}
			catch (final Exception e) {
				// TODO
			}
			
		}
	}
	
	private Set<IProject> getAllProjects() throws CoreException {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final Set<IProject> collected = new HashSet<IProject>();
		for (final IProject project : projects) {
			if (isProjectSupported(project)) {
				collected.add(project);
			}
		}
		return collected;
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		if (fBlock == null)
			init(null);
		
		final Control control = super.createContents(parent);
		
		if (isProjectPreferencePage())
			doEnableProjectSpecificSettings(hasProjectSpecificSettings(getProject()));
		
		return control;
	}
	
	protected final void openWorkspacePreferences(final Object data) {
		final String id = getPreferencePageID();
		PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, data).open();
	}
	
	protected final void openProjectProperties(final IProject project, final Object data) {
		final String id = getPropertyPageID();
		if (id != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), project, id, new String[] { id }, data).open();
		}
	}
	
	
	protected void doEnableProjectSpecificSettings(final boolean useProjectSpecificSettings) {
		if (fBlock != null) {
			fBlock.setUseProjectSpecificSettings(useProjectSpecificSettings);
		}
		if (fUseProjectSettings != null) {
			fUseProjectSettings.setSelection(useProjectSpecificSettings);
		}
		if (useProjectSpecificSettings) {
			if (fBlockEnableState != null) {
				fBlockEnableState.restore();
				fBlockEnableState = null;
			}
		}
		else {
			if (fBlockEnableState == null) {
				fBlockEnableState = ControlEnableState.disable(fBlockControl);
			}
		}
		
		updateLinkVisibility();
		updateStatus();
	}
	
	private void updateLinkVisibility() {
		if (fChangeWorkspaceSettings == null || fChangeWorkspaceSettings.isDisposed())
			return;
		
		if (isProjectPreferencePage())
			fChangeWorkspaceSettings.setEnabled(!useProjectSettings());
	}
	
	@Override
	protected void updateStatus() {
		if (!isProjectPreferencePage() || useProjectSettings()) {
			updateStatus(fBlockStatus);
		} else {
			updateStatus(new StatusInfo());
		}
	}
	
	
/* PropertyPage Implementation ************************************************/
	
	public IAdaptable getElement() {
		return fProject;
	}
	
	public void setElement(final IAdaptable element) {
		fProject = (IProject) element.getAdapter(IResource.class);
	}
	
	
/* PreferencePage Implementation **********************************************/
	
// super
//	public void init(IWorkbench workbench) {
//
//		fBlock = createConfigurationBlock(getProject());
//	}
	
	@Override
	public void applyData(final Object data) {
		if (data instanceof Map) {
			fData = (Map) data;
		}
		if (fChangeWorkspaceSettings != null) {
			if (!offerLink()) {
				fChangeWorkspaceSettings.dispose();
				fParentComposite.layout(true, true);
			}
		}
	}
	
	protected Map getData() {
		return fData;
	}
	
	@Override
	public void performDefaults() {
		if (isProjectPreferencePage() && !useProjectSettings()) {
			return;
		}
		super.performDefaults();
	}
	
	@Override
	public boolean performCancel() {
		if (fBlock != null) {
			fBlock.performCancel();
		}
		return super.performCancel();
	}
	
}
