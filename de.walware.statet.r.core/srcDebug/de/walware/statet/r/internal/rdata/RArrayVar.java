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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RArrayImpl;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class RArrayVar<DataType extends RStore> extends CombinedElement
		implements RArray<DataType>, ExternalizableRObject, IRLangElement {
	
	
	private DataType fData;
	
	private String fClassName;
	private int[] fDimAttribute;
	
	
	public RArrayVar(final DataType data, final String className, final int[] dim) {
		if (data == null || className == null || dim == null) {
			throw new NullPointerException();
		}
		if (data.getLength() >= 0) {
			RArrayImpl.checkDim(data.getLength(), dim);
		}
		fClassName = className;
		fDimAttribute = dim;
		fData = data;
	}
	
	public RArrayVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
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
		
		final int dimCount = in.readInt();
		fDimAttribute = new int[dimCount];
		for (int i = 0; i < dimCount; i++) {
			fDimAttribute[i] = in.readInt();
		}
		
		fData = (DataType) factory.readStore(in, flags);
		
		if (!customClass) {
			fClassName = (dimCount == 2) ? RObject.CLASSNAME_MATRIX : RObject.CLASSNAME_ARRAY;
		}
	}
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		int options = 0;
		final boolean customClass = !fClassName.equals((fDimAttribute.length == 2) ?
				RObject.CLASSNAME_MATRIX : RObject.CLASSNAME_ARRAY);
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		out.writeInt(options);
		
		if (customClass) {
			out.writeUTF(fClassName);
		}
		final int dimCount = fDimAttribute.length;
		out.writeInt(dimCount);
		for (int i = 0; i < dimCount; i++) {
			out.writeInt(fDimAttribute[i]);
		}
		
		factory.writeStore(fData, out, flags);
	}
	
	
	public final int getRObjectType() {
		return TYPE_ARRAY;
	}
	
	public String getRClassName() {
		return fClassName;
	}
	
	public int getLength() {
		if (fDimAttribute.length == 0) {
			return 0;
		}
		int length = 1;
		for (int i = 0; i < fDimAttribute.length; i++) {
			length *= fDimAttribute[i];
		}
		return length;
	}
	
	public RList getDimNames() {
		return null;
	}
	
	public int[] getDim() {
		return fDimAttribute;
	}
	
	public DataType getData() {
		return fData;
	}
	
	
	public void setData(final DataType data) {
		throw new UnsupportedOperationException();
	}
	
	public void setDim(final int[] dim) {
		throw new UnsupportedOperationException();
	}
	
	public void setDimNames(final RList list) {
		throw new UnsupportedOperationException();
	}
	
	public void insert(final int dim, final int idx) {
		throw new UnsupportedOperationException();
	}
	
	public void remove(final int dim, final int idx) {
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
		sb.append("RObject type=array, class=").append(getRClassName());
		sb.append("\n\tlength=").append(getLength());
		sb.append("\n\tdim=").append(Arrays.toString(fDimAttribute));
		sb.append("\n\tdata: ");
		sb.append(fData.toString());
		return sb.toString();
	}
	
}
