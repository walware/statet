/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rmodel;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.rsource.ast.RAstNode;

/**
 *
 */
public interface IRSourceUnit extends ISourceUnit, IRCoreAccess {
	
	public static final String R_CONTENT = "de.walware.statet.r.contentTypes.R"; //$NON-NLS-1$
	public static final String RD_CONTENT = "de.walware.statet.r.contentTypes.Rd"; //$NON-NLS-1$

	public IRSourceUnit getWorkingCopy(WorkingContext context, boolean create);
	public IRSourceUnit getUnderlyingUnit();
	
	public StatetProject getStatetProject();
	public RProject getRProject();
	public IPreferenceAccess getPrefs();
	public RCodeStyleSettings getRCodeStyle();

	public void reconcile(int level, IProgressMonitor monitor);
	public AstInfo<RAstNode> getAstInfo(boolean ensureSync, IProgressMonitor monitor);
	
}