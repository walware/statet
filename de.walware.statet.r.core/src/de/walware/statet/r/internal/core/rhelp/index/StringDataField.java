/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
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


abstract class StringDataField extends Field {
	
	
	StringDataField(final String name, final FieldType type) {
		super(name, type);
	}
	
	
	@Override
	public final void setStringValue(final String value) {
		if (value == null) {
			throw new IllegalArgumentException("value cannot be null");
		}
		this.fieldsData= value;
	}
	
	
}
