/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy2;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;


/**
 * R source unit working copy which can be processed by the model manager.
 */
public class REditorWorkingCopy
		extends GenericEditorWorkspaceSourceUnitWorkingCopy2<RSuModelContainer>
		implements IRWorkspaceSourceUnit {
	
	
	public REditorWorkingCopy(final IRWorkspaceSourceUnit from) {
		super(from);
	}
	
	@Override
	protected RSuModelContainer createModelContainer() {
		return new RUISuModelContainer(this);
	}
	
	
	@Override
	protected final void register() {
		super.register();
		
		if (!getModelTypeId().equals(RModel.TYPE_ID)) {
			RModel.getRModelManager().registerDependentUnit(this);
		}
	}
	
	@Override
	protected final void unregister() {
		super.unregister();
		
		if (!getModelTypeId().equals(RModel.TYPE_ID)) {
			RModel.getRModelManager().deregisterDependentUnit(this);
		}
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return ((IRSourceUnit) getUnderlyingUnit()).getRCoreAccess();
	}
	
}
