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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace;


public class RListVar extends CombinedElement
		implements RList, RWorkspace.ICombinedList, ExternalizableRObject {
	
	
	protected CombinedElement[] fComponents;
	private int fLength;
	
	private String fClassName;
	private RCharacterDataImpl fNamesAttribute;
	
	
//	public RListVar(final CombinedElement[] initialComponents, String[] initialNames) {
//		fComponents = initialComponents;
//		if (initialNames == null) {
//			initialNames = new String[fComponents.length];
//		}
//		fClassName =
//		fNamesAttribute = new RCharacterDataImpl(initialNames, fComponents.length);
//	}
//	
	public RListVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		final int options = in.readInt();
		
		fClassName = ((options & RObjectFactoryImpl.O_CLASS_NAME) != 0) ?
				in.readUTF() : ((getRObjectType() == RObject.TYPE_DATAFRAME) ?
						RObject.CLASSNAME_DATAFRAME : RObject.CLASSNAME_LIST);
		final int length = fLength = in.readInt();
		
		if ((options & RObjectFactory.O_NOCHILDREN) == 0) {
			fNamesAttribute = new RCharacterDataImpl(in);
			fComponents = new CombinedElement[length];
			for (int i = 0; i < length; i++) {
				fComponents[i] = CombinedFactory.INSTANCE.readObject(in, flags, this,
						(fNamesAttribute.isNA(i) || fNamesAttribute.getChar(i).length() == 0) ? 
								RElementName.create(RElementName.SUB_INDEXED_D, Integer.toString(i+1)) :
								RElementName.create(RElementName.SUB_NAMEDPART, fNamesAttribute.getChar(i), i+1) );
			}
		}
		
		if ((options & RObjectFactoryImpl.F_WITH_ATTR) != 0) {
			fAttributes = factory.readAttributeList(in, flags);
		}
	}
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		int options = 0;
		final boolean customClass = !((getRObjectType() == TYPE_DATAFRAME) ?
				fClassName.equals(RObject.CLASSNAME_DATAFRAME) : fClassName.equals(RObject.CLASSNAME_LIST));
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		final boolean withAttr = ((flags & RObjectFactoryImpl.F_WITH_ATTR) != 0) && (fAttributes != null);
		if (withAttr) {
			options |= RObjectFactory.O_WITH_ATTR;
		}
		if (fComponents == null) {
			options |= RObjectFactory.F_NOCHILDREN;
		}
		out.writeInt(options);
		
		if (customClass) {
			out.writeUTF(fClassName);
		}
		out.writeInt(fLength);
		
		if (fComponents != null) {
			fNamesAttribute.writeExternal(out);
			
			for (int i = 0; i < fLength; i++) {
				factory.writeObject(this.fComponents[i], out, flags);
			}
		}
		
		if (withAttr) {
			factory.writeAttributeList(fAttributes, out, flags);
		}
	}
	
	
	public byte getRObjectType() {
		return TYPE_LIST;
	}
	
	public final String getRClassName() {
		return fClassName;
	}
	
	
	public final RCharacterStore getNames() {
		return fNamesAttribute;
	}
	
	public final String getName(final int idx) {
		return fNamesAttribute.getChar(idx);
	}
	
	public final RObject get(final int idx) {
		return fComponents[idx];
	}
	
	public final boolean set(final int idx, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public final void insert(final int idx, final String label, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public final void add(final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public final void remove(final int idx) {
		throw new UnsupportedOperationException();
	}
	
	
	public final RObject get(final String name) {
		final int idx = fNamesAttribute.indexOf(name);
		if (idx >= 0) {
			return fComponents[idx];
		}
		return null;
	}
	
	public final boolean set(final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public final RStore getData() {
		return null;
	}
	
	public final RObject[] toArray() {
		final RObject[] array = new RObject[fComponents.length];
		System.arraycopy(fComponents, 0, array, 0, fComponents.length);
		return array;
	}
	
	
	public final int getLength() {
		return fLength;
	}
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	
	public final boolean hasModelChildren(final Filter filter) {
		if (fComponents == null) {
			return false;
		}
		if (filter == null) {
			return (fLength > 0);
		}
		else {
			for (final CombinedElement component : fComponents) {
				if (filter.include(component)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public final List<? extends IRLangElement> getModelChildren(final Filter filter) {
		if (fComponents == null) {
			return Collections.EMPTY_LIST;
		}
		if (filter == null) {
			return Arrays.asList(fComponents);
		}
		else {
			final List<CombinedElement> list = new ArrayList<CombinedElement>();
			for (final CombinedElement component : fComponents) {
				if (filter.include(component)) {
					list.add(component);
				}
			}
			return list;
		}
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("RObject type=list, class=").append(getRClassName());
		sb.append("\n\tlength=").append(fLength);
		if (fComponents != null) {
			sb.append("\n\tdata: ");
			for (int i = 0; i < fLength; i++) {
				if (fNamesAttribute.isNA(i)) {
					sb.append("\n[[").append(i).append("]]\n");
				}
				else {
					sb.append("\n$").append(fNamesAttribute.getChar(i)).append("\n");
				}
				sb.append(fComponents[i]);
			}
		}
		else {
			sb.append("\n<NODATA/>");
		}
		return sb.toString();
	}
	
}
