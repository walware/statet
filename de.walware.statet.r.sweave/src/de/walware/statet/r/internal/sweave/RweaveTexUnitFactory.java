/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.WorkingContext;

import de.walware.statet.base.core.StatetCore;

import de.walware.statet.r.core.model.AbstractRUnitFactory;
import de.walware.statet.r.internal.sweave.model.RweaveTexDocUnit;
import de.walware.statet.r.internal.sweave.model.RweaveTexEditorWorkingCopy;


public class RweaveTexUnitFactory extends AbstractRUnitFactory {
	
	
	@Override
	protected ISourceUnit createNew(final IFile file, final WorkingContext context) {
		assert (context == StatetCore.PERSISTENCE_CONTEXT);
		return new RweaveTexDocUnit(file);
	}
	
	@Override
	public ISourceUnit createNew(final ISourceUnit from, final WorkingContext context) {
		assert (context == StatetCore.EDITOR_CONTEXT);
		return new RweaveTexEditorWorkingCopy(from);
	}
	
	@Override
	protected ISourceUnit createNew(final String id, final IFileStore store, final WorkingContext context) {
		assert (context == StatetCore.EDITOR_CONTEXT);
		return null;
	}
	
}
