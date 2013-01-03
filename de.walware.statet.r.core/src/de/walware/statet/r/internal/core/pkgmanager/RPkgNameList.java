/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;

import de.walware.statet.r.core.pkgmanager.RPkgUtil;


public final class RPkgNameList extends ArrayList<String> {
	
	
	private static final long serialVersionUID = -3379607002623432425L;
	
	
	public RPkgNameList(final int capacity) {
		super(capacity);
	}
	
	
	public int indexOf(final String name) {
		if (name == null) {
			return -1;
		}
		int low = 0;
		int high = super.size() - 1;
		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final int diff = RPkgUtil.COLLATOR.compare(super.get(mid), name);
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
		return (o instanceof String) ? indexOf((String) o) : -1;
	}
	
	@Override
	public boolean add(final String name) {
		final int idx = indexOf(name);
		if (idx < 0) {
			super.add(-idx - 1, name);
			return true;
		}
		return false;
	}
	
}
