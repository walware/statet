/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.core.Preference.StringPref2;

import de.walware.statet.r.internal.core.RProject;


public interface IRProject extends IPreferenceAccess, IRCoreAccess {
	
	
	String BUILD_PREF_QUALIFIER= RCore.PLUGIN_ID + "/build/RProject"; //$NON-NLS-1$
	
	Preference<String> BASE_FOLDER_PATH_PREF= new StringPref2(BUILD_PREF_QUALIFIER, RProject.BASE_FOLDER_PATH_KEY);
	
	Preference<String> RENV_CODE_PREF= new StringPref2(BUILD_PREF_QUALIFIER, RProject.RENV_CODE_KEY);
	
	Preference<String> PACKAGE_NAME_PREF= new StringPref2(BUILD_PREF_QUALIFIER, "Package.name"); //$NON-NLS-1$
	
	
	IProject getProject();
	
	String getPackageName();
	
	void setPackageConfig(String name) throws CoreException;
	
	IContainer getBaseContainer();
	
	
}
