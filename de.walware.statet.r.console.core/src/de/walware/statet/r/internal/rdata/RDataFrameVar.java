/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
	
	
	private long rowCount;
	
	
	public RDataFrameVar(final RJIO io, final CombinedFactory factory, final int options,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(io, factory, options, parent, name);
		
		this.rowCount = io.readLong();
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		int options = 0;
		super.doWriteExternal(io, factory, options);
		io.writeLong(this.rowCount);
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_DATAFRAME;
	}
	
	@Override
	protected String getDefaultRClassName() {
		return RObject.CLASSNAME_DATAFRAME;
	}
	
	
	@Override
	public long getColumnCount() {
		return getLength();
	}
	
	@Override
	public RCharacterStore getColumnNames() {
		return getNames();
	}
	
	@Override
	public RStore getColumn(final int idx) {
		final RObject obj = get(idx);
		return (obj != null) ? obj.getData() : null;
	}
	
	@Override
	public RStore getColumn(final long idx) {
		final RObject obj = get(idx);
		return (obj != null) ? obj.getData() : null;
	}
	
	@Override
	public RStore getColumn(final String name) {
		final RObject obj = get(name);
		return (obj != null) ? obj.getData() : null;
	}
	
	@Override
	public long getRowCount() {
		return this.rowCount;
	}
	
	@Override
	public RStore getRowNames() {
		return null;
	}
	
}
