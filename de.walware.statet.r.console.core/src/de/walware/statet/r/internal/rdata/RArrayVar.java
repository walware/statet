/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.rj.data.RArray;
import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.ExternalizableRStore;
import de.walware.rj.data.defaultImpl.RArrayImpl;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RIntegerDataImpl;
import de.walware.rj.data.defaultImpl.SimpleRListImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class RArrayVar<DataType extends RStore> extends CombinedElement
		implements RArray<DataType>, ExternalizableRObject {
	
	
	private DataType data;
	
	private String className1;
	private RIntegerDataImpl dimAttribute;
	private SimpleRListImpl<RStore> dimnamesAttribute;
	
	
	public RArrayVar(final DataType data, final String className1, final int[] dim) {
		if (data == null || className1 == null || dim == null) {
			throw new NullPointerException();
		}
		if (data.getLength() >= 0) {
			RArrayImpl.checkDim(data.getLength(), dim);
		}
		this.className1 = className1;
		this.dimAttribute = new RIntegerDataImpl(dim);
		this.data = data;
	}
	
	public RArrayVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		fParent = parent;
		fElementName = name;
		readExternal(io, factory);
	}
	
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		final int options = io.readInt();
		final boolean customClass = ((options & RObjectFactory.O_CLASS_NAME) != 0);
		//-- special attributes
		if (customClass) {
			this.className1 = io.readString();
		}
		final int[] dim = io.readIntArray();
		this.dimAttribute = new RIntegerDataImpl(dim);
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			final RCharacterDataImpl names0 = new RCharacterDataImpl(io);
			final RStore[] names1 = new RStore[dim.length];
			for (int i = 0; i < dim.length; i++) {
				names1[i] = factory.readNames(io);
			}
			this.dimnamesAttribute = new SimpleRListImpl<RStore>(names0, names1);
		}
		//-- data
		this.data = (DataType) factory.readStore(io);
		
		if (!customClass) {
			this.className1 = (dim.length == 2) ? RObject.CLASSNAME_MATRIX : RObject.CLASSNAME_ARRAY;
		}
	}
	
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		int options = 0;
		final boolean customClass = !this.className1.equals((this.dimAttribute.getLength() == 2) ?
				RObject.CLASSNAME_MATRIX : RObject.CLASSNAME_ARRAY);
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		if ((io.flags & RObjectFactory.F_ONLY_STRUCT) == 0 && this.dimnamesAttribute != null) {
			options |= RObjectFactory.O_WITH_NAMES;
		}
		io.writeInt(options);
		//-- special attributes
		if (customClass) {
			io.writeString(this.className1);
		}
		this.dimAttribute.writeExternal(io);
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			((ExternalizableRStore) this.dimnamesAttribute.getNames()).writeExternal(io);
			for (int i = 0; i < this.dimnamesAttribute.getLength(); i++) {
				factory.writeNames(this.dimnamesAttribute.get(i), io);
			}
		}
		//-- data
		factory.writeStore(this.data, io);
	}
	
	
	public final byte getRObjectType() {
		return TYPE_ARRAY;
	}
	
	public String getRClassName() {
		return this.className1;
	}
	
	public int getLength() {
		if (this.dimAttribute.getLength() == 0) {
			return 0;
		}
		int length = this.data.getLength();
		if (length >= 0) {
			return length;
		}
		length = 1;
		for (int i = 0; i < this.dimAttribute.getLength(); i++) {
			length *= this.dimAttribute.getInt(i);
		}
		return length;
	}
	
	public RIntegerStore getDim() {
		return this.dimAttribute;
	}
	
	public RCharacterStore getDimNames() {
		if (this.dimnamesAttribute != null) {
			return this.dimnamesAttribute.getNames();
		}
		return null;
	}
	
	public RStore getNames(final int dim) {
		if (this.dimnamesAttribute != null) {
			return this.dimnamesAttribute.get(dim);
		}
		return null;
	}
	
	
	public DataType getData() {
		return this.data;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.EMPTY_LIST;
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
