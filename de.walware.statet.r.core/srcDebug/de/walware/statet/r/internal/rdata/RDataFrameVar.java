/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.IElementName;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;


public final class RDataFrameVar extends RListVar
		implements RDataFrame, ExternalizableRObject {
	
	
	private int fRowCount;
	
	
	public RDataFrameVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final IElementName name) throws IOException, ClassNotFoundException {
		super(in, flags, factory, parent, name);
	}
	
	@Override
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		super.readExternal(in, flags, factory);
		fRowCount = in.readInt();
	}
	
	@Override
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		super.writeExternal(out, flags, factory);
		out.writeInt(fRowCount);
	}
	
	
	@Override
	public int getRObjectType() {
		return TYPE_DATAFRAME;
	}
	
	
	public int getColumnCount() {
		return fComponents.length;
	}
	
	public RCharacterStore getColumnNames() {
		return getNames();
	}
	
	public String getColumnName(final int idx) {
		return getName(idx);
	}
	
	public RStore getColumn(final int idx) {
		return fComponents[idx].getData();
	}
	
//	public void setColumn(final int idx, final RStore column) {
//		throw new UnsupportedOperationException();
//	}
	
	
	public int getRowCount() {
		return fRowCount;
	}
	
	public RCharacterStore getRowNames() {
		throw new UnsupportedOperationException();
	}
	
	public void insertRow(final int idx) {
		throw new UnsupportedOperationException();
	}
	
	public void removeRow(final int idx) {
		throw new UnsupportedOperationException();
	}
	
}
