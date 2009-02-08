/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.text;


/**
 * A cache of fixed size, interning string
 */
public class FixInterningStringCache implements IStringCache {
	
	
	private static final int HASHSET_MAX = 0x1ff; // bits true from right
	
	private static final int CHARTABLE_SIZE = 127;
	private static final String[] CHARTABLE;
	static {
		CHARTABLE = new String[CHARTABLE_SIZE];
		for (int i = 0; i < CHARTABLE_SIZE; i++) {
			CHARTABLE[i] = String.valueOf((char) i).intern();
		}
	}
	
	
	private final String[] fValues;
	
	
	public FixInterningStringCache() {
		fValues = new String[HASHSET_MAX+1];
	}
	
	
	public String get(final String s) {
		switch (s.length()) {
		case 0:
			return ""; //$NON-NLS-1$
		case 1:
			final int c = s.charAt(0);
			if (c >= 0 && c < CHARTABLE_SIZE) {
//				fStatCharmap++;
				return CHARTABLE[c];
			}
			else {
				return getChar(s);
			}
		default:
			return getDefault(s);
		}
	}
	
	private String getChar(final String s) {
		final int hashCode = s.hashCode();
		final int i1 = ((hashCode-CHARTABLE_SIZE) & HASHSET_MAX);
		final String s1 = fValues[i1];
		if (s1 != null && s1.hashCode() == hashCode && s1.equals(s)) {
//			fStatFound++;
			return s1;
		}
//		fStatSet++;
		return (fValues[i1] = s.intern());
	}
	
	private String getDefault(final String s) {
		final int hashCode = s.hashCode();
		final int i1 = (hashCode & HASHSET_MAX);
		final String s1 = fValues[i1];
		if (s1 != null && s1.hashCode() == hashCode && s1.equals(s)) {
//			fStatFound++;
			return s1;
		}
//		fStatSet++;
		return (fValues[i1] = s.intern());
	}
	
	
//	private long fStatCharmap = 0;
//	private long fStatFound = 0;
//	private long fStatSet = 0;
//	
//	@Override
//	public String toString() {
//		final double charmap = fStatCharmap;
//		final double set = fStatSet;
//		final double found = fStatFound;
//		final double sum = charmap+set+found;
//		return "StringCache stat: sum="+ sum +" char=" + charmap/sum + " found=" + found/sum + " set=" + set/sum; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//	}
	
}
