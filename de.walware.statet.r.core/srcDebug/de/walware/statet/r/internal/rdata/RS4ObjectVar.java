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
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ConstList;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RS4Object;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace;


public final class RS4ObjectVar extends CombinedElement
		implements RS4Object, RWorkspace.ICombinedList, ExternalizableRObject {
	
	
	private String className;
	
	private RCharacterDataImpl slotNames;
	private CombinedElement[] slotValues;
	private int dataSlotIdx;
	
	
	public RS4ObjectVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		fParent = parent;
		fElementName = name;
		readExternal(io, factory);
	}
	
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		this.className = io.readString();
		this.dataSlotIdx = io.in.readInt();
		this.slotNames = new RCharacterDataImpl(io);
		final int length = this.slotNames.getLength();
		this.slotValues = new CombinedElement[length];
		for (int i = 0; i < length; i++) {
			this.slotValues[i] = CombinedFactory.INSTANCE.readObject(io, this,
					RElementName.create(RElementName.SUB_NAMEDSLOT, slotNames.getChar(i)));
		}
	}
	
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		io.writeString(this.className);
		io.out.writeInt(this.dataSlotIdx);
		this.slotNames.writeExternal(io);
		final int length = this.slotNames.getLength();
		for (int i = 0; i < length; i++) {
			factory.writeObject(this.slotValues[i], io);
		}
	}
	
	public byte getRObjectType() {
		return TYPE_S4OBJECT;
	}
	
	public String getRClassName() {
		return this.className;
	}
	
	
	public int getLength() {
		return this.slotValues.length;
	}
	
	
	public boolean hasDataSlot() {
		return (this.dataSlotIdx >= 0);
	}
	
	public RObject getDataSlot() {
		return (this.dataSlotIdx >= 0) ? this.slotValues[this.dataSlotIdx] : null;
	}
	
	public RStore getData() {
		return (this.dataSlotIdx >= 0 && this.slotValues[this.dataSlotIdx] != null) ?
				this.slotValues[this.dataSlotIdx].getData() : null;
	}
	
	public RCharacterStore getNames() {
		return this.slotNames;
	}
	
	public String getName(final int idx) {
		return this.slotNames.getChar(idx);
	}
	
	public RObject get(final int idx) {
		return this.slotValues[idx];
	}
	
	public RObject get(final String name) {
		final int idx = this.slotNames.indexOf(name);
		if (idx >= 0) {
			return this.slotValues[idx];
		}
		throw new IllegalArgumentException();
	}
	
	public final RObject[] toArray() {
		final RObject[] array = new RObject[this.slotValues.length];
		System.arraycopy(this.slotValues, 0, array, 0, this.slotValues.length);
		return array;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	public boolean hasModelChildren(final Filter filter) {
		if (filter == null) {
			return (slotValues.length > 0);
		}
		else {
			for (final CombinedElement component : slotValues) {
				if (filter.include(component)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		if (filter == null) {
			return new ConstList<IRLangElement>(slotValues);
		}
		else {
			final List<CombinedElement> list = new ArrayList<CombinedElement>();
			for (final CombinedElement component : slotValues) {
				if (filter.include(component)) {
					list.add(component);
				}
			}
			return list;
		}
	}
	
}
