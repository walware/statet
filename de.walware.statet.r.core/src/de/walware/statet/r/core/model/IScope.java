/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;


/**
 * 
 */
public interface IScope {
	
	
	public static final int T_PROJ = 1;
	public static final int T_PKG = 2;
	public static final int T_EXPLICIT = 3;
	public static final int T_FUNCTION = 4;
	
	
	public String getId();
	
	public boolean containsElement(final String name);
	
}
