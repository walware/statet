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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.FieldInfo.IndexOptions;


/**
 * @see IntField
 */
final class FlagField extends Field {
	
	public static final FieldType TYPE_STORED;
	
	static {
		TYPE_STORED= new FieldType();
		TYPE_STORED.setIndexed(true);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setOmitNorms(true);
		TYPE_STORED.setIndexOptions(IndexOptions.DOCS_ONLY);
		TYPE_STORED.setNumericType(FieldType.NumericType.INT);
		TYPE_STORED.setNumericPrecisionStep(Integer.MAX_VALUE);
		TYPE_STORED.setStored(true);
		TYPE_STORED.freeze();
	}
	
	
	/** Creates a new field.
	 * 
	 * @param name field name
	 * @param value field value
	 * @throws IllegalArgumentException if the field name is null.
	 */
	public FlagField(final String name, final int value) {
		super(name, TYPE_STORED);
		this.fieldsData= Integer.valueOf(value);
	}
	
}
