/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.pkgmanager;

import java.util.List;

import de.walware.statet.r.core.pkgmanager.RPkgAction;


public class StartAction {
	
	
	public static final int UNINSTALL = RPkgAction.UNINSTALL;
	public static final int INSTALL = RPkgAction.INSTALL;
	public static final int REINSTALL = 3;
	
	
	private final int fAction;
	private final List<String> fPkgNames;
	
	
	public StartAction(final int action) {
		this(action, null);
	}
	
	public StartAction(final int action, final List<String> pkgNames) {
		fAction = action;
		fPkgNames = pkgNames;
	}
	
	
	public int getAction() {
		return fAction;
	}
	
	public List<String> getPkgNames() {
		return fPkgNames;
	}
	
}
