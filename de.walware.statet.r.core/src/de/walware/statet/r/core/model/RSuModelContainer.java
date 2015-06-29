/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.impl.SourceUnitModelContainer;


public class RSuModelContainer extends SourceUnitModelContainer<IRSourceUnit, IRModelInfo> {
	
	
	public RSuModelContainer(final IRSourceUnit sourceUnit) {
		super(sourceUnit);
	}
	
	
	@Override
	public boolean isContainerFor(final String modelTypeId) {
		return (modelTypeId == RModel.TYPE_ID);
	}
	
	@Override
	public Class<?> getAdapterClass() {
		return RSuModelContainer.class;
	}
	
	@Override
	protected IModelManager getModelManager() {
		return RModel.getRModelManager();
	}
	
}
