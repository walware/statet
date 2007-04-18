/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.core.internal.BaseCorePlugin;


/**
 * Project to extend for a special StatET project nature. 
 */
public abstract class StatextProject implements IProjectNature, IPreferenceAccess {

	
	protected static IProjectDescription appendNature(IProjectDescription description, String id) {
		
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = id;
		description.setNatureIds(newNatures);
		
		return description;
	}
	
	
	protected IProject fProject;
	
	
	public StatextProject() {

		super();
	}

	
/*-- IProjectNature ----------------------------------------------------------*/
	
	public void configure() throws CoreException {

	}

	public void deconfigure() throws CoreException {

	}

	public void setProject(IProject project) {
		
		fProject = project;

		// Migrate from pre 0.4 nature
		// We keep old nature for backward compatibility (in this version),
		// but we can not add the old (forbidden by Eclipse)
		try {
			String oldId = "de.walware.statet.base.StatetNature"; //$NON-NLS-1$
			if (!project.hasNature(StatetProject.NATURE_ID) && project.hasNature(oldId)) {
				IProjectDescription description = project.getDescription();
				String[] natureIds = description.getNatureIds();
				for (int i = 0; i < natureIds.length; i++) {
					if (natureIds[i].equals(oldId)) {
						natureIds[i] = StatetProject.NATURE_ID;
					}
				}
				description.setNatureIds(natureIds);
				appendNature(description, oldId);
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			BaseCorePlugin.logError(-1, "Error occured when migrating StatET project nature 'base' -> 'base.core' for project '" + project.getName() + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public IProject getProject() {

		return fProject;
	}


/*-- IPreferenceAccess -------------------------------------------------------*/

	private IScopeContext[] getPrefContexts(boolean inheritInstanceSettings) {
		
		return (inheritInstanceSettings) ? 
				new IScopeContext[] {
					new ProjectScope(getProject()),
					new InstanceScope(),
					new DefaultScope(),
				} :
				new IScopeContext[] {
				  	new ProjectScope(getProject()),
				  	new DefaultScope(),
				};
	}
	
	public <T> T getPreferenceValue(Preference<T> key) {
		
		return getPrefValue(key, true);
	}
	
	public <T> T getPrefValue(Preference<T> key, boolean inheritInstanceSettings) {
		
		return PreferencesUtil.getPrefValue(getPrefContexts(inheritInstanceSettings), key);
	}
	
	public IEclipsePreferences[] getPreferenceNodes(String nodeQualifier) {
		
		return PreferencesUtil.getRelevantNodes(nodeQualifier, getPrefContexts(true));
	}
	
	public IScopeContext[] getPreferenceContexts() {
		
		return getPrefContexts(true);
	}
			
}
