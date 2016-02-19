/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImIdentitySet;

import de.walware.ecommons.preferences.PreferencesManageListener;
import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.IPreferenceSetService;
import de.walware.ecommons.preferences.core.IPreferenceSetService.IChangeEvent;
import de.walware.ecommons.preferences.core.Preference.StringPref2;
import de.walware.ecommons.preferences.core.util.PreferenceUtils;
import de.walware.ecommons.resources.AbstractProjectNature;
import de.walware.ecommons.resources.ProjectUtil;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvUtil;


public class RProject extends AbstractProjectNature implements IRProject, IPreferenceSetService.IChangeListener {
	
	
	public static final String OLD_QUALIFIER= "de.walware.r.core/RProjectBuild"; //$NON-NLS-1$
	
	public static final String BASE_FOLDER_PATH_KEY= "BaseFolder.path"; //$NON-NLS-1$
	public static final String RENV_CODE_KEY= "REnv.code"; //$NON-NLS-1$
	
	private static final StringPref2 PACKAGE_NAME_OLD_PREF= new StringPref2(OLD_QUALIFIER, "Package.name"); //$NON-NLS-1$
	
	
	private static final ImIdentitySet<String> PREF_QUALIFIERS= ImCollections.newIdentitySet(
			IREnvManager.PREF_QUALIFIER,
			BUILD_PREF_QUALIFIER );
	
	
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
	
	private IREnv rEnv;
	
	
	public RProject() {
		super();
	}
	
	
	@Override
	public void setProject(final IProject project) {
		super.setProject(project);
		
		addPreferenceSetListener(this, PREF_QUALIFIERS);
		
		updateREnv();
		
		this.rCodeStyle= new RCodeStyleSettings(1);
		this.preferenceListener= new PreferencesManageListener(this.rCodeStyle, getPrefs(),
				RCodeStyleSettings.ALL_GROUP_IDS );
		
		String pkgName= checkRPkgName(getProjectValue(PACKAGE_NAME_OLD_PREF));
		if (pkgName == null) {
			pkgName= checkRPkgName(getProjectValue(PACKAGE_NAME_OLD_PREF));
		}
		synchronized (this) {
			this.rPackageName= pkgName;
			try {
				RCorePlugin.getDefault().getRModelManager().getIndex().updateProjectConfig(this);
				
				if ((pkgName != null) != project.hasNature(RProjects.R_PKG_NATURE_ID)) {
					checkPackageNature();
				}
			}
			catch (final CoreException e) {
			}
		}
		
		RCorePlugin.getDefault().getResourceTracker().register(project, this);
	}
	
	@Override
	public void dispose() {
		RCorePlugin.getDefault().getResourceTracker().unregister(getProject());
		
		super.dispose();
		
		if (this.preferenceListener != null) {
			this.preferenceListener.dispose();
			this.preferenceListener= null;
		}
		
		this.rEnv= null;
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
	public void preferenceChanged(final IChangeEvent event) {
		if (event.contains(IREnvManager.PREF_QUALIFIER)
				|| event.contains(IRProject.RENV_CODE_PREF) ) {
			updateREnv();
		}
		if (event.contains(IRProject.PACKAGE_NAME_PREF)) {
			synchronized (this) {
				updateRPkgConfig(checkRPkgName(getProjectValue(IRProject.PACKAGE_NAME_PREF)));
			}
		}
	}
	
	private void updateREnv() {
		final IREnvManager rEnvManager= RCore.getREnvManager();
		final String s= getProjectValue(IRProject.RENV_CODE_PREF);
		this.rEnv= (s != null) ? REnvUtil.decode(s, rEnvManager) : rEnvManager.getDefault();
	}
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this;
	}
	
	@Override
	public IREnv getREnv() {
		return this.rEnv;
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.rCodeStyle;
	}
	
	@Override
	public IContainer getBaseContainer() {
		final String s= getPreferenceValue(IRProject.BASE_FOLDER_PATH_PREF);
		if (s != null) {
			final IPath path= Path.fromPortableString(s);
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
	public void setPackageConfig(String pkgName) throws CoreException {
		pkgName= checkRPkgName(pkgName);
		try {
			synchronized (this) {
				if (pkgName == this.rPackageName) {
					return;
				}
				
				final IScopeContext context= getProjectContext();
				PreferenceUtils.setPrefValue(context, PACKAGE_NAME_PREF, pkgName);
				context.getNode(PACKAGE_NAME_PREF.getQualifier()).flush();
				
				if (PreferenceUtils.getPrefValue(context, PACKAGE_NAME_OLD_PREF) != null) {
					PreferenceUtils.setPrefValue(context, PACKAGE_NAME_OLD_PREF, null);
				}
				
				updateRPkgConfig(pkgName);
			}
		}
		catch (final BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"An error occurred when saving the R project configuration." ));
		}
	}
	
	private String checkRPkgName(final String pkgName) {
		if (pkgName != null) {
			return pkgName.intern();
		}
		return null;
	}
	
	private void updateRPkgConfig(final String pkgName) {
		final boolean changed= (pkgName != null) != (this.rPackageName != null);
		this.rPackageName= pkgName;
		
		RCorePlugin.getDefault().getRModelManager().getIndex().updateProjectConfig(this);
		
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
