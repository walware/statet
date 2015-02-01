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
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RIntegerDataImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class RArrayVar<DataType extends RStore> extends CombinedElement
		implements RArray<DataType>, ExternalizableRObject {
	
	
	private final long length;
	private final DataType data;
	
	private String className1;
	private final RIntegerDataImpl dimAttribute;
	
	
	public RArrayVar(final DataType data, final String className1, final int[] dim,
			final CombinedElement parent, final RElementName name) {
		super(parent, name);
		if (data == null || className1 == null || dim == null) {
			throw new NullPointerException();
		}
		this.length = RDataUtil.computeLengthFromDim(dim);
		if (data.getLength() >= 0 && data.getLength() != this.length) {
			throw new IllegalArgumentException("dim");
		}
		this.className1 = className1;
		this.dimAttribute = new RIntegerDataImpl(dim);
		this.data = data;
	}
	
	public RArrayVar(final RJIO io, final RObjectFactory factory,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		//-- options
		final int options = io.readInt();
		//-- special attributes
		if ((options & RObjectFactory.O_CLASS_NAME) != 0) {
			this.className1 = io.readString();
		}
		this.length = io.readVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK));
		final int[] dim = io.readIntArray();
		this.dimAttribute = new RIntegerDataImpl(dim);
		assert ((options & RObjectFactory.O_WITH_NAMES) == 0);
		//-- data
		this.data = (DataType) factory.readStore(io, this.length);
		
		if ((options & RObjectFactory.O_CLASS_NAME) == 0) {
			this.className1 = (dim.length == 2) ? RObject.CLASSNAME_MATRIX : RObject.CLASSNAME_ARRAY;
		}
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		final int n = (int) this.dimAttribute.getLength();
		//-- options
		int options = io.getVULongGrade(this.length);
		if (!this.className1.equals((n == 2) ?
				RObject.CLASSNAME_MATRIX : RObject.CLASSNAME_ARRAY )) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		io.writeInt(options);
		//-- special attributes
		if ((options & RObjectFactory.O_CLASS_NAME) != 0) {
			io.writeString(this.className1);
		}
		io.writeVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK), this.length);
		io.writeInt(n);
		this.dimAttribute.writeExternal(io);
		//-- data
		factory.writeStore(this.data, io);
	}
	
	
	@Override
	public final byte getRObjectType() {
		return TYPE_ARRAY;
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
	public RIntegerStore getDim() {
		return this.dimAttribute;
	}
	
	@Override
	public RCharacterStore getDimNames() {
		return null;
	}
	
	@Override
	public RStore getNames(final int dim) {
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
		sb.append("RObject type=array, class=").append(getRClassName());
		sb.append("\n\tlength=").append(getLength());
		sb.append("\n\tdim=");
		this.dimAttribute.appendTo(sb);
		sb.append("\n\tdata: ");
		sb.append(this.data.toString());
		return sb.toString();
	}
	
}
