/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.PreferencesManageListener;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.core.StatextProject;

import de.walware.statet.r.internal.core.Messages;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.RSupportBuilder;


public class RProject extends StatextProject implements IRCoreAccess {
	
	
	public static final String NATURE_ID = "de.walware.statet.r.RNature"; //$NON-NLS-1$
	
	
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
				
				final IProjectDescription description = appendNature(project.getDescription(), NATURE_ID);
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
	
	
/* IProjectNature *************************************************************/
	
	@Override
	public void configure() throws CoreException {
		addBuilders();
	}
	
	@Override
	public void deconfigure() throws CoreException {
		removeBuilders();
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
	
/* **/
	
	private RCodeStyleSettings fRCodeStyle;
	private PreferencesManageListener fPreferenceListener;
	
	
	public RProject() {
		super();
	}
	
	
	@Override
	public void setProject(final IProject project) {
		super.setProject(project);
		fRCodeStyle = new RCodeStyleSettings();
		fPreferenceListener = new PreferencesManageListener(fRCodeStyle, this, RCodeStyleSettings.GROUP_ID);
	}
	
	@Override
	protected void dispose() {
		super.dispose();
		if (fPreferenceListener != null) {
			fPreferenceListener.dispose();
			fPreferenceListener = null;
		}
	}
	
	public StatetProject getStatetProject() throws CoreException {
		return (StatetProject) fProject.getNature(StatetProject.NATURE_ID);
	}
	
	public IPreferenceAccess getPrefs() {
		return this;
	}
	
	public RCodeStyleSettings getRCodeStyle() {
		return fRCodeStyle;
	}
	
}
