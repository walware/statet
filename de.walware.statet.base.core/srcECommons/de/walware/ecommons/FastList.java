/*******************************************************************************
 * Copyright (c) 2004-2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - extended generalized API
 *******************************************************************************/

package de.walware.ecommons;

import java.lang.reflect.Array;


/**
 * ListenerList for common usage with support of generics.
 * 
 * This class is a thread safe list that is designed for storing lists of items.
 * The implementation is optimized for minimal memory footprint, frequent reads
 * and infrequent writes.  Modification of the list is synchronized and relatively
 * expensive, while accessing the items is very fast.  Readers are given access
 * to the underlying array data structure for reading, with the trust that they will
 * not modify the underlying array.
 * <p>
 * <a name="same">A item list handles the <i>same</i> item being added
 * multiple times, and tolerates removal of items that are the same as other
 * items in the list.  For this purpose, items can be compared with each other
 * using either equality or identity, as specified in the list constructor.
 * </p>
 * <p>
 * Use the <code>getListeners</code> method when notifying items. The recommended
 * code sequence for notifying all registered items of say,
 * <code>FooListener.eventHappened</code>, is:
 * 
 * <pre>
 * Object[] listeners = myListenerList.toArray();
 * for (int i = 0; i &lt; listeners.length; ++i) {
 *     ((FooListener) listeners[i]).eventHappened(event);
 * }
 * </pre>
 * 
 * </p><p>
 * This class can be used without OSGi running.
 * </p>
 */
public final class FastList<T> {
	
	/**
	 * Mode constant (value 0) indicating that items should be considered
	 * the <a href="#same">same</a> if they are equal.
	 */
	public static final int EQUALITY = 0;
	
	/**
	 * Mode constant (value 1) indicating that items should be considered
	 * the <a href="#same">same</a> if they are identical.
	 */
	public static final int IDENTITY = 1;
	
	/**
	 * Indicates the comparison mode used to determine if two
	 * items are equivalent
	 */
	private final boolean identity;
	
	private final Class type;
	private final T[] emptyArray;
	
	/**
	 * The list of items.  Initially empty but initialized
	 * to an array of size capacity the first time a item is added.
	 * Maintains invariant: items != null
	 */
	private volatile T[] items;
	
	
	/**
	 * Creates a item list in which items are compared using equality.
	 */
	public FastList(final Class type) {
		this(type, EQUALITY, null);
	}
	
	/**
	 * Creates a item list using the provided comparison mode.
	 * 
	 * @param mode The mode used to determine if items are the <a href="#same">same</a>.
	 */
	public FastList(final Class type, final int mode) {
		this(type, mode, null);
	}
	
	/**
	 * Creates a item list using the provided comparison mode
	 * and initial items.
	 * 
	 * @param mode The mode used to determine if items are the <a href="#same">same</a>
	 * @param initial array with the initial items, the array is used directly
	 */
	public FastList(final Class type, final int mode, final T[] initial) {
		if (mode != EQUALITY && mode != IDENTITY)
			throw new IllegalArgumentException();
		this.type = type;
		this.identity = mode == IDENTITY;
		
		emptyArray = (T[]) Array.newInstance(type, 0);
		items = (initial != null) ? initial : emptyArray;
	}
	
	
	/**
	 * Adds a item to this list. This method has no effect if the <a href="#same">same</a>
	 * item is already registered.
	 * 
	 * @param item the non-<code>null</code> item to add
	 */
	public synchronized void add(final T item) {
		// This method is synchronized to protect against multiple threads adding
		// or removing items concurrently. This does not block concurrent readers.
		if (item == null)
			throw new IllegalArgumentException();
		// check for duplicates
		final int oldSize = items.length;
		for (int i = 0; i < oldSize; ++i) {
			final Object item2 = items[i];
			if (identity ? item == item2 : item.equals(item2))
				return;
		}
		// Thread safety: create new array to avoid affecting concurrent readers
		final T[] newListeners = (T[]) Array.newInstance(type, oldSize + 1);
		System.arraycopy(items, 0, newListeners, 0, oldSize);
		newListeners[oldSize] = item;
		//atomic assignment
		this.items = newListeners;
	}
	
	/**
	 * Returns an array containing all the registered items.
	 * The resulting array is unaffected by subsequent adds or removes.
	 * If there are no items registered, the result is an empty array.
	 * Use this method when notifying items, so that any modifications
	 * to the item list during the notification will have no effect on
	 * the notification itself.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned array.
	 * 
	 * @return the list of registered items
	 */
	public T[] toArray() {
		return items;
	}
	
	/**
	 * Returns whether this item list is empty.
	 * 
	 * @return <code>true</code> if there are no registered items, and
	 *   <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return items.length == 0;
	}
	
	/**
	 * Removes a item from this list. Has no effect if the <a href="#same">same</a>
	 * item was not already registered.
	 * 
	 * @param item the non-<code>null</code> item to remove
	 */
	public synchronized void remove(final T item) {
		// This method is synchronized to protect against multiple threads adding
		// or removing items concurrently. This does not block concurrent readers.
		if (item == null)
			throw new IllegalArgumentException();
		final int oldSize = items.length;
		for (int i = 0; i < oldSize; ++i) {
			final Object item2 = items[i];
			if (identity ? item == item2 : item.equals(item2)) {
				if (oldSize == 1) {
					items = emptyArray;
				} else {
					// Thread safety: create new array to avoid affecting concurrent readers
					final T[] newListeners = (T[]) Array.newInstance(type, oldSize - 1);
					System.arraycopy(items, 0, newListeners, 0, i);
					System.arraycopy(items, i + 1, newListeners, i, oldSize - i - 1);
					//atomic assignment to field
					this.items = newListeners;
				}
				return;
			}
		}
	}
	
	/**
	 * Returns the number of registered items.
	 * 
	 * @return the number of registered items
	 */
	public int size() {
		return items.length;
	}
	
	/**
	 * Removes all items from this list.
	 * 
	 * @return the previous registered items
	 */
	public synchronized T[] clear() {
		final T[] oldListeners = items;
		items = emptyArray;
		return oldListeners;
	}
	
}
