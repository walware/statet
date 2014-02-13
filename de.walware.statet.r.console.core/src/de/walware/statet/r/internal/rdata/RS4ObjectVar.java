/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
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
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RS4Object;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class RS4ObjectVar extends CombinedElement
		implements RS4Object, RWorkspace.ICombinedRList, ExternalizableRObject {
	
	
	private final String className;
	
	private final RCharacterDataImpl slotNames;
	private final CombinedElement[] slotValues;
	private final int dataSlotIdx;
	
	
	public RS4ObjectVar(final RJIO io, final CombinedFactory factory,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		//-- options
		final int options = io.readInt();
		//-- special attributes
		this.className = io.readString();
		//-- data
		final int l = (int) io.readVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK));
		
		this.dataSlotIdx = io.readInt();
		this.slotNames = new RCharacterDataImpl(io, l);
		this.slotValues = new CombinedElement[l];
		for (int i = 0; i < l; i++) {
			this.slotValues[i] = factory.readObject(io, this,
					RElementName.create(RElementName.SUB_NAMEDSLOT, slotNames.getChar(i)));
		}
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		final int l = this.slotValues.length;
		//-- options
		final int options = io.getVULongGrade(l);
		io.writeInt(options);
		//-- special attributes
		io.writeString(this.className);
		//-- data
		io.writeVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK), l);
		
		io.writeInt(this.dataSlotIdx);
		this.slotNames.writeExternal(io);
		for (int i = 0; i < l; i++) {
			factory.writeObject(this.slotValues[i], io);
		}
	}
	
	@Override
	public byte getRObjectType() {
		return TYPE_S4OBJECT;
	}
	
	@Override
	public String getRClassName() {
		return this.className;
	}
	
	
	@Override
	public long getLength() {
		return this.slotValues.length;
	}
	
	@Override
	public boolean hasDataSlot() {
		return (this.dataSlotIdx >= 0);
	}
	
	@Override
	public RObject getDataSlot() {
		return (this.dataSlotIdx >= 0) ? this.slotValues[this.dataSlotIdx] : null;
	}
	
	@Override
	public RStore getData() {
		return (this.dataSlotIdx >= 0 && this.slotValues[this.dataSlotIdx] != null) ?
				this.slotValues[this.dataSlotIdx].getData() : null;
	}
	
	@Override
	public RCharacterStore getNames() {
		return this.slotNames;
	}
	
	@Override
	public String getName(final int idx) {
		return this.slotNames.getChar(idx);
	}
	
	@Override
	public String getName(final long idx) {
		return this.slotNames.getChar(idx);
	}
	
	@Override
	public ICombinedRElement get(final int idx) {
		return this.slotValues[idx];
	}
	
	@Override
	public ICombinedRElement get(final long idx) {
		if (idx < 0 || idx >= Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException(Long.toString(idx));
		}
		return this.slotValues[(int) idx];
	}
	
	@Override
	public ICombinedRElement get(final String name) {
		final int idx = this.slotNames.indexOf(name, 0);
		if (idx >= 0) {
			return this.slotValues[idx];
		}
		throw new IllegalArgumentException();
	}
	
	
	@Override
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	@Override
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
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		if (filter == null) {
			return new ConstArrayList<IRLangElement>(slotValues);
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
