/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import org.eclipse.core.resources.ICommand;
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
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference.StringPref2;
import de.walware.ecommons.preferences.PreferencesManageListener;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.resources.ProjectUtil;

import de.walware.statet.base.core.StatetExtNature;
import de.walware.statet.base.core.StatetProject;

import de.walware.statet.r.internal.core.Messages;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.RSupportBuilder;


public class RProject extends StatetExtNature implements IRCoreAccess {
	
	
	public static final String NATURE_ID = "de.walware.statet.r.RNature"; //$NON-NLS-1$
	
	private static final String RPROJECT_QUALIFIER = "de.walware.r.core/RProjectBuild";
	private static final String BASE_FOLDER_KEY = "BaseFolder.path"; //$NON-NLS-1$
	
	public static final StringPref2 PREF_BASE_FOLDER = new StringPref2(RPROJECT_QUALIFIER, BASE_FOLDER_KEY);
	
	private static final StringPref2 PREF_PACKAGE_NAME = new StringPref2(RPROJECT_QUALIFIER, "Package.name");
	
	
	public static RProject getRProject(final IProject project) {
		try {
			if (project == null || !project.hasNature(NATURE_ID)) {
				return null;
			}
			return (RProject) project.getNature(NATURE_ID);
		}
		catch (final CoreException e) {
			RCorePlugin.log(e.getStatus());
			return null;
		}
	}
	
	public static void addNature(final IProject project, final IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(Messages.RProject_ConfigureTask_label, 1000);
			
			if (!project.hasNature(NATURE_ID)) {
				StatetProject.addNature(project, new SubProgressMonitor(monitor, 400));
				
				IProjectDescription description = project.getDescription();
				description = ProjectUtil.appendNature(description, NATURE_ID);
				project.setDescription(description, new SubProgressMonitor(monitor, 600));
			}
		}
		finally {
			monitor.done();
		}
	}
	
	/**
	 * Find the specific Java command amongst the given build spec
	 * and return its index or -1 if not found.
	 */
	private static int getBuilderIndex(final ICommand[] buildSpec, final String id) {
		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(id)) {
				return i;
			}
		}
		return -1;
	}
	
	
	private RCodeStyleSettings fRCodeStyle;
	private PreferencesManageListener fPreferenceListener;
	
	private String fRPackageName;
	
	
	public RProject() {
		super();
	}
	
	
	@Override
	public void setProject(final IProject project) {
		super.setProject(project);
		fRCodeStyle = new RCodeStyleSettings();
		fPreferenceListener = new PreferencesManageListener(fRCodeStyle, getPrefs(), RCodeStyleSettings.ALL_GROUP_IDS);
		
		fRPackageName = getPrefs().getPreferenceValue(PREF_PACKAGE_NAME);
		try {
			if ((fRPackageName != null) != project.hasNature(RPkgProject.NATURE_ID)) {
				checkPackageNature(project);
			}
		}
		catch (CoreException e) {
		}
	}
	
	@Override
	protected void dispose() {
		if (fPreferenceListener != null) {
			fPreferenceListener.dispose();
			fPreferenceListener = null;
		}
		RCorePlugin.getDefault().getRModelManager().getIndex().updateProjectConfigClosed(fProject);
		super.dispose();
	}
	
	@Override
	public void configure() throws CoreException {
		addBuilders();
	}
	
	@Override
	public void deconfigure() throws CoreException {
		super.deconfigure();
		removeBuilders();
		RCorePlugin.getDefault().getRModelManager().getIndex().updateProjectConfigRemoved(fProject);
	}
	
	public void addBuilders() throws CoreException {
		final String builderId = RSupportBuilder.ID;
		
		final IProjectDescription description = fProject.getDescription();
		final ICommand[] existingCommands = description.getBuildSpec();
		final int builderIndex = getBuilderIndex(existingCommands, builderId);
		
		if (builderIndex == -1) {
			// Add new builder
			final ICommand newCommand = description.newCommand();
			newCommand.setBuilderName(builderId);
			
			final ICommand[] newCommands = new ICommand[existingCommands.length+1];
			System.arraycopy(existingCommands, 0, newCommands, 0, existingCommands.length);
			newCommands[existingCommands.length] = newCommand;
			
			description.setBuildSpec(newCommands);
			fProject.setDescription(description, null);
		}
	}
	
	public void removeBuilders() throws CoreException {
		final String builderId = RSupportBuilder.ID;
		
		final IProjectDescription description = getProject().getDescription();
		final ICommand[] existingCommands = description.getBuildSpec();
		final int builderIndex = getBuilderIndex(existingCommands, builderId);
		
		if (builderIndex >= 0) {
			final ICommand[] newCommands = new ICommand[existingCommands.length - 1];
			System.arraycopy(existingCommands, 0, newCommands, 0, builderIndex);
			System.arraycopy(existingCommands, builderIndex+1, newCommands, builderIndex, newCommands.length-builderIndex);
			description.setBuildSpec(newCommands);
		}
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return getStatetProject();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return fRCodeStyle;
	}
	
	public IContainer getBaseContainer() {
		final String value = getPrefs().getPreferenceValue(PREF_BASE_FOLDER);
		if (value != null) {
			final IPath path = Path.fromPortableString(value);
			if (path.segmentCount() == 0) {
				return getProject();
			}
			else {
				return getProject().getFolder(path);
			}
		}
		return null;
	}
	
	
	public String getPackageName() {
		return fRPackageName;
	}
	
	public void setPackageConfig(final String pkgName) throws CoreException {
		boolean changed = false;
		try {
			final IScopeContext context = getStatetProject().getProjectContext();
			PreferencesUtil.setPrefValue(context, PREF_PACKAGE_NAME, pkgName);
			context.getNode(PREF_PACKAGE_NAME.getQualifier()).flush();
			RCorePlugin.getDefault().getRModelManager().getIndex().updateProjectConfig(this, pkgName);
			changed = (pkgName != null) != (fRPackageName != null);
			fRPackageName = pkgName;
		}
		catch (final BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "An error occurred when saving the R project configuration."));
		}
		if (changed) {
			checkPackageNature(getProject());
		}
	}
	
	
	private void checkPackageNature(final IProject project) {
		final WorkspaceJob job = new WorkspaceJob("Update R Project") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				final boolean hasPackageNature = project.hasNature(RPkgProject.NATURE_ID);
				if ((fRPackageName != null) != hasPackageNature) {
					final IProjectDescription description;
					if (hasPackageNature) {
						description = ProjectUtil.removeNature(project.getDescription(),
								RPkgProject.NATURE_ID );
					}
					else {
						description = ProjectUtil.appendNature(project.getDescription(),
								RPkgProject.NATURE_ID );
					}
					project.setDescription(description, monitor);
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}
	
	
	@Override
	public int hashCode() {
		return getProject().hashCode()+1;
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj != null
				&& getClass().equals(obj.getClass())
				&& getProject().equals(((RProject) obj).getProject()) );
	}
	
}
