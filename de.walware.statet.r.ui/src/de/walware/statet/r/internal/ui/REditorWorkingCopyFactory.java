/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.core.filesystem.IFileStore;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.IWorkspaceSourceUnit;
import de.walware.ecommons.ltk.core.impl.AbstractEditorSourceUnitFactory;
import de.walware.ecommons.text.ISourceFragment;

import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;


/**
 * R source unit factory for editor context
 */
public final class REditorWorkingCopyFactory extends AbstractEditorSourceUnitFactory {
	
	
	public REditorWorkingCopyFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IWorkspaceSourceUnit su) {
		return new REditorWorkingCopy((IRWorkspaceSourceUnit) su);
	}
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFileStore file) {
		return new REditorUriSourceUnit(id, file);
	}
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final ISourceFragment fragment) {
		return new RFragmentSourceUnit(id, fragment);
	}
	
}
