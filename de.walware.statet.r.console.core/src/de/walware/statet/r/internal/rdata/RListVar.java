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
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class RListVar extends CombinedElement
		implements RList, RWorkspace.ICombinedRList, ExternalizableRObject {
	
	
	private int length;
	private CombinedElement[] components;
	
	private String className1;
	private RCharacterDataImpl namesAttribute;
	
	
	public RListVar(final RJIO io, final CombinedFactory factory, final int options,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		//-- options
		//-- special attributes
		this.className1 = ((options & RObjectFactory.O_CLASS_NAME) != 0) ?
				io.readString() : ((getRObjectType() == RObject.TYPE_DATAFRAME) ?
						RObject.CLASSNAME_DATAFRAME : RObject.CLASSNAME_LIST);
		final int l = this.length = (int) io.readVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK));
		
		if ((options & RObjectFactory.O_NO_CHILDREN) != 0) {
			this.namesAttribute = null;
			this.components = null;
		}
		else {
			this.namesAttribute = (RCharacterDataImpl) factory.readNames(io, l);
			//-- data
			this.components = new CombinedElement[l];
			for (int i = 0; i < l; i++) {
				this.components[i] = factory.readObject(io, this,
						(this.namesAttribute.isNA(i) || this.namesAttribute.getChar(i).isEmpty()) ? 
								RElementName.create(RElementName.SUB_INDEXED_D, Integer.toString(i+1), i+1) :
								RElementName.create(RElementName.SUB_NAMEDPART, this.namesAttribute.getChar(i), i+1) );
			}
		}
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		doWriteExternal(io, factory, 0);
	}
	protected final void doWriteExternal(final RJIO io, final RObjectFactory factory, int options) throws IOException {
		final int l = this.length;
		//-- options
		options |= io.getVULongGrade(l);
		if (!this.className1.equals(getDefaultRClassName())) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		if (this.components == null) {
			options |= RObjectFactory.O_NO_CHILDREN;
		}
		io.writeInt(options);
		//-- special attributes
		if ((options & RObjectFactory.O_CLASS_NAME) != 0) {
			io.writeString(this.className1);
		}
		io.writeVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK), l);
		
		if (this.components != null) {
			factory.writeNames(this.namesAttribute, io);
			//-- data
			for (int i = 0; i < l; i++) {
				factory.writeObject(this.components[i], io);
			}
		}
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_LIST;
	}
	
	protected String getDefaultRClassName() {
		return RObject.CLASSNAME_LIST;
	}
	
	@Override
	public final String getRClassName() {
		return this.className1;
	}
	
	
	protected int length() {
		return this.length;
	}
	
	@Override
	public long getLength() {
		return this.length;
	}
	
	@Override
	public final RCharacterStore getNames() {
		return this.namesAttribute;
	}
	
	@Override
	public final String getName(final int idx) {
		if (this.namesAttribute != null) {
			return this.namesAttribute.getChar(idx);
		}
		return null;
	}
	
	@Override
	public final String getName(final long idx) {
		if (this.namesAttribute != null) {
			return this.namesAttribute.getChar(idx);
		}
		return null;
	}
	
	@Override
	public final ICombinedRElement get(final int idx) {
		return this.components[idx];
	}
	
	@Override
	public final ICombinedRElement get(final long idx) {
		if (idx < 0 || idx >= Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException(Long.toString(idx));
		}
		return this.components[(int) idx];
	}
	
	@Override
	public final ICombinedRElement get(final String name) {
		if (this.namesAttribute != null) {
			final int idx = this.namesAttribute.indexOf(name, 0);
			if (idx >= 0) {
				return this.components[idx];
			}
		}
		return null;
	}
	
	@Override
	public final RStore getData() {
		return null;
	}
	
	
	@Override
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	@Override
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
	
	@Override
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
				if (i > 100) {
					sb.append("\n... ");
					break;
				}
				if (this.namesAttribute == null || this.namesAttribute.isNA(i)) {
					sb.append("\n[[").append((i + 1)).append("]]\n");
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
