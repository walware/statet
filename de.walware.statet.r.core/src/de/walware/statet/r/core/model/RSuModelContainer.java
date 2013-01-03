/*******************************************************************************
 * Copyright (c) 2011-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.impl.SourceUnitModelContainer;

import de.walware.statet.r.core.RCore;


public class RSuModelContainer extends SourceUnitModelContainer<IRSourceUnit, IRModelInfo> {
	
	
	public RSuModelContainer(final IRSourceUnit su) {
		super(su);
	}
	
	
	@Override
	protected IModelManager getModelManager() {
		return RCore.getRModelManager();
	}
	
	
}
