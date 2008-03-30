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

package de.walware.statet.r.internal.ui;

import org.eclipse.core.resources.IFile;

import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.WorkingContext;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.r.core.rmodel.AbstractRUnitFactory;
import de.walware.statet.r.core.rmodel.IRSourceUnit;


/**
 * 
 */
public class REditorWorkingCopyFactory extends AbstractRUnitFactory {
	
	@Override
	protected ISourceUnit createNew(final IFile file, final WorkingContext context) {
		return null;
	}
	
	@Override
	protected ISourceUnit createNew(final ISourceUnit unit, final WorkingContext context) {
		assert(context == StatetCore.EDITOR_CONTEXT);
		return new REditorWorkingCopy((IRSourceUnit) unit);
	}
	
}
