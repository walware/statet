/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;


public final class RConsoleType {
	
	
	private String fName;
	private String fId;
	private boolean fRequireJRE;
	private boolean fSupportsJDebug;
	
	
	public RConsoleType(final String name, final String id, final boolean requireJRE, final boolean supportsJDebug) {
		fName = name;
		fId = id;
		fRequireJRE = requireJRE;
		fSupportsJDebug = supportsJDebug;
	}
	
	
	public String getName() {
		return fName;
	}
	
	public String getId() {
		return fId;
	}
	
	public boolean requireJRE() {
		return fRequireJRE;
	}
	
	public boolean supportsJDebug() {
		return fSupportsJDebug;
	}
	
}
