/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;


public final class NameField extends Field {
	
	public static final FieldType TYPE_STORED;
	
	static {
		TYPE_STORED= new FieldType(StringField.TYPE_STORED);
		TYPE_STORED.setOmitNorms(false);
		TYPE_STORED.freeze();
	}
	
	
	/**
	 * Creates a new field with String value. 
	 * 
	 * @param name field name
	 * @param value string value
	 * @throws IllegalArgumentException if the field name or value is null.
	 */
	public NameField(final String name, final String value) {
		super(name, value, TYPE_STORED);
	}
	
}
