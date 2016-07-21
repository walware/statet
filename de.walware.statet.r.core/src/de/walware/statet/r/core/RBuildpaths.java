/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.buildpaths.core.BuildpathElementType;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathAttribute;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathElement;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.RProject;


public class RBuildpaths {
	
	
	public static final String R_TYPE_ID= RModel.R_TYPE_ID;
	
	
	public static final String PKG_DESCRIPTION_FILE_NAME= "DESCRIPTION"; //$NON-NLS-1$
	public static final IPath PKG_DESCRIPTION_FILE_PATH= new Path(PKG_DESCRIPTION_FILE_NAME);
	public static final String PKG_NAMESPACE_FILE_NAME= "NAMESPACE"; //$NON-NLS-1$
	public static final IPath PKG_NAMESPACE_FILE_PATH= new Path(PKG_NAMESPACE_FILE_NAME);
	public static final String PKG_R_FOLDER_NAME= "R"; //$NON-NLS-1$
	public static final IPath PKG_R_FOLDER_PATH= new Path(PKG_R_FOLDER_NAME);
	public static final String PKG_DATA_FOLDER_NAME= "data"; //$NON-NLS-1$
	public static final IPath PKG_DATA_FOLDER_PATH= new Path(PKG_DATA_FOLDER_NAME);
	public static final String PKG_DEMO_FOLDER_NAME= "demo"; //$NON-NLS-1$
	public static final IPath PKG_DEMO_FOLDER_PATH= new Path(PKG_DEMO_FOLDER_NAME);
	public static final String PKG_MAN_FOLDER_NAME= "man"; //$NON-NLS-1$
	public static final IPath PKG_MAN_FOLDER_PATH= new Path(PKG_MAN_FOLDER_NAME);
	public static final String PKG_VIGNETTES_FOLDER_NAME= "vignettes"; //$NON-NLS-1$
	public static final IPath PKG_VIGNETTES_FOLDER_PATH= new Path(PKG_VIGNETTES_FOLDER_NAME);
	public static final String PKG_SRC_FOLDER_NAME= "src"; //$NON-NLS-1$
	public static final IPath PKG_SRC_FOLDER_PATH= new Path(PKG_SRC_FOLDER_NAME);
	public static final String PKG_TESTS_FOLDER_NAME= "tests"; //$NON-NLS-1$
	public static final IPath PKG_TESTS_FOLDER_PATH= new Path(PKG_TESTS_FOLDER_NAME);
	public static final String PKG_PO_FOLDER_NAME= "po"; //$NON-NLS-1$
	public static final IPath PKG_PO_FOLDER_PATH= new Path(PKG_PO_FOLDER_NAME);
	public static final String PKG_EXEC_FOLDER_NAME= "exec"; //$NON-NLS-1$
	public static final IPath PKG_EXEC_FOLDER_PATH= new Path(PKG_EXEC_FOLDER_NAME);
	public static final String PKG_INST_FOLDER_NAME= "inst"; //$NON-NLS-1$
	public static final IPath PKG_INST_FOLDER_PATH= new Path(PKG_INST_FOLDER_NAME);
	public static final String PKG_RCHECK_FOLDER_NAME= ".Rcheck"; //$NON-NLS-1$
	public static final IPath PKG_RCHECK_FOLDER_PATH= new Path(PKG_RCHECK_FOLDER_NAME);
	
	
	public static final BuildpathElementType R_SOURCE_TYPE= new BuildpathElementType(R_TYPE_ID,
			IBuildpathElement.SOURCE, ImCollections.newList(
					IBuildpathAttribute.FILTER_INCLUSIONS, IBuildpathAttribute.FILTER_EXCLUSIONS ));
	
	
	public static void set(final IProject project, final ImList<IBuildpathElement> rawBuildpath) {
		final RProject rProject= RProject.getRProject(project);
		rProject.saveBuildpath(rawBuildpath);
	}
	
}
