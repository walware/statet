/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.core.impl.AbstractFilePersistenceSourceUnitFactory;

import de.walware.statet.r.internal.sweave.model.LtxRweaveDocUnit;


public class LtxRweavePersistenceUnitFactory extends AbstractFilePersistenceSourceUnitFactory {
	
	
	public LtxRweavePersistenceUnitFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFile file) {
		return new LtxRweaveDocUnit(id, file);
	}
	
}
