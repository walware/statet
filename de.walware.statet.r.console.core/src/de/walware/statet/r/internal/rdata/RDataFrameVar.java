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

package de.walware.statet.r.internal.rdata;

import java.io.IOException;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;

import de.walware.statet.r.core.model.RElementName;


public final class RDataFrameVar extends RListVar
		implements RDataFrame, ExternalizableRObject {
	
	
	private RStore rownamesAttribute;
	private int rowCount;
	
	
	public RDataFrameVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		super(io, factory, parent, name);
	}
	
	@Override
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		final int options = super.doReadExternal(io, factory);
		this.rowCount = io.readInt();
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			this.rownamesAttribute = factory.readNames(io);
		}
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		int options = 0;
		if ((io.flags & RObjectFactory.F_ONLY_STRUCT) == 0 && this.rownamesAttribute != null) {
			options |= RObjectFactory.O_WITH_NAMES;
		}
		super.doWriteExternal(io, options, factory);
		io.writeInt(this.rowCount);
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			factory.writeNames(this.rownamesAttribute, io);
		}
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_DATAFRAME;
	}
	
	
	@Override
	public int getColumnCount() {
		return getLength();
	}
	
	@Override
	public RCharacterStore getColumnNames() {
		return getNames();
	}
	
	public String getColumnName(final int idx) {
		return getName(idx);
	}
	
	@Override
	public RStore getColumn(final int idx) {
		final RObject obj = get(idx);
		return (obj != null) ? obj.getData() : null;
	}
	
	@Override
	public RStore getColumn(final String name) {
		final RObject obj = get(name);
		return (obj != null) ? obj.getData() : null;
	}
	
	@Override
	public int getRowCount() {
		return this.rowCount;
	}
	
	@Override
	public RStore getRowNames() {
		return this.rownamesAttribute;
	}
	
}
