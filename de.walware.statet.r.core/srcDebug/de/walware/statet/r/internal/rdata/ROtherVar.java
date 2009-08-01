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
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RList;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class ROtherVar extends CombinedElement {
	
	
	private String fClassName;
	
	protected RList fAttributes;
	
	
	public ROtherVar(final String className) {
		if (className == null) {
			throw new NullPointerException();
		}
		fClassName = className;
	}
	
	public ROtherVar(final ObjectInput in, final int flags, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		fParent = parent;
		fElementName = name;
		readExternal(in, flags, factory);
	}
	
	public void readExternal(final ObjectInput in, final int flags, final RObjectFactory factory) throws IOException, ClassNotFoundException {
		final int options = in.readInt();
		fClassName = in.readUTF();
		if ((options & RObjectFactoryImpl.F_WITH_ATTR) != 0) {
			fAttributes = factory.readAttributeList(in, flags);
		}
	}
	
	public void writeExternal(final ObjectOutput out, final int flags, final RObjectFactory factory) throws IOException {
		final boolean withAttr = ((flags & RObjectFactoryImpl.F_WITH_ATTR) != 0) && (fAttributes != null);
		out.writeInt((withAttr) ? RObjectFactoryImpl.F_WITH_ATTR : 0);
		out.writeUTF(fClassName);
		if (withAttr) {
			factory.writeAttributeList(fAttributes, out, flags);
		}
	}
	
	
	public byte getRObjectType() {
		return TYPE_OTHER;
	}
	
	public String getRClassName() {
		return fClassName;
	}
	
	public int getLength() {
		return 0;
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
