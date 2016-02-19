/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.util.Set;

import de.walware.jcommons.collections.ImList;


public interface IRFrameInSource extends IRFrame {
	
	
	Set<String> getAllAccessNames();
	ImList<? extends RElementAccess> getAllAccessOf(final String name, boolean includeSlaves);
	boolean isResolved(String name);
	
}
