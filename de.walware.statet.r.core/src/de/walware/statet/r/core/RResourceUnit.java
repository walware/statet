/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * 
 */
public class RResourceUnit implements IRCoreAccess {

	
	public static final String R_CONTENT = "de.walware.statet.r.contentTypes.R"; //$NON-NLS-1$
	public static final String RD_CONTENT = "de.walware.statet.r.contentTypes.Rd"; //$NON-NLS-1$
	
	
//	private String fElementStorageName;
	private IFile fFile;
	
//	public RResourceUnit(String elementStorageName) {
//		fElementStorageName = elementStorageName;
//	}
	

	public RResourceUnit(IFile file) {
		
		fFile = file;
	}
	
	public String getElementName() {
		
//		if (fElementStorageName != null)
//			return fElementStorageName;
		if (fFile != null)
			return fFile.getName();
		return "unnamed"; //$NON-NLS-1$
	}
	
	public RProject getRProject() {
		
		if (fFile != null) {
			IProject proj =  fFile.getProject();
			try {
				if (proj.hasNature(RProject.NATURE_ID)) {
					return (RProject) proj.getNature(RProject.NATURE_ID);
				}
			} catch (CoreException e) {
				RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occurred while access R project nature.", e));
			}
		}
		return null;
	}
	
	public StatetProject getStatetProject() {
		
		RProject rproj = getRProject();
		if (rproj != null) {
			try {
				return rproj.getStatetProject();
			} catch (CoreException e) {
				RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occurred while access Statet project nature.", e));
			}
		}
		return null;
	}
	
	public RCodeStyleSettings getRCodeStyle() {
		
		RProject rproj = getRProject();
		if (rproj != null) {
			rproj.getRCodeStyle();
		}
		return RCore.getWorkbenchAccess().getRCodeStyle();
	}
}
