/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.pkgmanager.IRPkgList;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;


public final class RPkgList<T extends IRPkg> extends ArrayList<T> implements IRPkgList<T> {
	
	
	private static final long serialVersionUID = -3022375551268568786L;
	
	
	public RPkgList(final int capacity) {
		super(capacity);
	}
	
	
	@Override
	public int indexOf(final String name) {
		if (name == null) {
			return -1;
		}
		int low = 0;
		int high = super.size() - 1;
		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final int diff = RPkgUtil.COLLATOR.compare(super.get(mid).getName(), name);
			if (diff < 0) {
				low = mid + 1;
			}
			else if (diff > 0) {
				high = mid - 1;
			}
			else {
				return mid;
			}
		}
		return -(low + 1);
	}
	
	public int indexOf(final String name, int low) {
		int high = super.size() - 1;
		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final int diff = RPkgUtil.COLLATOR.compare(super.get(mid).getName(), name);
			if (diff < 0) {
				low = mid + 1;
			}
			else if (diff > 0) {
				high = mid - 1;
			}
			else {
				return mid;
			}
		}
		return -(low + 1);
	}
	
	@Override
	public int indexOf(final Object o) {
		if (o instanceof IRPkg) {
			return indexOf(((IRPkg) o).getName());
		}
		return -1;
	}
	
	@Override
	public boolean contains(final String name) {
		return (indexOf(name) >= 0);
	}
	
	@Override
	public T get(final String name) {
		final int idx = indexOf(name);
		return (idx >= 0) ? super.get(idx) : null;
	}
	
	@Override
	public boolean add(final T pkg) {
		final int idx = indexOf(pkg.getName());
		if (idx < 0) {
			super.add(-idx - 1, pkg);
			return true;
		}
		return false;
	}
	
	@Override
	public void add(final int index, final T element) {
		if (element == null) {
			throw new NullPointerException("element"); //$NON-NLS-1$
		}
		super.add(index, element);
	}
	
	public void set(final T pkg) {
		final int idx = indexOf(pkg.getName());
		if (idx < 0) {
			super.add(-idx - 1, pkg);
		}
		else {
			set(idx, pkg);
		}
	}
	
	public void remove(final String name) {
		final int idx = indexOf(name);
		if (idx >= 0) {
			remove(idx);
		}
	}
	
}
