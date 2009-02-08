/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.GenericSourceUnitWorkingCopy;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ast.IAstNode;

import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * 
 */
public abstract class RWorkingCopy extends GenericSourceUnitWorkingCopy {
	
	
	protected RWorkingCopy(final ISourceUnit from) {
		super(from);
	}
	
	
	@Override
	protected final void register() {
		if (getModelTypeId().equals(RModel.TYPE_ID)) {
			RCorePlugin.getDefault().getRModelManager().registerWorkingCopy((IRSourceUnit) this);
		}
		else {
			RCorePlugin.getDefault().getRModelManager().registerWorksheetCopy(this);
		}
	}
	
	@Override
	protected final void unregister() {
		if (getModelTypeId().equals(RModel.TYPE_ID)) {
			RCorePlugin.getDefault().getRModelManager().removeWorkingCopy((IRSourceUnit) this);
		}
		else {
			RCorePlugin.getDefault().getRModelManager().removeWorksheetCopy(this);
		}
	}
	
	public AstInfo<? extends IAstNode> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		return null;
	}
	
}
