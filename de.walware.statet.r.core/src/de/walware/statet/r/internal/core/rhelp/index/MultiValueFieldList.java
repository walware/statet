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

import java.util.ArrayList;

import org.apache.lucene.document.Field;


/**
 * List creating automatically new elements in {@link #get(int)}.
 */
@SuppressWarnings("serial")
abstract class MultiValueFieldList<E extends Field> extends ArrayList<E> {
	
	
	public static MultiValueFieldList<NameField> forNameField(final String name) {
		return new MultiValueFieldList<NameField>() {
			@Override
			protected NameField createNewField() {
				return new NameField(name);
			}
		};
	}
	
	public static MultiValueFieldList<KeywordField> forKeywordField(final String name) {
		return new MultiValueFieldList<KeywordField>() {
			@Override
			protected KeywordField createNewField() {
				return new KeywordField(name);
			}
		};
	}
	
	public static MultiValueFieldList<TxtField> forTxtField(final String name) {
		return new MultiValueFieldList<TxtField>() {
			@Override
			protected TxtField createNewField() {
				return new TxtField(name);
			}
		};
	}
	
	public static MultiValueFieldList<TxtField> forTxtField(final String name, final float boost) {
		return new MultiValueFieldList<TxtField>() {
			@Override
			protected TxtField createNewField() {
				return new TxtField(name, boost);
			}
		};
	}
	
	
	public MultiValueFieldList() {
	}
	
	
	protected abstract E createNewField();
	
	
	@Override
	public E get(final int index) {
		while (index >= size()) {
			add(createNewField());
		}
		return super.get(index);
	}
	
}
