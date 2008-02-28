/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.PreferencesUtil;


/**
 * Project to extend for a special StatET project nature. 
 */
public abstract class StatextProject implements IProjectNature, IPreferenceAccess {
	
	
	protected static IProjectDescription appendNature(final IProjectDescription description, final String id) {
		final String[] prevNatures = description.getNatureIds();
		final String[] newNatures = new String[prevNatures.length + 1];
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
	
	public void setProject(final IProject project) {
		fProject = project;
		project.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(final IResourceChangeEvent event) {
				if (event.getResource() == fProject) {
					fProject.getWorkspace().removeResourceChangeListener(this);
					dispose();
				}
			}
		}, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
	}
	
	public IProject getProject() {
		return fProject;
	}
	
	protected void dispose() {
	}
	
	
/*-- IPreferenceAccess -------------------------------------------------------*/
	
	private IScopeContext[] getPrefContexts(final boolean inheritInstanceSettings) {
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
	
	public <T> T getPreferenceValue(final Preference<T> key) {
		return getPrefValue(key, true);
	}
	
	public <T> T getPrefValue(final Preference<T> key, final boolean inheritInstanceSettings) {
		return PreferencesUtil.getPrefValue(getPrefContexts(inheritInstanceSettings), key);
	}
	
	public IEclipsePreferences[] getPreferenceNodes(final String nodeQualifier) {
		return PreferencesUtil.getRelevantNodes(nodeQualifier, getPrefContexts(true));
	}
	
	public IScopeContext[] getPreferenceContexts() {
		return getPrefContexts(true);
	}
	
}
