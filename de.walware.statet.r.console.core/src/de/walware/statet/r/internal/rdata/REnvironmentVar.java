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
import java.util.Set;

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RJIO;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class REnvironmentVar extends CombinedElement
		implements REnvironment, RWorkspace.ICombinedREnvironment, ExternalizableRObject, IRFrame {
	
	
	private String className1;
	
	private String environmentName;
	private String fCombinedName;
	private int fSpecialType;
	private long handle;
	
	private int length;
	private CombinedElement[] components;
	
	private RCharacterDataImpl namesAttribute;
	
	private int fFrameType;
	private RProcess source;
	private int stamp;
	
	
	public REnvironmentVar(final String id, final boolean isSearch,
			final CombinedElement parent, final RElementName name) {
		super(parent, name);
		setEnvName(id, isSearch);
	}
	
	public REnvironmentVar(final RJIO io, final CombinedFactory factory,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		//-- options
		final int options = io.readInt();
		//-- special attributes
		this.className1 = ((options & RObjectFactory.O_CLASS_NAME) != 0) ?
				io.readString() : RObject.CLASSNAME_ENV;
		//-- data
		this.handle = io.readLong();
		setEnvName(io.readString(), false);
		final int l = this.length = (int) io.readVULong((byte) (options & RObjectFactory.O_LENGTHGRADE_MASK));
		
		if ((options & RObjectFactory.O_NO_CHILDREN) != 0) {
			this.namesAttribute = null;
			this.components = null;
		}
		else {
			this.namesAttribute = new RCharacterDataImpl(io, l);
			this.components = new CombinedElement[l];
			for (int i = 0; i < l; i++) {
				this.components[i] = factory.readObject(io, this,
						RElementName.create(RElementName.MAIN_DEFAULT, namesAttribute.getChar(i)) );
			}
		}
		
		if (getElementName() == null) {
			setElementName(RElementName.create(RElementName.MAIN_OTHER, this.environmentName));
		}
	}
	
	@Override
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
		io.writeInt(options);
		//-- special attributes
		if (customClass) {
			io.writeString(this.className1);
		}
		
		io.writeLong(this.handle);
		io.writeString(fCombinedName);
		io.writeInt(this.length);
		
		if (this.components != null) {
			this.namesAttribute.writeExternal(io);
			//-- data
			for (int i = 0; i < this.length; i++) {
				factory.writeObject(this.components[i], io);
			}
		}
	}
	
	public void setSource(final RProcess source, final int stamp) {
		this.source = source;
		this.stamp = stamp;
	}
	
	@Override
	public RProcess getSource() {
		return this.source;
	}
	
	public int getStamp() {
		return this.stamp;
	}
	
	
	protected void setEnvName(final String id, final boolean isSearch) {
		if (id != null) {
			if (id.equals("base") || id.equals("package:base")) { //$NON-NLS-1$ //$NON-NLS-2$
				this.environmentName = ENVNAME_BASE;
				fSpecialType = ENVTYPE_BASE;
				fFrameType = IRFrame.PACKAGE;
				if (getElementName() == null) {
					setElementName(RElementName.create(RElementName.MAIN_PACKAGE, "base")); //$NON-NLS-1$
				}
				return;
			}
			else if (id.startsWith("package:")) { //$NON-NLS-1$
				this.environmentName = id;
				fSpecialType = ENVTYPE_PACKAGE;
				fFrameType = IRFrame.PACKAGE;
				if (getElementName() == null) {
					setElementName(RElementName.create(RElementName.MAIN_PACKAGE, id.substring(8)));
				}
				return;
			}
			else if (id.equals(".GlobalEnv") || id.equals("R_GlobalEnv")){ //$NON-NLS-1$ //$NON-NLS-2$
				this.environmentName = ENVNAME_GLOBAL;
				fSpecialType = ENVTYPE_GLOBAL;
				fFrameType = IRFrame.PROJECT;
				if (getElementName() == null) {
					setElementName(RElementName.create(RElementName.MAIN_SEARCH_ENV, ".GlobalEnv")); //$NON-NLS-1$
				}
				return;
			}
			else if (id.equals("Autoloads")){ //$NON-NLS-1$
				this.environmentName = ENVNAME_AUTOLOADS;
				fSpecialType = ENVTYPE_AUTOLOADS;
				fFrameType = IRFrame.EXPLICIT;
				if (getElementName() == null) {
					setElementName(RElementName.create(RElementName.MAIN_SEARCH_ENV, ENVNAME_AUTOLOADS));
				}
				return;
			}
			this.environmentName = id;
		}
		else {
			this.environmentName = ""; //$NON-NLS-1$
		}
		fSpecialType = 0;
		fFrameType = IRFrame.EXPLICIT;
		if (getElementName() == null) {
			setElementName(RElementName.create(isSearch ? RElementName.MAIN_SEARCH_ENV : RElementName.MAIN_OTHER, id));
		}
	}
	
	
	@Override
	public final byte getRObjectType() {
		return TYPE_ENV;
	}
	
	@Override
	public String getRClassName() {
		return this.className1;
	}
	
	
	@Override
	public int getSpecialType() {
		return fSpecialType;
	}
	
	@Override
	public String getEnvironmentName() {
		return this.environmentName;
	}
	
	@Override
	public long getHandle() {
		return this.handle;
	}
	
	
	@Override
	public long getLength() {
		return this.length;
	}
	
	@Override
	public RCharacterStore getNames() {
		return this.namesAttribute;
	}
	
	@Override
	public String getName(final int idx) {
		return this.namesAttribute.getChar(idx);
	}
	
	@Override
	public String getName(final long idx) {
		return this.namesAttribute.getChar(idx);
	}
	
	@Override
	public ICombinedRElement get(final int idx) {
		return this.components[idx];
	}
	
	@Override
	public ICombinedRElement get(final long idx) {
		if (idx < 0 || idx >= Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException(Long.toString(idx));
		}
		return this.components[(int) idx];
	}
	
	@Override
	public ICombinedRElement get(final String name) {
		final int idx = this.namesAttribute.indexOf(name, 0);
		if (idx >= 0) {
			return this.components[idx];
		}
		return null;
	}
	
	@Override
	public RStore getData() {
		return null;
	}
	
	
	@Override
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	
	@Override
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
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		if (this.components == null) {
			return Collections.emptyList();
		}
		if (filter == null) {
			return new ConstArrayList<IRLangElement>(this.components);
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
		setElementName(RElementName.create(RElementName.MAIN_OTHER, this.environmentName));
		this.components = new CombinedElement[0];
		this.namesAttribute = new RCharacterDataImpl();
		fCombinedName = fCombinedName + " ("+message+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	@Override
	public String getFrameId() {
		return null;
	}
	
	@Override
	public List<? extends IRElement> getModelElements() {
		return null;
	}
	
	@Override
	public List<? extends IRFrame> getPotentialParents() {
		return null;
	}
	
	
	@Override
	protected int singleHash() {
		return (fSpecialType > 0) ? this.environmentName.hashCode() : (int) this.handle;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof REnvironment)) {
			return false;
		}
		final REnvironment other = (REnvironment) obj;
		return (fSpecialType == other.getSpecialType()
					&& this.environmentName.equals(other.getEnvironmentName()) );
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
