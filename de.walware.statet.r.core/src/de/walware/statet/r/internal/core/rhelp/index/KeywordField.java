/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp.index;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;


/** 
 * A field that is indexed but not tokenized: the entire String value is indexed as a single token.
 * For example this might be used for a 'country' field or an 'id' field, or any field that you
 * intend to use for sorting or access through the field cache.
 * 
 * @see StringField
 **/
final class KeywordField extends StringDataField {
	
	public static final FieldType TYPE_NOT_STORED;
	
	static {
		TYPE_NOT_STORED= new FieldType(StringField.TYPE_STORED);
		TYPE_NOT_STORED.setStored(false);
		TYPE_NOT_STORED.freeze();
	}
	
	
	/**
	 * Creates a new field.
	 * 
	 * @param name field name
	 * @throws IllegalArgumentException if the field name.
	 */
	public KeywordField(final String name) {
		super(name, TYPE_NOT_STORED);
	}
	
}
