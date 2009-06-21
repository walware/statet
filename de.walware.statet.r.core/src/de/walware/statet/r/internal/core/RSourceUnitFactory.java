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

package de.walware.statet.r.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitFactory;
import de.walware.ecommons.ltk.WorkingContext;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.internal.core.sourcemodel.RSourceUnit;


/**
 * Factory for common R script files
 */
public class RSourceUnitFactory implements ISourceUnitFactory {
	
	
	final String prefix = "platform:/resource/";
	
	
	public ISourceUnit getUnit(final Object from, final String typeId, final WorkingContext context, final boolean create) {
		if (context == ECommonsLTK.PERSISTENCE_CONTEXT) {
			if (from instanceof IFile) {
				final IFile file = (IFile) from;
				final String id = RSourceUnit.createResourceId(file);
				if (id == null) {
					return null;
				}
				IRSourceUnit u = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(id, ECommonsLTK.PERSISTENCE_CONTEXT);
				if (u == null && create) {
					u = new RSourceUnit(file);
				}
				return u;
			}
			if (from instanceof String) {
				final String id = (String) from;
				IRSourceUnit u = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(id, ECommonsLTK.PERSISTENCE_CONTEXT);
				if (u == null && create && id.startsWith(prefix)) {
					final IPath path = new Path(id.substring(prefix.length()));
					final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					u = new RSourceUnit(file);
				}
				return u;
			}
		}
		return null;
	}
	
}
