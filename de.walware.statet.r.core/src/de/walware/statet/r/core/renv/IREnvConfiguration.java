/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.renv;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;


/**
 * Configuration and properties of an R "runtime" environment.
 */
public interface IREnvConfiguration {
	
	
	String PROP_NAME = "name"; //$NON-NLS-1$
	
	String PROP_ID = "id"; //$NON-NLS-1$
	
	String PROP_RHOME = "RHome"; //$NON-NLS-1$
	
	String PROP_SUBARCH = "subArch"; //$NON-NLS-1$
	
	String PROP_ROS = "ROS"; //$NON-NLS-1$
	
	String PROP_RLIBS = "RLibraries"; //$NON-NLS-1$
	
	String PROP_RDOC_DIRECTORY = "RDocDirectoryPath"; //$NON-NLS-1$
	
	String PROP_RSHARE_DIRECTORY = "RShareDirectoryPath"; //$NON-NLS-1$
	
	String PROP_RINCLUDE_DIRECTORY = "RIncludeDirectoryPath"; //$NON-NLS-1$
	
	String PROP_INDEX_DIRECTORY = "indexDirectoryPath"; //$NON-NLS-1$
	
	
	String USER_LOCAL_TYPE = "user-local"; //$NON-NLS-1$
	
	String USER_REMOTE_TYPE = "user-remote"; //$NON-NLS-1$
	
	String EPLUGIN_LOCAL_TYPE = "eplugin-local"; //$NON-NLS-1$
	
	
	static enum Exec {
		COMMON,
		CMD,
		TERM;
	}
	
	
	static interface WorkingCopy extends IREnvConfiguration {
		
		
		void load(IREnvConfiguration template);
		
		
		void setName(String label);
		
		
		void setRHome(String directory);
		
		void setSubArch(String arch);
		
		void setROS(String type);
		
		void setRDocDirectoryPath(String directory);
		
		void setRShareDirectoryPath(String directory);
		
		void setRIncludeDirectoryPath(String directory);
		
		List<IRLibraryGroup.WorkingCopy> getRLibraryGroups();
		
		IRLibraryGroup.WorkingCopy getRLibraryGroup(String id);
		
		boolean isValidRHomeLocation(IFileStore rHome);
		
		List<String> searchAvailableSubArchs(IFileStore rHome);
		
		
		void setIndexDirectoryPath(String directory);
		
	}
	
	
	IREnv getReference();
	
	String getName();
	
	
	String getType();
	
	boolean isEditable();
	
	boolean isLocal();
	
	boolean isRemote();
	
	
	WorkingCopy createWorkingCopy();
	
	
	String getRHome();
	
	String getSubArch();
	
	String getROS();
	
	String getRDocDirectoryPath();
	
	String getRShareDirectoryPath();
	
	String getRIncludeDirectoryPath();
	
	List<? extends IRLibraryGroup> getRLibraryGroups();
	
	IRLibraryGroup getRLibraryGroup(String id);
	
	Map<String, String> getEnvironmentsVariables()
			throws CoreException;
	
	Map<String, String> getEnvironmentsVariables(boolean configureRLibs)
			throws CoreException;
	
	
	String getIndexDirectoryPath();
	
	IFileStore getIndexDirectoryStore();
	
	
	IStatus validate();
	
	List<String> getExecCommand(final Exec execType)
			throws CoreException;
	
	List<String> getExecCommand(String arg1, Set<Exec> execTypes)
			throws CoreException;
	
}
