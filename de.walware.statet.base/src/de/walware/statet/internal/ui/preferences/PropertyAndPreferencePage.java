/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.internal.ui.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
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

import de.walware.eclipsecommon.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommon.ui.dialogs.Layouter;
import de.walware.eclipsecommon.ui.dialogs.StatusInfo;
import de.walware.eclipsecommon.ui.dialogs.StatusUtil;
import de.walware.eclipsecommon.ui.preferences.AbstractConfigurationBlock;
import de.walware.eclipsecommon.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.statet.base.StatetCore;
import de.walware.statet.base.StatetProject;
import de.walware.statet.ext.ui.preferences.ProjectSelectionDialog;


/**
 * Base for project property and preference pages
 */
public abstract class PropertyAndPreferencePage<Block extends AbstractConfigurationBlock> 
		extends ConfigurationBlockPreferencePage<Block> 
		implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {
	
	
	public static final String DATA_NO_LINK = "PropertyAndPreferencePage.nolink"; //$NON-NLS-1$

	
	// GUI Components
	private Composite fParentComposite;
	protected Button fUseProjectSettings;
	private Link fChangeWorkspaceSettings;
	private ControlEnableState fBlockEnableState;
	private IStatus fBlockSettingsStatus;
	
	private IProject fProject; // project or null
	private Map fData; // page data
	
	
	public PropertyAndPreferencePage() {
		
		fBlockSettingsStatus = new StatusInfo();
		fProject = null;
		fData = null;
	}

	protected abstract String getPreferencePageID();
	protected abstract String getPropertyPageID();

	protected abstract Block createConfigurationBlock(IProject project);
	protected abstract boolean hasProjectSpecificSettings(IProject project);
	

	protected boolean supportsProjectSpecificOptions() {
		return getPropertyPageID() != null;
	}
	
	protected boolean offerLink() {
		return fData == null || !Boolean.TRUE.equals(fData.get(DATA_NO_LINK));
	}

	protected boolean useProjectSettings() {
		return isProjectPreferencePage() && fUseProjectSettings != null && fUseProjectSettings.getSelection();
	}
	
	protected boolean isProjectPreferencePage() {
		return fProject != null;
	}

	protected IProject getProject() {
		return fProject;
	}


	protected Label createDescriptionLabel(Composite parent) {
		
		fParentComposite = parent;
		
		if (isProjectPreferencePage()) {
			Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), 2);
			layouter.fComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			fUseProjectSettings = layouter.addCheckBox(Messages.PropertyAndPreference_UseProjectSettings_label, 0, 1);
			fUseProjectSettings.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					doEnableProjectSpecificSettings(fUseProjectSettings.getSelection());
				};
			});
			
			if (offerLink()) {
				fChangeWorkspaceSettings = createLink(layouter.fComposite, Messages.PropertyAndPreference_ShowWorkspaceSettings_label);
			}
			
			layouter.addHorizontalLine();
		}
		else if (supportsProjectSpecificOptions() && offerLink()) {
			fChangeWorkspaceSettings = createLink(parent, Messages.PropertyAndPreference_ShowProjectSpecificSettings_label);
		}

		return super.createDescriptionLabel(parent);
    }
	
	private Link createLink(Composite composite, String text) {
		
		Link link = new Link(composite, SWT.RIGHT);
		link.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}
		});
		return link;
	}

	final void doLinkActivated(Link link) {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DATA_NO_LINK, Boolean.TRUE);
		
		if (isProjectPreferencePage()) {
			openWorkspacePreferences(data);
		} else {
			try {
				Set<StatetProject> all = StatetCore.getStatetProjects();
				Set<StatetProject> projectsWithSpecifics = new HashSet<StatetProject>();
				for (StatetProject proj : all) {
					if (hasProjectSpecificSettings(proj.getProject())) {
						projectsWithSpecifics.add(proj);
					}
				}
				
				ProjectSelectionDialog dialog = new ProjectSelectionDialog<StatetProject>(getShell(), all, projectsWithSpecifics);
				if (dialog.open() == Window.OK) {
					StatetProject res = (StatetProject) dialog.getFirstResult();
					openProjectProperties(res.getProject(), data);
				}
			} catch (Exception e) {
				//
			}
			
		}
	}

	
	protected Control createContents(Composite parent) {
		
		if (fBlock == null)
			init(null);
		
		Control control = super.createContents(parent);

		if (isProjectPreferencePage())
			doEnableProjectSpecificSettings(hasProjectSpecificSettings(getProject()));

		return control;
	}

	protected final void openWorkspacePreferences(Object data) {
		
		String id = getPreferencePageID();
		PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, data).open();
	}
	
	protected final void openProjectProperties(IProject project, Object data) {
		
		String id = getPropertyPageID();
		if (id != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), project, id, new String[] { id }, data).open();
		}
	}
	
	
	protected void doEnableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		
		if (fBlock != null)
			fBlock.setUseProjectSpecificSettings(useProjectSpecificSettings);

		fUseProjectSettings.setSelection(useProjectSpecificSettings);
		if (useProjectSpecificSettings) {
			if (fBlockEnableState != null) {
				fBlockEnableState.restore();
				fBlockEnableState = null;
			}
		} else {
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
	
	/**
	 * Returns a new status change listener
	 * when the status has changed
	 * @return The new listener
	 */
	protected IStatusChangeListener getNewStatusChangedListener() {
		
		return new IStatusChangeListener() {
			public void statusChanged(IStatus status) {
				fBlockSettingsStatus = status;
				updateStatus();
			}
		};		
	}
	
	private void updateStatus() {
		
		IStatus status;
		if (!isProjectPreferencePage() || useProjectSettings()) {
			status = fBlockSettingsStatus;
		} else {
			status = new StatusInfo();
		}
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}
	
//	protected void doStatusChanged() {
//		if (!isProjectPreferencePage() || useProjectSettings()) {
//			updateStatus(fBlockStatus);
//		} else {
//			updateStatus(new StatusInfo());
//		}
//	}

	

/* PropertyPage Implementation ************************************************/
	
	/* 
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		
		return fProject;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		
		fProject = (IProject) element.getAdapter(IResource.class);
	}
	
	
/* PreferencePage Implementation **********************************************/

// super
//	public void init(IWorkbench workbench) {
//
//		fBlock = createConfigurationBlock(getProject());
//	}
	protected final Block createConfigurationBlock() {
		
		return createConfigurationBlock(getProject());
	}

	public void applyData(Object data) {
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

	public void performApply() {
		if (fBlock != null) {
			fBlock.performApply();
		}
	}

	public void performDefaults() {
		if (useProjectSettings()) {
			doEnableProjectSpecificSettings(false);
		}
		super.performDefaults();
	}

	public boolean performCancel() {
		if (fBlock != null) {
			fBlock.performCancel();
		}
		return super.performCancel();
	}
	
}
