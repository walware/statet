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

package de.walware.statet.r.internal.debug.core.model;

import org.eclipse.jdt.annotation.NonNull;

import de.walware.ecommons.debug.core.model.IIndexedValue;

import de.walware.statet.r.core.data.ICombinedRElement;


public interface IRIndexElementValue extends IIndexedValue {
	
	
	@NonNull ICombinedRElement getElement();
	
}
