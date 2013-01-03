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

package de.walware.statet.r.core.renv;

import java.util.Arrays;
import java.util.Iterator;


public final class RNumVersion {
	
	// First int in numeric version is the operator
	
	
	public static final RNumVersion NONE = new RNumVersion(""); //$NON-NLS-1$
	
	
	public static RNumVersion create(final String s) {
		if (s == null || s.isEmpty()) {
			return NONE;
		}
		return new RNumVersion(s);
	}
	
	
	private static final int OP_UNSUPPORTED = -2;
	private static final int OP_NONE = -1;
	
	private static final int OP_GE = 2;
	
	static {
		NONE.fNumeric = new int[] { OP_NONE };
	}
	
	
	private static final int[] parseVersion(final String s) {
		final int[] v = new int[(3 + s.length())/2];
		int idx = 1;
		int i = 0;
		if (s.startsWith(">=")) { //$NON-NLS-1$
			v[0] = OP_GE;
			i = 2;
		}
		int start = -3;
		for (; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (start == -3 && c == ' ') {
				continue;
			}
			if (c >= '0' && c <= '9') {
				if (start < 0) {
					start = i;
				}
				continue;
			}
			if (start >= 0) {
				v[idx++] = Integer.parseInt(s.substring(start, i));
				start = -1;
				if (c == '.' || c == '-') {
					continue;
				}
			}
			break;
		}
		if (start >= 0) {
			v[idx++] = Integer.parseInt(s.substring(start, s.length()));
		}
		if (idx <= 2) {
			v[0] = OP_UNSUPPORTED;
			idx = 1;
		}
		return (v.length == idx) ? v : Arrays.copyOf(v, idx);
	}
	
	
	private final String fString;
	
	/** use {@link #getNumericVersion()} */
	private volatile int[] fNumeric;
	
	
	private RNumVersion(final String s) {
		fString = s;
	}
	
	
	public boolean isGreaterEqualThan(final RNumVersion pkgVersion2) {
		return isGreaterEqualThan(getNumericVersion(), pkgVersion2.getNumericVersion());
	}
	
	private boolean isGreaterEqualThan(final int[] v1, final int[] v2) {
		final int l = Math.max(v1.length, v2.length);
		for (int i = 1; i < l; i++) {
			final int diff = ((i < v1.length) ? v1[i] : 0) - ((i < v2.length) ? v2[i] : 0);
			if (diff > 0) {
				return true;
			}
			else if (diff < 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isGreaterThan(final RNumVersion pkgVersion2) {
		return isGreaterThan(getNumericVersion(), pkgVersion2.getNumericVersion());
	}
	
	private boolean isGreaterThan(final int[] v1, final int[] v2) {
		final int l = Math.max(v1.length, v2.length);
		for (int i = 1; i < l; i++) {
			final int diff = ((i < v1.length) ? v1[i] : 0) - ((i < v2.length) ? v2[i] : 0);
			if (diff > 0) {
				return true;
			}
			else if (diff < 0) {
				return false;
			}
		}
		return false;
	}
	
	public boolean isSmallerThan(final RNumVersion pkgVersion2) {
		return isSmallerThan(getNumericVersion(), pkgVersion2.getNumericVersion());
	}
	
	private boolean isSmallerThan(final int[] v1, final int[] v2) {
		final int l = Math.max(v1.length, v2.length);
		for (int i = 1; i < l; i++) {
			final int diff = ((i < v1.length) ? v1[i] : 0) - ((i < v2.length) ? v2[i] : 0);
			if (diff < 0) {
				return true;
			}
			else if (diff > 0) {
				return false;
			}
		}
		return false;
	}
	
	public boolean isSatisfiedBy(final RNumVersion pkgVersion2) {
		final int[] v1 = getNumericVersion();
		switch (v1[0]) {
		case OP_GE:
			return isGreaterEqualThan(pkgVersion2.getNumericVersion(), v1);
		default:
			return true;
		}
	}
	
	public boolean isSatisfiedByAny(final Iterator<RNumVersion> pkgVersion2) {
		final int[] v1 = getNumericVersion();
		switch (v1[0]) {
		case OP_GE:
			while (pkgVersion2.hasNext()) {
				if (isGreaterEqualThan(pkgVersion2.next().getNumericVersion(), v1)) {
					return true;
				}
			}
			return false;
		default:
			return pkgVersion2.hasNext();
		}
	}
	
	private int[] getNumericVersion() {
		if (fNumeric == null) {
			fNumeric = parseVersion(fString);
		}
		return fNumeric;
	}
	
	
	@Override
	public int hashCode() {
		return fString.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (this == obj || (obj instanceof RNumVersion
				&& fString.equals(((RNumVersion) obj).fString) ));
	}
	
	@Override
	public String toString() {
		return fString;
	}
	
	
}
