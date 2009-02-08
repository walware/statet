/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.WorkingContext;

import de.walware.statet.r.core.model.AbstractRUnitFactory;
import de.walware.statet.r.core.model.IRSourceUnit;


/**
 * R source unit factory for editor context
 */
public final class REditorWorkingCopyFactory extends AbstractRUnitFactory {
	
	
	public REditorWorkingCopyFactory() {
	}
	
	
	@Override
	protected ISourceUnit createNew(final IFile file, final WorkingContext context) {
		return null;
	}
	
	@Override
	protected ISourceUnit createNew(final ISourceUnit unit, final WorkingContext context) {
		assert(context == ECommonsLTK.EDITOR_CONTEXT);
		return new REditorWorkingCopy((IRSourceUnit) unit);
	}
	
	@Override
	protected ISourceUnit createNew(final String id, final IFileStore store, final WorkingContext context) {
		assert(context == ECommonsLTK.EDITOR_CONTEXT);
		return new REditorUriSourceUnit(id, store);
	}
	
}
