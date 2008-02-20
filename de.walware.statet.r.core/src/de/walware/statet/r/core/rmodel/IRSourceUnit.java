/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rmodel;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.eclipsecommons.ltk.ISourceUnit;

import de.walware.statet.r.core.RProject;


/**
 * R source (script) file
 */
public interface IRSourceUnit extends ISourceUnit {
	
	public static final String R_CONTENT = "de.walware.statet.r.contentTypes.R"; //$NON-NLS-1$
	public static final String RD_CONTENT = "de.walware.statet.r.contentTypes.Rd"; //$NON-NLS-1$
	
	public RProject getRProject();
//	public IPreferenceAccess getPrefs();
//	public RCodeStyleSettings getRCodeStyle();
	
	public void reconcile(int level, IProgressMonitor monitor);
	
}