/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
	
	
	public RLanguageVar(final RJIO io, final RObjectFactory factory,
			final CombinedElement parent, final RElementName name) throws IOException {
		super(parent, name);
		
		//-- options
		final int options = io.readInt();
		//-- special attributes
		this.type = io.readByte();
		this.className1 = ((options & RObjectFactory.O_CLASS_NAME) != 0) ?
				io.readString() :
				RLanguageImpl.getBaseClassname(this.type);
		//-- data
	}
	
	@Override
	public void writeExternal(final RJIO io, final RObjectFactory factory) throws IOException {
		int options = 0;
		if (!this.className1.equals(RLanguageImpl.getBaseClassname(this.type))) {
			options |= RObjectFactory.O_CLASS_NAME;
		}
		io.writeInt(options);
		io.writeByte(this.type);
		//-- special attributes
		if ((options & RObjectFactory.O_CLASS_NAME) != 0) {
			io.writeString(this.className1);
		}
		//-- data
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_LANGUAGE;
	}
	
	@Override
	public byte getLanguageType() {
		return this.type;
	}
	
	@Override
	public String getRClassName() {
		return this.className1;
	}
	
	
	@Override
	public long getLength() {
		return 0;
	}
	
	@Override
	public String getSource() {
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
		return false;
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.emptyList();
	}
	
}
