/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.AbstractEditorSourceUnitFactory;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitStateListener;

import de.walware.statet.r.core.model.IRSourceUnit;


/**
 * R source unit factory for editor context
 */
public final class REditorWorkingCopyFactory extends AbstractEditorSourceUnitFactory {
	
	
	public REditorWorkingCopyFactory() {
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final ISourceUnit su, final ISourceUnitStateListener callback) {
		return new REditorWorkingCopy((IRSourceUnit) su, callback);
	}
	
	@Override
	protected ISourceUnit createSourceUnit(final String id, final IFileStore file, final ISourceUnitStateListener callback) {
		return new REditorUriSourceUnit(id, file, callback);
	}
	
}
