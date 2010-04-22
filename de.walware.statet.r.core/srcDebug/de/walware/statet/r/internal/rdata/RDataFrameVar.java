/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;

import de.walware.statet.r.core.model.RElementName;


public final class RDataFrameVar extends RListVar
		implements RDataFrame, ExternalizableRObject {
	
	
	private RStore rownamesAttribute;
	private int rowCount;
	
	
	public RDataFrameVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		super(in, flags, factory, parent, name);
	}
	
	@Override
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		final int options = super.doReadExternal(in, flags, factory);
		this.rowCount = in.readInt();
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			this.rownamesAttribute = factory.readNames(in, flags);
		}
	}
	
	@Override
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		int options = 0;
		if ((flags & RObjectFactory.F_ONLY_STRUCT) == 0 && this.rownamesAttribute != null) {
			options |= RObjectFactory.O_WITH_NAMES;
		}
		super.doWriteExternal(out, options, flags, factory);
		out.writeInt(this.rowCount);
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			factory.writeNames(this.rownamesAttribute, out, flags);
		}
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_DATAFRAME;
	}
	
	
	public int getColumnCount() {
		return getLength();
	}
	
	public RCharacterStore getColumnNames() {
		return getNames();
	}
	
	public String getColumnName(final int idx) {
		return getName(idx);
	}
	
	public RStore getColumn(final int idx) {
		final RObject obj = get(idx);
		return (obj != null) ? obj.getData() : null;
	}
	
	public RStore getColumn(final String name) {
		final RObject obj = get(name);
		return (obj != null) ? obj.getData() : null;
	}
	
	public int getRowCount() {
		return this.rowCount;
	}
	
	public RStore getRowNames() {
		return this.rownamesAttribute;
	}
	
}
