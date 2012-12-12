/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;


class Store<S> {
	
	
	public static class LoadDataException extends Exception {
		
		private static final long serialVersionUID = -2599418116314887064L;
		
		private final boolean fIsUnrecoverable;
		
		public LoadDataException(final boolean isUnrecoverable) {
			fIsUnrecoverable = isUnrecoverable;
		}
		
		public boolean isUnrecoverable() {
			return fIsUnrecoverable;
		}
		
	}
	
	public static class Fragment<T> {
		
		public final T rObject;
		
		public final int beginRowIdx;
		public final int endRowIdx;
		public final int beginColumnIdx;
		public final int endColumnIdx;
		
		public Fragment(final T rObject, final int beginRowIdx, final int endRowIdx,
				final int beginColumnIdx, final int endColumnIdx) {
			this.rObject = rObject;
			this.beginRowIdx = beginRowIdx;
			this.endRowIdx = endRowIdx;
			this.beginColumnIdx = beginColumnIdx;
			this.endColumnIdx = endColumnIdx;
		}
		
	}
	
	static class Item<T> extends Fragment<T> {
		
		/** sequential number of fragments, first by fragment column, then by fragment row */
		final int num;
		
		Store.Fragment<T> fragment;
		
		int lastAccess;
		boolean scheduled;
		
		Item(final int num, final int beginRowIdx, final int endRowIdx,
				final int beginColumnIdx, final int endColumnIdx, final int lastAccess) {
			super(null, beginRowIdx, endRowIdx, beginColumnIdx, endColumnIdx);
			
			this.num = num;
			this.lastAccess = lastAccess;
		}
		
	}
	
	static abstract class Lock {
		
		static final int ERROR_STATE = 4;
		static final int RELOAD_STATE = 3;
		static final int PAUSE_STATE = 2;
		static final int LOCAL_PAUSE_STATE = 1;
		
		int state;
		
		abstract void schedule(Object obj);
		
	}
	
	
	private final Item<S> DUMMY_ITEM = new Item<S>(-1, 0, 0, 0, 0, 0);
	
	private final Lock fFragmentsLock;
	
	private final int fColumnCount;
	private int fRowCount;
	
	private final Item<S>[] fFragments;
	private Item<S> fLastFragment;
	private int fCurrentFragmentIdx = -1;
	
	private final int fFragmentSize;
	private final int fFragmentRowCount;
	private final int fFragmentColumnCount;
	private final int fFragmentCountInRow;
	
	private final int fMaxFragments;
	
	private int fAccessCounter = Integer.MIN_VALUE;
	
	
	public Store(final Lock lock, final int columnCount, final int rowCount, final int max) {
		fFragmentsLock = lock;
		
		fColumnCount = columnCount;
		fRowCount = rowCount;
		
		fMaxFragments = max;
		
//			fFragmentSize = 100;
//			fFragmentColumnCount = Math.min(columnCount, 5);
		fFragmentSize = 2500;
		fFragmentColumnCount = Math.min(columnCount, 25);
		fFragmentCountInRow = (columnCount - 1) / fFragmentColumnCount + 1;
		fFragmentRowCount = fFragmentSize / fFragmentColumnCount;
		
		fFragments = new Item[fMaxFragments+1];
		fLastFragment = DUMMY_ITEM;
	}
	
	
	public Store.Fragment<S> getFor(final int row, final int column) throws LoadDataException {
		final int num = (column / fFragmentColumnCount) +
				(row / fFragmentRowCount) * fFragmentCountInRow;
		final Item<S> item;
		synchronized (fFragmentsLock) {
			if (fFragmentsLock.state > 0) {
				switch (fFragmentsLock.state) {
				case Lock.LOCAL_PAUSE_STATE:
				case Lock.PAUSE_STATE:
					return null;
				case Lock.RELOAD_STATE:
					throw new LoadDataException(false);
				default:
					throw new LoadDataException(true);
				}
			}
			if (fRowCount <= 0) {
				throw new LoadDataException(false);
			}
			if (row >= fRowCount) {
				return null;
			}
			if (fLastFragment.num == num) {
				item = fLastFragment;
			}
			else {
				item = getItem(num);
				fLastFragment = item;
			}
			
			if (item.fragment != null) {
				return item.fragment;
			}
			
			if (item.scheduled) {
				return null;
			}
			item.scheduled = true;
			fFragmentsLock.schedule(item);
			return item.fragment;
		}
	}
	
	private Item<S> getItem(final int num) {
		int low = 0;
		int high = fCurrentFragmentIdx;
		
		while (low <= high) {
			final int mid = (low + high) >> 1;
			final Item<S> item = fFragments[mid];
			
			if (item.num < num) {
				low = mid + 1;
			}
			else if (item.num > num) {
				high = mid - 1;
			}
			else {
				item.lastAccess = ++fAccessCounter;
				return item;
			}
		}
		final Item<S> item = createItem(num);
		System.arraycopy(fFragments, low, fFragments, low+1, ++fCurrentFragmentIdx-low);
		fFragments[low] =  item;
		
		if (fCurrentFragmentIdx >= fMaxFragments) {
			removeOldest();
		}
		return item;
	}
	
	private Item<S> createItem(final int num) {
		final int beginRowIdx = (num / fFragmentCountInRow) * fFragmentRowCount;
		final int beginColumnIdx = (num % fFragmentCountInRow) * fFragmentColumnCount; 
		final int endRowIdx = Math.min(beginRowIdx + fFragmentRowCount, fRowCount);
		final int endColumnIdx = Math.min(beginColumnIdx + fFragmentColumnCount, fColumnCount);
		return new Item<S>(num, beginRowIdx, endRowIdx, beginColumnIdx, endColumnIdx,
				++fAccessCounter);
	}
	
	private void removeOldest() {
		int oldestIdx = -1;
		int oldestAccess = fAccessCounter;
		boolean overflow = false;
		for (int i = 0; i <= fCurrentFragmentIdx; i++) {
			if (overflow) {
				if (fFragments[i].lastAccess > fAccessCounter
						&& fFragments[i].lastAccess < oldestAccess) {
					oldestIdx = i;
					oldestAccess = fFragments[i].lastAccess;
				}
			}
			else if (fFragments[i].lastAccess > fAccessCounter) {
				overflow = true;
				oldestIdx = i;
				oldestAccess = fFragments[i].lastAccess;
			}
			else if (fFragments[i].lastAccess < oldestAccess) {
				oldestIdx = i;
				oldestAccess = fFragments[i].lastAccess;
			}
		}
		fFragments[oldestIdx].scheduled = false;
		System.arraycopy(fFragments, oldestIdx+1, fFragments, oldestIdx, fCurrentFragmentIdx-oldestIdx);
		fFragments[fCurrentFragmentIdx--] = null;
		return;
	}
	
	public void internalClear(final int rowCount) {
		for (int i = 0; i <= fCurrentFragmentIdx; i++) {
			fFragments[i].scheduled = false;
			fFragments[i] = null;
		}
		fCurrentFragmentIdx = -1;
		fLastFragment = DUMMY_ITEM;
		
		if (rowCount >= 0) {
			fRowCount = rowCount;
		}
	}
	
	public Store.Fragment<S>[] internalAvailable() {
		int count = 0;
		final Store.Fragment<S>[] available = new Item[fCurrentFragmentIdx+1];
		for (int i = 0; i <= fCurrentFragmentIdx; i++) {
			if (fFragments[i].fragment != null) {
				available[count++] = fFragments[i].fragment;
			}
		}
		return available;
	}
	
	public Item<S>[] internalForUpdate() {
		final Item<S>[] toUpdate = new Item[fCurrentFragmentIdx+1];
		int count1 = 0;
		int count2 = 0;
		ITER_FRAGMENTS: for (int i = 0; i <= fCurrentFragmentIdx; i++) {
			final Item<S> item = fFragments[i];
			if (item.scheduled) {
				if (item.lastAccess <= fAccessCounter) {
					for (int j = 0; j < count1; j++) {
						if (item.lastAccess < toUpdate[j].lastAccess) {
							System.arraycopy(toUpdate, j, toUpdate, j+1, (count1++)+count2-j);
							toUpdate[j] = item;
							continue ITER_FRAGMENTS;
						}
					}
					if (count2 > 0) {
						System.arraycopy(toUpdate, count1, toUpdate, count1+1, count2);
					}
					toUpdate[count1++] = item;
					continue ITER_FRAGMENTS;
				}
				else {
					for (int j = count1; j < count1+count2; j++) {
						if (item.lastAccess < toUpdate[j].lastAccess) {
							System.arraycopy(toUpdate, j, toUpdate, j+1, count1+(count2++)-j);
							toUpdate[j] = item;
							continue ITER_FRAGMENTS;
						}
					}
					toUpdate[count2++] = item;
					continue ITER_FRAGMENTS;
				}
			}
		}
		return toUpdate;
	}
	
}
