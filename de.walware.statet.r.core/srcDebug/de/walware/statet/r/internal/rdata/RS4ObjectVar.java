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
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ConstList;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RS4Object;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace;


public final class RS4ObjectVar extends CombinedElement
		implements RS4Object, RWorkspace.ICombinedList {
	
	
	private String fClassName;
	private int fDataSlotIdx;
	private RCharacterDataImpl fSlotNames;
	private CombinedElement[] fSlotValues;
	
	
	public RS4ObjectVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		fClassName = in.readUTF();
		fDataSlotIdx = in.readInt();
		fSlotNames = new RCharacterDataImpl(in);
		final int length = fSlotNames.getLength();
		fSlotValues = new CombinedElement[length];
		for (int i = 0; i < length; i++) {
			fSlotValues[i] = CombinedFactory.INSTANCE.readObject(in, flags, this,
					RElementName.create(RElementName.SUB_NAMEDSLOT, fSlotNames.getChar(i)));
		}
	}
	
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		out.writeUTF(fClassName);
		out.writeInt(fDataSlotIdx);
		fSlotNames.writeExternal(out);
		final int length = fSlotNames.getLength();
		for (int i = 0; i < length; i++) {
			factory.writeObject(fSlotValues[i], out, flags);
		}
	}
	
	public byte getRObjectType() {
		return TYPE_S4OBJECT;
	}
	
	public String getRClassName() {
		return fClassName;
	}
	
	public int getLength() {
		return fSlotValues.length;
	}
	
	public boolean hasDataSlot() {
		return (fDataSlotIdx >= 0);
	}
	
	public RObject getDataSlot() {
		return (fDataSlotIdx >= 0) ? fSlotValues[fDataSlotIdx] : null;
	}
	
	public RStore getData() {
		return (fDataSlotIdx >= 0 && fSlotValues[fDataSlotIdx] != null) ?
				fSlotValues[fDataSlotIdx].getData() : null;
	}
	
	public RCharacterStore getNames() {
		return fSlotNames;
	}
	
	public String getName(final int idx) {
		return fSlotNames.getChar(idx);
	}
	
	public RObject get(final int idx) {
		return fSlotValues[idx];
	}
	
	public RObject get(final String name) {
		final int idx = fSlotNames.indexOf(name);
		if (idx >= 0) {
			return fSlotValues[idx];
		}
		throw new IllegalArgumentException();
	}
	
	public void insert(final int idx, final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public void add(final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public void remove(final int idx) {
		throw new UnsupportedOperationException();
	}
	
	public boolean set(final int idx, final RObject value) {
		throw new UnsupportedOperationException();
	}
	
	public boolean set(final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public RObject[] toArray() {
		return null;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	
	public boolean hasModelChildren(final Filter filter) {
		if (filter == null) {
			return (fSlotValues.length > 0);
		}
		else {
			for (final CombinedElement component : fSlotValues) {
				if (filter.include(component)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		if (filter == null) {
			return new ConstList<IRLangElement>(fSlotValues);
		}
		else {
			final List<CombinedElement> list = new ArrayList<CombinedElement>();
			for (final CombinedElement component : fSlotValues) {
				if (filter.include(component)) {
					list.add(component);
				}
			}
			return list;
		}
	}
	
}
