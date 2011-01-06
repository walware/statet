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
import java.util.Set;

import de.walware.ecommons.ConstList;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace;


public final class REnvironmentVar extends CombinedElement
		implements REnvironment, RWorkspace.ICombinedEnvironment, ExternalizableRObject, IRFrame {
	
	
	private String fCombinedName;
	protected String fEnvironmentName;
	private int fSpecialType;
	private long handle;
	
	private CombinedElement[] components;
	private int length;
	
	private String className1;
	private RCharacterDataImpl namesAttribute;
	
	private int fFrameType;
	
	
	public REnvironmentVar(final String id, final boolean isSearch) {
		setEnvName(id, isSearch);
	}
	
	public REnvironmentVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		fParent = parent;
		fElementName = name;
		readExternal(io, factory);
		if (fElementName == null) {
			fElementName = RElementName.create(RElementName.MAIN_OTHER, fEnvironmentName);
		}
	}
	
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		final int options = io.in.readInt();
		//-- special attributes
		this.className1 = ((options & RObjectFactory.O_CLASS_NAME) != 0) ?
				io.readString() : RObject.CLASSNAME_ENV;
		//-- data
		this.handle = io.in.readLong();
		setEnvName(io.readString(), false);
		final int length = this.length = io.in.readInt();
		
		if ((options & RObjectFactory.O_NO_CHILDREN) != 0) {
			this.namesAttribute = null;
			this.components = null;
		}
		else {
			this.namesAttribute = new RCharacterDataImpl(io);
			this.components = new CombinedElement[length];
			for (int i = 0; i < length; i++) {
				components[i] = CombinedFactory.INSTANCE.readObject(io, this,
						RElementName.create(RElementName.MAIN_DEFAULT, namesAttribute.getChar(i)) );
			}
		}
	}
	
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		//-- options
		int options = 0;
		final boolean customClass = !this.className1.equals(RObject.CLASSNAME_ENV);
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
		
		io.out.writeLong(this.handle);
		io.writeString(fCombinedName);
		io.out.writeInt(this.length);
		
		if (this.components != null) {
			this.namesAttribute.writeExternal(io);
			//-- data
			for (int i = 0; i < this.length; i++) {
				factory.writeObject(this.components[i], io);
			}
		}
	}
	
	
	protected void setEnvName(final String id, final boolean isSearch) {
		if (id != null) {
			if (id.equals("base") || id.equals("package:base")) {
				fEnvironmentName = ENVNAME_BASE;
				fSpecialType = ENVTYPE_BASE;
				fFrameType = IRFrame.PACKAGE;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_PACKAGE, "base");
				}
				return;
			}
			else if (id.startsWith("package:")) {
				fEnvironmentName = id;
				fSpecialType = ENVTYPE_PACKAGE;
				fFrameType = IRFrame.PACKAGE;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_PACKAGE, id.substring(8));
				}
				return;
			}
			else if (id.equals(".GlobalEnv") || id.equals("R_GlobalEnv")){
				fEnvironmentName = ENVNAME_GLOBAL;
				fSpecialType = ENVTYPE_GLOBAL;
				fFrameType = IRFrame.PROJECT;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, ".GlobalEnv");
				}
				return;
			}
			else if (id.equals("Autoloads")){
				fEnvironmentName = ENVNAME_AUTOLOADS;
				fSpecialType = ENVTYPE_AUTOLOADS;
				fFrameType = IRFrame.EXPLICIT;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, ENVNAME_AUTOLOADS);
				}
				return;
			}
		}
		fEnvironmentName = id;
		fSpecialType = 0;
		fFrameType = IRFrame.EXPLICIT;
		if (fElementName == null) {
			fElementName = RElementName.create(isSearch ? RElementName.MAIN_SEARCH_ENV : RElementName.MAIN_OTHER, id);
		}
	}
	
	
	public final byte getRObjectType() {
		return TYPE_ENV;
	}
	
	public String getRClassName() {
		return this.className1;
	}
	
	
	public int getSpecialType() {
		return fSpecialType;
	}
	
	public String getEnvironmentName() {
		return fEnvironmentName;
	}
	
	public long getHandle() {
		return this.handle;
	}
	
	
	public int getLength() {
		return this.length;
	}
	
	public RCharacterStore getNames() {
		return this.namesAttribute;
	}
	
	public String getName(final int idx) {
		return this.namesAttribute.getChar(idx);
	}
	
	public RObject get(final int idx) {
		return this.components[idx];
	}
	
	public RObject get(final String name) {
		final int idx = this.namesAttribute.indexOf(name);
		if (idx >= 0) {
			return this.components[idx];
		}
		return null;
	}
	
	public RObject[] toArray() {
		final RObject[] array = new RObject[this.length];
		System.arraycopy(this.components, 0, array, 0, this.length);
		return array;
	}
	
	public RStore getData() {
		return null;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	
	public boolean hasModelChildren(final Filter filter) {
		if (this.components == null) {
			return false;
		}
		if (filter == null) {
			return (this.components.length > 0);
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
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
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
	
	
	public int getFrameType() {
		return fFrameType;
	}
	
	public Set<String> getElementNames() {
		return null;
	}
	
	public boolean containsElement(final String name) {
		return this.namesAttribute.contains(name);
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IRFrame.class.equals(required)) {
			return this;
		}
		return super.getAdapter(required);
	}
	
	public void setError(final String message) {
		fElementName = RElementName.create(RElementName.MAIN_OTHER, fEnvironmentName);
		this.components = new CombinedElement[0];
		this.namesAttribute = new RCharacterDataImpl();
		fCombinedName = fCombinedName + " ("+message+")";
	}
	
	
	public String getFrameId() {
		return null;
	}
	
	public List<? extends IRElement> getModelElements() {
		return null;
	}
	
	public List<? extends IRFrame> getPotentialParents() {
		return null;
	}
	
	
	@Override
	protected int singleHash() {
		return (fSpecialType > 0) ? fEnvironmentName.hashCode() : (int) this.handle;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof REnvironment) {
			final REnvironment other = (REnvironment) obj;
			return other.getSpecialType() == getSpecialType()
					&& getEnvironmentName().equals(other.getEnvironmentName());
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("RObject type=environment, class=").append(getRClassName());
		sb.append("\n\tlength=").append(this.length);
		if (this.components != null) {
			sb.append("\n\tdata: ");
			for (int i = 0; i < this.length; i++) {
				sb.append("\n$").append(this.namesAttribute.getChar(i)).append("\n");
				sb.append(this.components[i]);
			}
		}
		else {
			sb.append("\n<NODATA/>");
		}
		return sb.toString();
	}
	
}
