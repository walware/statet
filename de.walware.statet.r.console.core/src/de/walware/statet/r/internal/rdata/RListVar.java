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
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.ConstList;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class RListVar extends CombinedElement
		implements RList, RWorkspace.ICombinedList, ExternalizableRObject {
	
	
	private CombinedElement[] components;
	private int length;
	
	private String className1;
	private RCharacterDataImpl namesAttribute;
	
	
	public RListVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		fParent = parent;
		fElementName = name;
		readExternal(io, factory);
	}
	
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		doReadExternal(io, factory);
	}
	protected final int doReadExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		final int options = io.in.readInt();
		//-- special attributes
		this.className1 = ((options & RObjectFactory.O_CLASS_NAME) != 0) ?
				io.readString() : ((getRObjectType() == RObject.TYPE_DATAFRAME) ?
						RObject.CLASSNAME_DATAFRAME : RObject.CLASSNAME_LIST);
		final int length = this.length = io.in.readInt();
		
		if ((options & RObjectFactory.O_NO_CHILDREN) != 0) {
			this.namesAttribute = null;
			this.components = null;
		}
		else {
			this.namesAttribute = (RCharacterDataImpl) CombinedFactory.INSTANCE.readNames(io);
			//-- data
			this.components = new CombinedElement[length];
			for (int i = 0; i < length; i++) {
				this.components[i] = CombinedFactory.INSTANCE.readObject(io, this,
						(this.namesAttribute.isNA(i) || this.namesAttribute.getChar(i).length() == 0) ? 
								RElementName.create(RElementName.SUB_INDEXED_D, Integer.toString(i+1), i+1) :
								RElementName.create(RElementName.SUB_NAMEDPART, this.namesAttribute.getChar(i), i+1) );
			}
		}
		return options;
	}
	
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		doWriteExternal(io, 0, factory);
	}
	protected final void doWriteExternal(final RJIO io, int options, final RObjectFactory factory) throws IOException {
		//-- options
		final boolean customClass = !((getRObjectType() == TYPE_DATAFRAME) ?
				this.className1.equals(RObject.CLASSNAME_DATAFRAME) : this.className1.equals(RObject.CLASSNAME_LIST));
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		if (this.components == null) {
			options |= RObjectFactory.O_NO_CHILDREN;
		}
		io.out.writeInt(options);
		//-- special attributes
		if (customClass) {
			io.writeString(this.className1);
		}
		io.out.writeInt(this.length);
		
		if (this.components != null) {
			factory.writeNames(this.namesAttribute, io);
			//-- data
			for (int i = 0; i < this.length; i++) {
				factory.writeObject(this.components[i], io);
			}
		}
	}
	
	
	public byte getRObjectType() {
		return TYPE_LIST;
	}
	
	public final String getRClassName() {
		return this.className1;
	}
	
	
	public int getLength() {
		return this.length;
	}
	
	public final RCharacterStore getNames() {
		return this.namesAttribute;
	}
	
	public final String getName(final int idx) {
		if (this.namesAttribute != null) {
			return this.namesAttribute.getChar(idx);
		}
		return null;
	}
	
	public final RObject get(final int idx) {
		return this.components[idx];
	}
	
	public final RObject get(final String name) {
		if (this.namesAttribute != null) {
			final int idx = this.namesAttribute.indexOf(name);
			if (idx >= 0) {
				return this.components[idx];
			}
		}
		return null;
	}
	
	public final RObject[] toArray() {
		final RObject[] array = new RObject[this.length];
		System.arraycopy(this.components, 0, array, 0, this.length);
		return array;
	}
	
	public final RStore getData() {
		return null;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	public final boolean hasModelChildren(final Filter filter) {
		if (this.components == null) {
			return false;
		}
		if (filter == null) {
			return (this.length > 0);
		}
		else {
			for (final CombinedElement component : this.components) {
				if (filter.include(component)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public final List<? extends IRLangElement> getModelChildren(final Filter filter) {
		if (this.components == null) {
			return Collections.emptyList();
		}
		if (filter == null) {
			return new ConstList<IRLangElement>(this.components);
		}
		else {
			final List<CombinedElement> list = new ArrayList<CombinedElement>();
			for (final CombinedElement component : this.components) {
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
		sb.append("\n\tlength=").append(this.length);
		if (this.components != null) {
			sb.append("\n\tdata: ");
			for (int i = 0; i < this.length; i++) {
				if (this.namesAttribute == null || this.namesAttribute.isNA(i)) {
					sb.append("\n[[").append(i).append("]]\n");
				}
				else {
					sb.append("\n$").append(this.namesAttribute.getChar(i)).append("\n");
				}
				sb.append(this.components[i]);
			}
		}
		else {
			sb.append("\n<NODATA/>");
		}
		return sb.toString();
	}
	
}
