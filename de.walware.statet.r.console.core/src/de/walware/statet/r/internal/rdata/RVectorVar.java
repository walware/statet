/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class RVectorVar<DataType extends RStore> extends CombinedElement
		implements RVector<DataType>, ExternalizableRObject {
	
	
	private final DataType data;
	private final long length;
	
	private String className1;
	
	
	public RVectorVar(final DataType data, final long length, final String className1,
			final CombinedElement parent, final RElementName name) {
		super(parent, name);
		if (data == null || className1 == null) {
			throw new NullPointerException();
		}
		this.data = data;
		this.length = length;
		this.className1 = className1;
	}
	
	public RVectorVar(final RJIO io, final RObjectFactory factory,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		//-- options
		final int options = io.readInt();
		//-- special attributes
		if ((options & RObjectFactory.O_CLASS_NAME) != 0) {
			this.className1 = io.readString();
		}
		this.length = io.readVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK));
		assert ((options & RObjectFactory.O_WITH_NAMES) == 0);
		//-- data
		this.data = (DataType) factory.readStore(io, this.length);
		if ((options & RObjectFactory.O_CLASS_NAME) == 0) {
			this.className1 = this.data.getBaseVectorRClassName();
		}
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		int options = io.getVULongGrade(this.length);
		if (!this.className1.equals(this.data.getBaseVectorRClassName())) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		io.writeInt(options);
		//-- special attributes
		if ((options & RObjectFactory.O_CLASS_NAME) != 0) {
			io.writeString(this.className1);
		}
		io.writeVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK), this.length);
		//-- data
		factory.writeStore(this.data, io);
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_VECTOR;
	}
	
	@Override
	public String getRClassName() {
		return this.className1;
	}
	
	@Override
	public long getLength() {
		return this.length;
	}
	
	@Override
	public RStore getNames() {
		return null;
	}
	
	
	@Override
	public DataType getData() {
		return this.data;
	}
	
	
	@Override
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.emptyList();
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("RObject type=vector, class=").append(getRClassName());
		sb.append("\n\tlength=").append(getLength());
		sb.append("\n\tdata: ");
		sb.append(this.data.toString());
		return sb.toString();
	}
	
}
