/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons;

import java.lang.reflect.Array;


/**
 * 
 * List is not thread safe
 */
public final class FastArrayCacheList<T> {
	
	
	private final Class fType;
	private int fInitialCapacity;
	
	private T[] fArray;
	private int fSize;
	
	
	public FastArrayCacheList(final Class type, final int initialCapacity) {
		fType = type;
		fSize = 0;
		fInitialCapacity = initialCapacity;
		
		fArray = (T[]) Array.newInstance(type, initialCapacity);
	}
	
	
	public void add(final T value) {
		if (value == null)
			throw new IllegalArgumentException();
		final int oldCapacity = fArray.length;
		if (fSize < oldCapacity) {
			fArray[fSize++] = value;
			return;
		}
		final T[] newArray = (T[]) Array.newInstance(fType, oldCapacity + fInitialCapacity);
		System.arraycopy(fArray, 0, newArray, 0, oldCapacity);
		newArray[fSize++] = value;
		this.fArray = newArray;
	}
	
	public boolean isEmpty() {
		return (fSize == 0);
	}
	
	/**
	 * @return the number of elements
	 */
	public int size() {
		return fSize;
	}
	
	/**
	 * Removes all elements from this list.
	 */
	public void clear() {
		if (fSize > 0) {
			removeAll();
		}
	}
	
	/**
	 * Removes all elements from this list.
	 * 
	 * @return an array with the previous registered elements. 
	 *     The length of the array can be greater than size of this list.
	 */
	public T[] removeAll() {
		final T[] oldListeners = fArray;
		fSize = 0;
		fArray =  (T[]) Array.newInstance(fType, fInitialCapacity);
		return oldListeners;
	}
	
}
