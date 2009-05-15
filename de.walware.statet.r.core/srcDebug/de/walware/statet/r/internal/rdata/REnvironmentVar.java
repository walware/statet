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
import java.util.List;
import java.util.Set;

import de.walware.ecommons.ltk.IElementName;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;
import de.walware.rj.data.defaultImpl.RUniqueCharacterDataWithHashImpl;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace.ICombinedEnvironment;


public final class REnvironmentVar extends CombinedElement
		implements ICombinedEnvironment, ExternalizableRObject {
	
	
	private String fCombinedName;
	protected String fEnvironmentName;
	private int fSpecialType;
	private long fHandle;
	protected CombinedElement[] fComponents;
	protected RCharacterDataImpl fNamesAttribute;
	private int fFrameType;
	
	private String fClassName;
	
	
	public REnvironmentVar(final String id, final boolean isSearch) {
		setEnvName(id, isSearch);
	}
	
	public REnvironmentVar(final String id, final int handle, final CombinedElement[] initialComponents, String[] initialNames,
			final CombinedElement parent, final RElementName name) {
		fParent = parent;
		fElementName = name;
		setEnvName(id, false);
		fHandle = handle;
		fComponents = initialComponents;
		if (initialNames == null) {
			initialNames = new String[fComponents.length];
		}
		fNamesAttribute = new RUniqueCharacterDataWithHashImpl(initialNames);
		fClassName = RObject.CLASSNAME_ENV;
	}
	
	public REnvironmentVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final IElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
		if (fElementName == null) {
			fElementName = RElementName.create(RElementName.MAIN_OTHER, fEnvironmentName);
		}
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		final int options = in.readInt();
		
		fClassName = ((options & RObjectFactoryImpl.O_CLASS_NAME) != 0) ?
				in.readUTF() : RObject.CLASSNAME_ENV;
				
		fHandle = in.readLong();
		setEnvName(in.readUTF(), false);
		final int length = in.readInt();
		fNamesAttribute = new RUniqueCharacterDataWithHashImpl(in);
		fComponents = new CombinedElement[length];
		for (int i = 0; i < length; i++) {
			fComponents[i] = CombinedFactory.INSTANCE.readObject(in, flags, this,
					RElementName.create(RElementName.MAIN_DEFAULT, fNamesAttribute.getChar(i)) );
		}
	}
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		int options = 0;
		final boolean customClass = !fClassName.equals(RObject.CLASSNAME_ENV);
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		out.writeInt(options);
		
		if (customClass) {
			out.writeUTF(fClassName);
		}
		
		out.writeLong(fHandle);
		out.writeUTF(fCombinedName);
		out.writeInt(fComponents.length);
		fNamesAttribute.writeExternal(out);
		for (int i = 0; i < fComponents.length; i++) {
			factory.writeObject(fComponents[i], out, flags);
		}
	}
	
	
	protected void setEnvName(final String id, final boolean isSearch) {
		if (id != null) {
			if (id.equals("base") || id.equals("package:base")) {
				fCombinedName = "base";
				fEnvironmentName = ENVNAME_BASE;
				fSpecialType = ENVTYPE_BASE;
				fFrameType = IRFrame.PACKAGE;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, "package:base");
				}
				return;
			}
			else if (id.startsWith("package:")) {
				fCombinedName = id.substring(8);
				fEnvironmentName = id;
				fSpecialType = ENVTYPE_PACKAGE;
				fFrameType = IRFrame.PACKAGE;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, id);
				}
				return;
			}
			else if (id.equals(".GlobalEnv") || id.equals("R_GlobalEnv")){
				fCombinedName = ".GlobalEnv";
				fEnvironmentName = ENVNAME_GLOBAL;
				fSpecialType = ENVTYPE_GLOBAL;
				fFrameType = IRFrame.PROJECT;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, ".GlobalEnv");
				}
				return;
			}
			else if (id.equals("Autoloads")){
				fCombinedName = ENVNAME_AUTOLOADS;
				fEnvironmentName = ENVNAME_AUTOLOADS;
				fSpecialType = ENVTYPE_AUTOLOADS;
				fFrameType = IRFrame.EXPLICIT;
				if (fElementName == null) {
					fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, ENVNAME_AUTOLOADS);
				}
				return;
			}
		}
		fCombinedName = id;
		fEnvironmentName = id;
		fSpecialType = 0;
		fFrameType = IRFrame.EXPLICIT;
		if (fElementName == null) {
			fElementName = RElementName.create(isSearch ? RElementName.MAIN_SEARCH_ENV : RElementName.MAIN_OTHER, id);
		}
	}
	
	public final int getRObjectType() {
		return TYPE_ENV;
	}
	
	public String getRClassName() {
		return fClassName;
	}
	
	public long getHandle() {
		return fHandle;
	}
	
	public String getEnvironmentName() {
		return fEnvironmentName;
	}
	
	public String getName() {
		return fCombinedName;
	}
	
	public int getSpecialType() {
		return fSpecialType;
	}
	
	public int getLength() {
		return fComponents.length;
	}
	
	public RCharacterStore getNames() {
		return fNamesAttribute;
	}
	
	public String getName(final int idx) {
		return fNamesAttribute.getChar(idx);
	}
	
	public RObject get(final int idx) {
		return fComponents[idx];
	}
	
	public boolean set(final int idx, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public void insert(final int idx, final String label, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public void add(final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public void remove(final int idx) {
		throw new UnsupportedOperationException();
	}
	
	
	public RObject get(final String name) {
		final int idx = fNamesAttribute.getIdx(name);
		if (idx >= 0) {
			return fComponents[idx];
		}
		return null;
	}
	
	public boolean set(final String name, final RObject component) {
		throw new UnsupportedOperationException();
	}
	
	public RStore getData() {
		return null;
	}
	
	public RObject[] toArray() {
		final RObject[] array = new RObject[fComponents.length];
		System.arraycopy(fComponents, 0, array, 0, fComponents.length);
		return array;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	
	public boolean hasModelChildren(final Filter filter) {
		if (filter == null) {
			return (fComponents.length > 0);
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
	
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
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
	
	
	public int getFrameType() {
		return fFrameType;
	}
	
	public Set<String> getElementNames() {
		return null;
	}
	
	public boolean containsElement(final String name) {
		return fNamesAttribute.contains(name);
	}
	
	@Override
	protected int singleHash() {
		return (fSpecialType > 0) ? fEnvironmentName.hashCode() : (int) fHandle;
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
		sb.append("\n\tlength=").append(fComponents.length);
		sb.append("\n\tdata: ");
		for (int i = 0; i < fComponents.length; i++) {
			sb.append("\n$").append(fNamesAttribute.getChar(i)).append("\n");
			sb.append(fComponents[i]);
		}
		return sb.toString();
	}
	
	public void setError(final String message) {
		fElementName = RElementName.create(RElementName.MAIN_OTHER, fEnvironmentName);
		fComponents = new CombinedElement[0];
		fNamesAttribute = new RCharacterDataImpl();
		fCombinedName = fCombinedName + " ("+message+")";
	}
	
}
