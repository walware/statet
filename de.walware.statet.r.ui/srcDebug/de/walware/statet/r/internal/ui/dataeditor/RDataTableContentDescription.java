/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import de.walware.rj.data.RObject;

import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataTableContentDescription {
	
	
	public final RObject struct;
	
	public RDataFormatter defaultDataFormatter;
	
	public RDataTableColumn rowHeaderColumn;
	
	public RDataTableColumn[] dataColumns;
	
	
	public RDataTableContentDescription(final RObject struct) {
		this.struct = struct;
	}
	
	
	@Override
	public int hashCode() {
		return 986986;
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof RDataTableContentDescription);
	}
	
}
