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
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.core.StatetProject;


/**
 * 
 */
public class RResourceUnit {

	
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
		return "unnamed";
	}
	
	public StatetProject getStatetProject() {
		
		if (fFile != null) {
			try {
				RProject rproj = (RProject) fFile.getProject().getNature(RProject.NATURE_ID);
				if (rproj != null) {
					return rproj.getStatetProject();
				}
			} catch (CoreException e) {
			}
		}
		return null;
	}
	
}
