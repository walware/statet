/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.debug.core;

import org.eclipse.jdt.annotation.NonNull;

import de.walware.ecommons.debug.core.model.IIndexedVariableItem;


public interface IRIndexedVariableItem extends IRVariable, IIndexedVariableItem {
	
	
	long @NonNull [] getIndex();
	
}
