/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;


public class StatetProject implements IProjectNature, IPreferenceAccess {
	
	
	public static final String NATURE_ID = "de.walware.statet.base.StatetNature"; //$NON-NLS-1$
	
	
	public static void addNature(final IProject project, final IProgressMonitor monitor) throws CoreException {
		if (!project.hasNature(NATURE_ID)) {
			final IProjectDescription description = StatetExtNature.appendNature(project.getDescription(), NATURE_ID);
			project.setDescription(description, monitor);
		} 
		else {
			monitor.done();
		}
	}
	
	
	protected IProject fProject;
	
	protected IScopeContext[] fContexts;
	
	
	public StatetProject() {
		super();
	}
	
	
	public void setProject(final IProject project) {
		fProject = project;
		fContexts = createPrefContexts(true);
	}
	
	public IProject getProject() {
		return fProject;
	}
	
	public void configure() throws CoreException {
	}
	
	public void deconfigure() throws CoreException {
	}
	
	
/*-- IPreferenceAccess -------------------------------------------------------*/
	
	private IScopeContext[] createPrefContexts(final boolean inheritInstanceSettings) {
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
		return PreferencesUtil.getPrefValue(fContexts, key);
	}
	
	public IEclipsePreferences[] getPreferenceNodes(final String nodeQualifier) {
		return PreferencesUtil.getRelevantNodes(nodeQualifier, fContexts);
	}
	
	public IScopeContext[] getPreferenceContexts() {
		return fContexts;
	}
	
	public void addPreferenceNodeListener(final String nodeQualifier, final IPreferenceChangeListener listener) {
		int i = fContexts.length-2;
		while (i >= 0) {
			final IEclipsePreferences node = fContexts[i--].getNode(nodeQualifier);
			if (node != null) {
				node.addPreferenceChangeListener(listener);
			}
		}
	}
	
	public void removePreferenceNodeListener(final String nodeQualifier, final IPreferenceChangeListener listener) {
		int i = fContexts.length-2;
		while (i >= 0) {
			final IEclipsePreferences node = fContexts[i--].getNode(nodeQualifier);
			if (node != null) {
				node.removePreferenceChangeListener(listener);
			}
		}
	}
	
	public IScopeContext getProjectContext() {
		return fContexts[0];
	}
	
}
