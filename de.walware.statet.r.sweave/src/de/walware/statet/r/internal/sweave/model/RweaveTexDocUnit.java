/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.resources.IResource;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.sweave.Sweave;


public class RweaveTexDocUnit extends RResourceUnit {
	
	
	public RweaveTexDocUnit(final IResource file) {
		super(file);
	}
	
	@Override
	public String getModelTypeId() {
		return Sweave.R_TEX_UNIT_TYPE_ID;
	}
	
	@Override
	protected void init() {
		register();
	}
	
	@Override
	protected void dispose() {
		unregister();
	}
	
}
