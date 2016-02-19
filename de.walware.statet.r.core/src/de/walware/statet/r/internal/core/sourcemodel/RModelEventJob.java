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

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.ecommons.ltk.IModelElementDelta;
import de.walware.ecommons.ltk.core.impl.AbstractModelEventJob;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;


/**
 * R model update event job
 */
public class RModelEventJob extends AbstractModelEventJob<IRSourceUnit, IRModelInfo> {
	
	
	RModelEventJob(final RModelManager manager) {
		super(manager);
	}
	
	
	@Override
	protected IModelElementDelta createDelta(final Task task) {
		return new ModelDelta(task.getElement(), task.getOldInfo(), task.getNewInfo());
	}
	
	@Override
	protected void dispose() {
		super.dispose();
	}
	
}
