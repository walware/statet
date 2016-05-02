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

package de.walware.statet.r.internal.ui.buildpaths;

import org.eclipse.core.resources.IProject;

import de.walware.ecommons.ltk.buildpaths.core.BuildpathElementType;
import de.walware.ecommons.ltk.buildpaths.ui.BuildpathListElement;
import de.walware.ecommons.ltk.buildpaths.ui.BuildpathsUIDescription;


public class RBuildpathsUIDescription extends BuildpathsUIDescription {
	
	
	public RBuildpathsUIDescription() {
	}
	
	
	@Override
	public String getDefaultExt(final BuildpathListElement element) {
		return "R"; //$NON-NLS-1$
	}
	
	
	@Override
	public boolean getAllowAdd(final IProject project, final BuildpathElementType type) {
		return false;
	}
	
	@Override
	public boolean getAllowEdit(final BuildpathListElement element) {
		return false;
	}
	
}
