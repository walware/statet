/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.core.model;

import org.eclipse.debug.core.model.IVariable;

import de.walware.ecommons.models.core.util.IElementPartition;


/**
 * Represents a partition of an indexed variable in the Eclipse debug model.
 */
public interface IIndexedVariablePartition extends IVariable, IElementPartition {
	
	
}
