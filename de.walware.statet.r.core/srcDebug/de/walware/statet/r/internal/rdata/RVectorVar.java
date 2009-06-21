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
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class RVectorVar<DataType extends RStore> extends CombinedElement
		implements RVector<DataType>, ExternalizableRObject {
	
	
	private DataType fData;
	private int fLength;
	
	private String fClassName;
	private RCharacterDataImpl fNamesAttribute;
	
	
	public RVectorVar(final DataType data, final String className) {
		this(data, className, null);
	}
	
	public RVectorVar(final DataType data, final String className, final String[] initialNames) {
		if (data == null || className == null) {
			throw new NullPointerException();
		}
		if (initialNames != null && data.getLength() >= 0 && initialNames.length != data.getLength()) {
			throw new IllegalArgumentException();
		}
		fData = data;
		fClassName = className;
		if (initialNames != null) {
			fNamesAttribute = new RCharacterDataImpl(initialNames);
		}
	}
	
	public RVectorVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		final int options = in.readInt();
		final boolean customClass = ((options & RObjectFactoryImpl.O_CLASS_NAME) != 0);
		
		if (customClass) {
			fClassName = in.readUTF();
		}
		fLength = in.readInt();
		fData = (DataType) factory.readStore(in, flags);
		
		if (!customClass) {
			fClassName = fData.getBaseVectorRClassName();
		}
	}
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		int options = 0;
		final boolean customClass = !fClassName.equals(fData.getBaseVectorRClassName());
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		out.writeInt(options);
		
		if (customClass) {
			out.writeUTF(fClassName);
		}
		out.writeInt(fLength);
		
		factory.writeStore(fData, out, flags);
	}
	
	
	public int getRObjectType() {
		return TYPE_VECTOR;
	}
	
	public String getRClassName() {
		return fClassName;
	}
	
	public int getLength() {
		return fLength;
	}
	
	public RCharacterStore getNames() {
		return fNamesAttribute;
	}
	
	public void setData(final DataType data) {
		throw new UnsupportedOperationException();
	}
	
	public DataType getData() {
		return fData;
	}
	
	public void insert(final int idx) {
		throw new UnsupportedOperationException();
	}
	
	public void remove(final int idx) {
		throw new UnsupportedOperationException();
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
		sb.append("RObject type=vector, class=").append(getRClassName());
		sb.append("\n\tlength=").append(getLength());
		sb.append("\n\tdata: ");
		sb.append(fData.toString());
		return sb.toString();
	}
	
}
