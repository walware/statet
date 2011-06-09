/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RJIO;
import de.walware.rj.data.RLanguage;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.ExternalizableRObject;
import de.walware.rj.data.defaultImpl.RLanguageImpl;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public class RLanguageVar extends CombinedElement
		implements RLanguage, ExternalizableRObject {
	
	
	private byte type;
	
	private String className1;
	
	private String source;
	
	
	public RLanguageVar(final RJIO io, final RObjectFactory factory, final CombinedElement parent, final RElementName name) throws IOException {
		fParent = parent;
		fElementName = name;
		readExternal(io, factory);
	}
	
	public void readExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		final int options = io.readInt();
		this.type = io.readByte();
		//-- special attributes
		this.className1 = ((options & RObjectFactory.O_CLASS_NAME) != 0) ? io.readString() :
				RLanguageImpl.getBaseClassname(this.type);
		//-- data
		if ((io.flags & RObjectFactory.F_ONLY_STRUCT) == 0) {
			this.source = io.readString();
		}
	}
	
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		int options = 0;
		final boolean customClass = !this.className1.equals(RLanguageImpl.getBaseClassname(this.type));
		if (customClass) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		io.writeInt(options);
		io.writeByte(this.type);
		//-- special attributes
		if (customClass) {
			io.writeString(this.className1);
		}
		//-- data
		if ((io.flags & RObjectFactory.F_ONLY_STRUCT) == 0) {
			io.writeString(this.source);
		}
	}
	
	
	public byte getRObjectType() {
		return TYPE_LANGUAGE;
	}
	
	public byte getLanguageType() {
		return this.type;
	}
	
	public String getRClassName() {
		return this.className1;
	}
	
	public int getLength() {
		return 0;
	}
	
	public String getSource() {
		return this.source;
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
