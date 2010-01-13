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
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.nico.RWorkspace;


public final class RReferenceVar extends CombinedElement
		implements RReference {
	
	
	private int fType;
	private long fHandle;
	private String fClassName;
	private RWorkspace fResolver;
	
	
	public RReferenceVar(final String className) {
		fClassName = className;
	}
	
	public RReferenceVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		fHandle = in.readLong();
		fType = in.readInt();
		fClassName = in.readUTF();
	}
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		out.writeLong(fHandle);
		out.writeInt(fType);
		out.writeUTF(fClassName);
	}
	
	
	public byte getRObjectType() {
		return TYPE_REFERENCE;
	}
	
	public String getRClassName() {
		return fClassName;
	}
	
	public int getLength() {
		return 0;
	}
	
	public long getHandle() {
		return fHandle;
	}
	
	public void setResolver(final RWorkspace resolver) {
		fResolver = resolver;
	}
	
	public RObject getResolvedRObject() {
		if (fResolver != null) {
			return fResolver.resolve(this);
		}
		return null;
	}
	
	public RStore getData() {
		return null;
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
	
}
