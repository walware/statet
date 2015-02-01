/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.StringPref2;

import de.walware.statet.r.internal.core.RProject;


public interface IRProject extends IPreferenceAccess, IRCoreAccess {
	
	
	Preference<String> BASE_FOLDER_PREF= new StringPref2(RProject.RPROJECT_QUALIFIER, RProject.BASE_FOLDER_KEY);
	
	
	IProject getProject();
	
	String getPackageName();
	
	void setPackageConfig(String name) throws CoreException;
	
	IContainer getBaseContainer();
	
	
}
