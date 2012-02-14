/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;


public class RWorkspaceConfig {
	
	
	private boolean fEnableObjectDB;
	
	private boolean fEnableAutoRefresh;
	
	
	public RWorkspaceConfig() {
	}
	
	
	public void setEnableObjectDB(final boolean enableObjectDB) {
		fEnableObjectDB = enableObjectDB;
	}
	
	public boolean getEnableObjectDB() {
		return fEnableObjectDB;
	}
	
	public void setEnableAutoRefresh(final boolean enableAutoRefresh) {
		fEnableAutoRefresh = enableAutoRefresh;
	}
	
	public boolean getEnableAutoRefresh() {
		return fEnableAutoRefresh;
	}
	
	
}
