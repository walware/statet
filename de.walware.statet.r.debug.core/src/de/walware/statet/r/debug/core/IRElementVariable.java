/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.debug.core;

import org.eclipse.debug.core.model.IVariable;

import de.walware.statet.r.core.data.ICombinedRElement;


/**
 * Represents a main variable (not dim or index) associated with an {@link ICombinedRElement}
 * in the Eclipse debug model for R.
 */
public interface IRElementVariable extends IVariable {
	
	
	ICombinedRElement getElement();
	
}
