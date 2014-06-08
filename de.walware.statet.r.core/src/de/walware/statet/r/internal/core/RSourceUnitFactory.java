/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.core.impl.AbstractFilePersistenceSourceUnitFactory;
import de.walware.ecommons.ltk.core.model.ISourceUnit;

import de.walware.statet.r.internal.core.sourcemodel.RSourceUnit;


/**
 * Factory for common R script files
 */
public class RSourceUnitFactory extends AbstractFilePersistenceSourceUnitFactory {
	
	
	public RSourceUnitFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFile file) {
		return new RSourceUnit(id, file);
	}
	
}
