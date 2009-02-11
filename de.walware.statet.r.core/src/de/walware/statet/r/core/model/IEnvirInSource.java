/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.List;
import java.util.Set;

import de.walware.ecommons.ltk.IModelElement;


public interface IEnvirInSource {
	
	
	public static final int T_PROJ = 1;
	public static final int T_PKG = 2;
	public static final int T_EXPLICIT = 3;
	public static final int T_FUNCTION = 4;
	public static final int T_CLASS = 5;
	
	
	public int getType();
	public String getId();
	
	public boolean containsElement(final String name);
	public Set<String> getElementNames();
	public List<? extends IElementAccess> getAllAccessOfElement(final String name);
	
	public List<? extends IEnvirInSource> getUnderneathEnvirs();
	
	public IModelElement getModelElement();
	
}
