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

import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;

import de.walware.statet.r.core.model.RElementName;


public class CombinedFactory extends RObjectFactoryImpl {
	
	
	public static final String FACTORY_ID = "combined";
	public static final CombinedFactory INSTANCE = new CombinedFactory();
	
	
	@Override
	public CombinedElement readObject(final ObjectInput in, final int flags) throws IOException, ClassNotFoundException {
		assert ((flags & F_ONLY_STRUCT) != 0);
		return readObject(in, flags, null, null);
	}
	
	public CombinedElement readObject(final ObjectInput in, final int flags, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		final byte type = in.readByte();
		switch (type) {
		case -1:
			return null;
		case RObject.TYPE_NULL:
			return new RNullVar(parent, name);
		case RObject.TYPE_VECTOR: {
			return new RVectorVar(in, flags, this, parent, name); }
		case RObject.TYPE_ARRAY:
			return new RArrayVar(in, flags, this, parent, name);
		case RObject.TYPE_LIST:
			return new RListVar(in, flags, this, parent, name);
		case RObject.TYPE_DATAFRAME:
			return new RDataFrameVar(in, flags, this, parent, name);
		case RObject.TYPE_ENV:
			return new REnvironmentVar(in, flags, this, parent, name);
		case RObject.TYPE_FUNCTION:
			return new RFunction2(in, flags, this, parent, name);
		case RObject.TYPE_REFERENCE:
			return new RReferenceVar(in, flags, this, parent, name);
		case RObject.TYPE_S4OBJECT:
			return new RS4ObjectVar(in, flags, this, parent, name);
		case RObject.TYPE_OTHER:
			return new ROtherVar(in, flags, this, parent, name);
		default:
			throw new IOException("object type = " + type);
		}
	}
	
	@Override
	public RList readAttributeList(final ObjectInput in, final int flags) throws IOException, ClassNotFoundException {
		return readAttributeList(in, flags, null, null);
	}
	
	public RList readAttributeList(final ObjectInput in, final int flags, final CombinedElement parent, final RElementName name) throws IOException, ClassNotFoundException {
		return new RListVar(in, flags, this, parent, name);
	}
	
//	public void setElementName(final CombinedElement element, final String name) {
//		if (element.getRObjectType() == RObject.TYPE_ENV) {
//			element.fElementName = RElementName.create(RElementName.MAIN_DEFAULT, name);
//		}
//	}
	
	public void setElementName(final CombinedElement element, final RElementName name) {
		element.fElementName = name;
	}
	
}
