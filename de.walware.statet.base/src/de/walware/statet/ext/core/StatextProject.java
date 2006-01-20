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

package de.walware.statet.ext.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import de.walware.eclipsecommon.preferences.IPreferenceAccess;
import de.walware.eclipsecommon.preferences.Preference;
import de.walware.eclipsecommon.preferences.PreferencesUtil;


/**
 * Project to extend for a special StatET project nature. 
 */
public abstract class StatextProject implements IProjectNature, IPreferenceAccess {

	
	protected IProject fProject;
	
	
	public StatextProject() {

		super();
	}

	
/* IProjectNature *************************************************************/
	
	public void configure() throws CoreException {

	}

	public void deconfigure() throws CoreException {

	}

	public void setProject(IProject project) {
		
		fProject = project;
	}
	
	public IProject getProject() {

		return fProject;
	}


/* */

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
