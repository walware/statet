/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.filesystem.IFileStore;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.core.impl.AbstractEditorSourceUnitFactory;

import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.internal.sweave.model.LtxRweaveEditorWorkingCopy;


public final class LtxRweaveEditorUnitFactory extends AbstractEditorSourceUnitFactory {
	
	
	public LtxRweaveEditorUnitFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IWorkspaceSourceUnit su) {
		return new LtxRweaveEditorWorkingCopy((IRWorkspaceSourceUnit) su);
	}
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFileStore file) {
		return null;
	}
	
}
