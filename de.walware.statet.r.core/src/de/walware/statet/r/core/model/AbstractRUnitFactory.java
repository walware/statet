/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import org.eclipse.core.resources.IFile;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitFactory;
import de.walware.ecommons.ltk.WorkingContext;

import de.walware.statet.base.core.StatetCore;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * 
 */
public abstract class AbstractRUnitFactory implements ISourceUnitFactory {
	
	
	public ISourceUnit getUnit(final Object from, final String typeId, final WorkingContext context, final boolean create) {
		if (context == StatetCore.PERSISTENCE_CONTEXT) {
			if (from instanceof IFile) {
				final IFile fromFile = (IFile) from;
				final String id = RResourceUnit.createResourceId(fromFile);
				final ISourceUnit copy;
				if (typeId.equals(RModel.TYPE_ID)) {
					copy = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(id, context);
				}
				else {
					copy = RCorePlugin.getDefault().getRModelManager().getWorksheetCopy(typeId, id, context);
				}
				if (copy != null) {
					return copy;
				}
				if (create) {
					return createNew(fromFile, context);
				}
			}
			return null;
		}
		if (from instanceof ISourceUnit) {
			final ISourceUnit fromUnit = (ISourceUnit) from;
			final ISourceUnit copy;
			if (typeId.equals(RModel.TYPE_ID)) {
				copy = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(fromUnit.getId(), context);
			}
			else {
				copy = RCorePlugin.getDefault().getRModelManager().getWorksheetCopy(typeId, fromUnit.getId(), context);
			}
			if (copy != null) {
				return copy;
			}
			if (create) {
				return createNew(fromUnit, context);
			}
		}
		return null;
	}
	
	protected abstract ISourceUnit createNew(final IFile file, WorkingContext context);
	
	protected abstract ISourceUnit createNew(final ISourceUnit unit, WorkingContext context);
	
}
