/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.AbstractFilePersistenceSourceUnitFactory;
import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.r.internal.sweave.model.RweaveTexDocUnit;


public class RweaveTexPersistenceUnitFactory extends AbstractFilePersistenceSourceUnitFactory {
	
	
	public RweaveTexPersistenceUnitFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFile file) {
		return new RweaveTexDocUnit(id, file);
	}
	
}
