/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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


/**
 * 
 */
public interface IPackageReferences {
	
	public Set<String> getAllPackageNames();
	
	public boolean isImported(final String name);
	
	public List<? extends RElementAccess> getAllAccessOfPackage(
			final String name);
	
}
