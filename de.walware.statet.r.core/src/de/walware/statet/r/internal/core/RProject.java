/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference.StringPref2;
import de.walware.ecommons.preferences.PreferencesManageListener;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.resources.AbstractProjectNature;
import de.walware.ecommons.resources.ProjectUtil;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;


public class RProject extends AbstractProjectNature implements IRProject {
	
	
	public static final String RPROJECT_QUALIFIER= "de.walware.r.core/RProjectBuild"; //$NON-NLS-1$
	public static final String BASE_FOLDER_KEY= "BaseFolder.path"; //$NON-NLS-1$
	
	private static final StringPref2 PREF_PACKAGE_NAME= new StringPref2(RPROJECT_QUALIFIER, "Package.name"); //$NON-NLS-1$
	
	
	public static RProject getRProject(final IProject project) {
		try {
			return (project != null) ? (RProject) project.getNature(RProjects.R_NATURE_ID) : null;
		}
		catch (final CoreException e) {
			RCorePlugin.log(e.getStatus());
			return null;
		}
	}
	
	
	private String rPackageName;
	
	private PreferencesManageListener preferenceListener;
	
	private RCodeStyleSettings rCodeStyle;
	
	
	public RProject() {
		super();
	}
	
	
	@Override
	public void setProject(final IProject project) {
		super.setProject(project);
		
		this.rCodeStyle= new RCodeStyleSettings(1);
		this.preferenceListener= new PreferencesManageListener(this.rCodeStyle, getPrefs(),
				RCodeStyleSettings.ALL_GROUP_IDS );
		
		this.rPackageName= getPrefs().getPreferenceValue(PREF_PACKAGE_NAME);
		try {
			if ((this.rPackageName != null) != project.hasNature(RProjects.R_PKG_NATURE_ID)) {
				checkPackageNature();
			}
		}
		catch (final CoreException e) {
		}
		
		RCorePlugin.getDefault().getResourceTracker().register(project, this);
	}
	
	public void dispose() {
		if (this.preferenceListener != null) {
			this.preferenceListener.dispose();
			this.preferenceListener= null;
			
			RCorePlugin.getDefault().getResourceTracker().unregister(getProject());
		}
	}
	
	@Override
	public void deconfigure() throws CoreException {
		dispose();
		super.deconfigure();
	}
	
	@Override
	public void addBuilders() throws CoreException {
		final IProject project= getProject();
		final IProjectDescription description= project.getDescription();
		boolean changed= false;
		changed|= ProjectUtil.addBuilder(description, RSupportBuilder.ID);
		
		if (changed) {
			project.setDescription(description, null);
		}
	}
	
	@Override
	public void removeBuilders() throws CoreException {
		final IProject project= getProject();
		final IProjectDescription description= project.getDescription();
		boolean changed= false;
		changed|= ProjectUtil.removeBuilder(description, RSupportBuilder.ID);
		
		if (changed) {
			project.setDescription(description, null);
		}
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this;
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.rCodeStyle;
	}
	
	@Override
	public IContainer getBaseContainer() {
		final String value= getPrefs().getPreferenceValue(IRProject.BASE_FOLDER_PREF);
		if (value != null) {
			final IPath path= Path.fromPortableString(value);
			if (path.segmentCount() == 0) {
				return getProject();
			}
			else {
				return getProject().getFolder(path);
			}
		}
		return null;
	}
	
	
	@Override
	public String getPackageName() {
		return this.rPackageName;
	}
	
	@Override
	public void setPackageConfig(final String pkgName) throws CoreException {
		boolean changed= false;
		try {
			final IScopeContext context= getProjectContext();
			PreferencesUtil.setPrefValue(context, PREF_PACKAGE_NAME, pkgName);
			context.getNode(PREF_PACKAGE_NAME.getQualifier()).flush();
			RCorePlugin.getDefault().getRModelManager().getIndex().updateProjectConfig(this, pkgName);
			changed= (pkgName != null) != (this.rPackageName != null);
			this.rPackageName= pkgName;
		}
		catch (final BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "An error occurred when saving the R project configuration."));
		}
		if (changed) {
			checkPackageNature();
		}
	}
	
	
	private void checkPackageNature() {
		final IProject project= getProject();
		final WorkspaceJob job= new WorkspaceJob("Update R Project") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor)
					throws CoreException {
				final boolean hasPackageNature= project.hasNature(RProjects.R_PKG_NATURE_ID);
				if ((RProject.this.rPackageName != null) != hasPackageNature) {
					final IProjectDescription description= project.getDescription();
					boolean changed= false;
					if (hasPackageNature) {
						changed|= ProjectUtil.removeNature(description, RProjects.R_PKG_NATURE_ID);
					}
					else {
						changed|= ProjectUtil.addNature(description, RProjects.R_PKG_NATURE_ID);
					}
					if (changed) { // should be always true
						project.setDescription(description, monitor);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}
	
}
