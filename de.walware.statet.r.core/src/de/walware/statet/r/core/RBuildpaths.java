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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.buildpaths.core.BuildpathElementType;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathAttribute;
import de.walware.ecommons.ltk.buildpaths.core.IBuildpathElement;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.RProject;


public class RBuildpaths {
	
	
	public static final String R_TYPE_ID= RModel.R_TYPE_ID;
	
	
	public static final BuildpathElementType R_SOURCE_TYPE= new BuildpathElementType(R_TYPE_ID,
			IBuildpathElement.SOURCE, ImCollections.newList(
					IBuildpathAttribute.FILTER_INCLUSIONS, IBuildpathAttribute.FILTER_EXCLUSIONS ));
	
	
	public static void set(final IProject project, final ImList<IBuildpathElement> rawBuildpath) {
		final RProject rProject= RProject.getRProject(project);
		rProject.saveBuildpath(rawBuildpath);
	}
	
}
