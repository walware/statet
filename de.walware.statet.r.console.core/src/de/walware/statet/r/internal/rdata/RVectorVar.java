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
	
	
	private DataType data;
	private int length;
	
	private String className1;
	private RStore namesAttribute;
	
	
	public RVectorVar(final DataType data, final int length, final String className1, final String[] initialNames) {
		if (data == null || className1 == null) {
			throw new NullPointerException();
		}
		if ((initialNames != null && initialNames.length != length)
				|| (data.getLength() >= 0 && data.getLength() != length) ) {
			throw new IllegalArgumentException();
		}
		this.data = data;
		this.length = length;
		this.className1 = className1;
		if (initialNames != null) {
			this.namesAttribute = new RCharacterDataImpl(initialNames);
		}
	}
	
	public RVectorVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		fParent = parent;
		fElementName = name;
		readExternal(io, factory);
	}
	
	@Override
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		final int options = io.readInt();
		final boolean customClass = ((options & RObjectFactory.O_CLASS_NAME) != 0);
		//-- special attributes
		if (customClass) {
			this.className1 = io.readString();
		}
		this.length = io.readInt();
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			this.namesAttribute = factory.readNames(io);
		}
		//-- data
		this.data = (DataType) factory.readStore(io);
		if (!customClass) {
			this.className1 = this.data.getBaseVectorRClassName();
		}
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		int options = 0;
		final boolean customClass = !this.className1.equals(this.data.getBaseVectorRClassName());
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		if ((io.flags & RObjectFactory.F_ONLY_STRUCT) == 0 && this.namesAttribute != null) {
			options |= RObjectFactory.O_WITH_NAMES;
		}
		io.writeInt(options);
		//-- special attributes
		if (customClass) {
			io.writeString(this.className1);
		}
		io.writeInt(this.length);
		if ((options & RObjectFactory.O_WITH_NAMES) != 0) {
			factory.writeNames(this.namesAttribute, io);
		}
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
	public int getLength() {
		return this.length;
	}
	
	@Override
	public RStore getNames() {
		return this.namesAttribute;
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
		return Collections.EMPTY_LIST;
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
