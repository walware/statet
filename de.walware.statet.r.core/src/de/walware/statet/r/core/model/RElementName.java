/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.Comparator;
import java.util.List;

import com.ibm.icu.text.Collator;

import de.walware.ecommons.ltk.IElementName;

import de.walware.statet.r.core.RSymbolComparator;


/**
 * 
 */
public abstract class RElementName implements IElementName {
	
	
	public static final int RESOURCE =        0x011;
	
	public static final int MAIN_OTHER =      0x020;
	public static final int MAIN_DEFAULT =    0x021;
	public static final int MAIN_PACKAGE =    0x022;
	public static final int MAIN_CLASS =      0x023;
	public static final int MAIN_SLOT =       0x024;
	public static final int MAIN_SEARCH_ENV = 0x025;
	public static final int SUB_NAMEDSLOT =   0x028;
	public static final int SUB_NAMEDPART =   0x029;
	public static final int SUB_INDEXED_S =   0x02A;
	public static final int SUB_INDEXED_D =   0x02B;
	
	
	public static String createDisplayName(IElementName a) {
		final String firstName;
		StringBuilder sb = null;
		
		switch (a.getType()) {
		case MAIN_SEARCH_ENV:
			firstName = a.getSegmentName();
			if (firstName != null) {
				sb = new StringBuilder(firstName.length());
				sb.append("as.environment(\""); //$NON-NLS-1$
				sb.append(firstName);
				sb.append("\")"); //$NON-NLS-1$
			}
			else {
				return null;
			}
			a = a.getNextSegment();
			if (a != null && a.getType() == MAIN_DEFAULT) {
				sb.append('$');
				final String name = a.getSegmentName();
				if (name != null) {
					appendSymbol(sb, name);
				}
				a = a.getNextSegment();
			}
			else {
				return sb.toString();
			}
			break;
		case MAIN_DEFAULT:
		case MAIN_CLASS:
		case MAIN_SLOT:
		case MAIN_PACKAGE:
		case SUB_NAMEDPART:
		case SUB_NAMEDSLOT:
			firstName = a.getSegmentName();
			if (firstName != null) {
				sb = appendSymbol(sb, firstName);
			}
			a = a.getNextSegment();
			if (a == null) {
				return (sb != null) ? sb.toString() : firstName;
			}
			if (sb == null) {
				sb = (firstName != null) ? new StringBuilder(firstName) : new StringBuilder();
			}
			break;
		case SUB_INDEXED_D:
			if (a instanceof DefaultImpl) {
				sb = new StringBuilder("[["); //$NON-NLS-1$
				sb.append(a.getSegmentName());
				sb.append("]]"); //$NON-NLS-1$
				a = a.getNextSegment();
				break;
			}
			return null;
		case RESOURCE:
		case MAIN_OTHER:
			return a.getSegmentName();
		default:
			return null;
		}
		
		APPEND_SUB : while (a != null) {
			String name;
			switch (a.getType()) {
			case MAIN_DEFAULT:
			case MAIN_CLASS:
			case MAIN_SLOT:
			case MAIN_PACKAGE:
				name = a.getSegmentName();
				if (name != null) {
					appendSymbol(sb, name);
				}
				a = a.getNextSegment();
				continue APPEND_SUB;
			case SUB_NAMEDPART:
				sb.append('$');
				name = a.getSegmentName();
				if (name != null) {
					appendSymbol(sb, name);
				}
				a = a.getNextSegment();
				continue APPEND_SUB;
			case SUB_NAMEDSLOT:
				sb.append('@');
				name = a.getSegmentName();
				if (name != null) {
					appendSymbol(sb, name);
				}
				a = a.getNextSegment();
				continue APPEND_SUB;
			case SUB_INDEXED_S:
				sb.append("[…]"); //$NON-NLS-1$
				break APPEND_SUB;
			case SUB_INDEXED_D:
				if (a instanceof DefaultImpl) {
					sb.append("[["); //$NON-NLS-1$
					sb.append(a.getSegmentName());
					sb.append("]]"); //$NON-NLS-1$
					a = a.getNextSegment();
					continue APPEND_SUB;
				}
				sb.append("[[…]]"); //$NON-NLS-1$
				break APPEND_SUB;
			default:
				sb.append(" …"); //$NON-NLS-1$
				break APPEND_SUB;
			}
		}
		return sb.toString();
	}
	
	private static StringBuilder appendSymbol(StringBuilder sb, final String name) {
		if (name == null) {
			return null;
		}
		final int l = name.length();
		if (l == 0) {
			return (sb != null) ? sb.append("``") : new StringBuilder("``"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		final char c0 = name.charAt(0);
		int check;
		if (Character.isLetter(c0)) {
			check = 1;
		}
		else if (c0 == '.') {
			if (l == 1) {
				check = 1;
			}
			else {
				final char c1 = name.charAt(1);
				if (c1 == '.' || c1 == '_' || Character.isLetter(c1)) {
					check = 2;
				}
				else {
					check = -1;
				}
			}
		}
		else {
			check = -1;
		}
		VALID : if (check >= 0) {
			for (; check < l; check++) {
				final char cn = name.charAt(check);
				if ((cn < 'a' || cn > 'z') && cn != '.' && cn != '_'  && !Character.isLetterOrDigit(cn)) {
					break VALID;
				}
			}
			return (sb != null) ? sb.append(name) : null;
		}
		// no valid
		if (sb == null) {
			sb = new StringBuilder(name.length()+18);
		}
		sb.append('`');
		sb.append(name);
		sb.append('`');
		return sb;
	}
	
	private static final Collator NAME_COLLATOR = RSymbolComparator.R_NAMES_COLLATOR;
	
	public static final Comparator<IElementName> NAMEONLY_COMPARATOR = new Comparator<IElementName>() {
		
		public int compare(IElementName o1, IElementName o2) {
			final String n1 = o1.getSegmentName();
			final String n2 = o2.getSegmentName();
			if (n1 != null) {
				if (n2 != null) {
					final int diff = NAME_COLLATOR.compare(n1, n2);
					if (diff != 0) {
						return diff;
					}
				}
				else {
					return Integer.MIN_VALUE;
				}
			}
			else if (n2 != null) {
				return Integer.MAX_VALUE;
			}
			
			o1 = o1.getNextSegment();
			o2 = o2.getNextSegment();
			if (o1 != null) {
				if (o2 != null) {
					final int diff = o1.getType() - o2.getType();
					if (diff != 0) {
						return diff;
					}
					return compare(o1, o2);
				}
				else {
					return Integer.MIN_VALUE+100;
				}
			}
			else if (n2 != null) {
				return Integer.MAX_VALUE-100;
			}
			return 0;
		}
	};
	
	
	private static class DefaultImpl extends RElementName {
		
		
		private final int fType;
		private final String fSegmentName;
		private IElementName fNextSegment;
		
		
		public DefaultImpl(final int type, final String segmentName) {
			fType = type;
			fSegmentName = segmentName;
			fNextSegment = null;
		}
		
		public DefaultImpl(final int type, final String segmentName, final IElementName next) {
			fType = type;
			fSegmentName = segmentName;
			fNextSegment = next;
		}
		
		
		public int getType() {
			return fType;
		}
		
		public String getSegmentName() {
			return fSegmentName;
		}
		
		public IElementName getNextSegment() {
			return fNextSegment;
		}
		
	}
	
	public static RElementName create(final int type, final String segmentName) {
		return new DefaultImpl(type, segmentName);
	}
	
	public static RElementName parseDefault(final String code) {
		final char[] chars = code.toCharArray();
		boolean quoted = false;
		int symbolStart = -1;
		int mode = MAIN_DEFAULT;
		DefaultImpl main = null;
		DefaultImpl element = null;
		
		int idx = 0;
		while (idx < code.length()) {
			final char c = code.charAt(idx++);
			switch (c) {
			case '`':
				if (symbolStart >= 0) {
					if (!quoted) {
						return null;
					}
					final DefaultImpl next = new DefaultImpl(mode, new String(chars, symbolStart, idx - symbolStart - 1));
					if (main == null) {
						main = element = next;
					}
					else {
						element.fNextSegment = next;
						element = next;
					}
					symbolStart = -1;
					mode = -1;
					continue;
				}
				else {
					if (mode < 0) {
						return null;
					}
					symbolStart = idx;
					quoted = true;
					continue;
				}
			case ' ':
			case '\t':
				if (symbolStart >= 0 && !quoted) {
					final DefaultImpl next = new DefaultImpl(mode, new String(chars, symbolStart, idx - symbolStart));
					if (main == null) {
						main = element = next;
					}
					else {
						element.fNextSegment = next;
						element = next;
					}
					symbolStart = -1;
					mode = -1;
					continue;
				}
				else {
					continue;
				}
			case ':':
				return null; // TODO
			case '$':
				if (main == null) {
					return null;
				}
				mode = SUB_NAMEDPART;
				continue;
			case '@':
				if (main == null) {
					return null;
				}
				mode = SUB_NAMEDSLOT;
				continue;
			default:
				if (symbolStart < 0) {
					if (mode < 0) {
						return null;
					}
					symbolStart = idx-1;
					quoted = false;
					continue;
				}
				else {
					continue;
				}
			}
		}
		if (mode >= 0) {
			final DefaultImpl next = new DefaultImpl(mode, symbolStart >= 0 ? new String(chars, symbolStart, idx - symbolStart) : ""); //$NON-NLS-1$
			if (main == null) {
				main = element = next;
			}
			else {
				element.fNextSegment = next;
				element = next;
			}
		}
		return main;
	}
	
	public static IElementName cloneSegment(final IElementName segment) {
		return new DefaultImpl(segment.getType(), segment.getSegmentName(), null);
	}
	
	public static IElementName concat(final IElementName[] segments) {
		IElementName next = null;
		for (int i = segments.length-1; i >= 0; i--) {
			next = new DefaultImpl(segments[i].getType(), segments[i].getSegmentName(), next);
		}
		return next;
	}
	
	public static IElementName concat(final List<IElementName> segments) {
		IElementName next = null;
		for (int i = segments.size()-1; i >= 0; i--) {
			next = new DefaultImpl(segments.get(i).getType(), segments.get(i).getSegmentName(), next);
		}
		return next;
	}
	
	
	public String getDisplayName() {
		return createDisplayName(this);
	}
	
	
	@Override
	public final int hashCode() {
		final String name = getSegmentName();
		final IElementName next = getNextSegment();
		return getType() * ((name != null) ? name.hashCode() : 1) * ((next != null) ? next.hashCode() : 1);
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (!(obj instanceof RElementName)) {
			return false;
		}
		final IElementName other = (RElementName) obj;
		final String thisName = getSegmentName();
		final String otherName = other.getSegmentName();
		return ((getType() == other.getType())
				&& ((thisName != null) ? 
						(thisName == otherName || (otherName != null && thisName.hashCode() == otherName.hashCode() && thisName.equals(otherName)) ) : 
						(other.getSegmentName() == null) )
				&& ((getNextSegment() != null) ? 
						(getNextSegment().equals(other.getNextSegment())) :
						(other.getNextSegment() == null) ) );
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
	
}
