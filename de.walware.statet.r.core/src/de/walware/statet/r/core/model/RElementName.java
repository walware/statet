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

package de.walware.statet.r.core.model;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.ibm.icu.text.Collator;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RLexer;


/**
 * Base class for R element names
 * 
 * Defines type constants and provides static utility methods.
 */
public abstract class RElementName implements IElementName {
	
	
	public static final int RESOURCE =        0x00f;
	
	public static final int MAIN_OTHER =      0x010;
	public static final int MAIN_DEFAULT =    0x011;
	public static final int MAIN_CLASS =      0x013;
	public static final int MAIN_SEARCH_ENV = 0x015;
	public static final int MAIN_PACKAGE =    0x016;
	public static final int MAIN_PROJECT =    0x017;
	public static final int SUB_NAMEDSLOT =   0x018;
	public static final int SUB_NAMEDPART =   0x019;
	public static final int SUB_INDEXED_S =   0x01A;
	public static final int SUB_INDEXED_D =   0x01B;
	
	public static final int DISPLAY_NS_PREFIX = 0x1;
	public static final int DISPLAY_EXACT = 0x2;
	
	
	/**
	 * Element names providing the exact index as number (for SUB_INDEXED_D).
	 */
	public static interface IndexElementName extends IElementName {
		
		int getIndex();
		
	}
	
	public static String createDisplayName(RElementName a, final int options) {
		StringBuilder sb = null;
		
		final IElementName namespace = ((options & DISPLAY_NS_PREFIX) != 0) ? a.getNamespace() : null;
		if (((options & DISPLAY_NS_PREFIX) != 0) && namespace != null) {
			final String namespaceName;
			switch (namespace.getType()) {
			case MAIN_SEARCH_ENV:
				namespaceName = namespace.getSegmentName();
				break;
			case MAIN_PACKAGE:
				namespaceName = "package:"+namespace.getSegmentName();
				break;
			case MAIN_PROJECT:
				namespaceName = ".GlobalEnv";
				break;
			default:
				return null;
			}
			if (namespaceName != null) {
				sb = new StringBuilder(namespaceName.length() + 32);
				sb.append("as.environment(\""); //$NON-NLS-1$
				sb.append(namespaceName);
				sb.append("\")"); //$NON-NLS-1$
			}
			else {
				return null;
			}
			if (a != null && a.getType() == MAIN_DEFAULT) {
				sb.append('$');
				final String name = a.getSegmentName();
				if (name != null) {
					appendSymbol(sb, name);
				}
				a = a.getNextSegment();
			}
			else {
				return null;
			}
		}
		else if (((options & DISPLAY_NS_PREFIX) != 0)
				&& (a.getType() == MAIN_SEARCH_ENV || a.getType() == MAIN_PACKAGE)) {
			final String namespaceName;
			switch (a.getType()) {
			case MAIN_SEARCH_ENV:
				namespaceName = a.getSegmentName();
				break;
			case MAIN_PACKAGE:
				namespaceName = "package:"+a.getSegmentName();
				break;
			case MAIN_PROJECT:
				namespaceName = ".GlobalEnv";
				break;
			default:
				return null;
			}
			if (namespaceName != null) {
				sb = new StringBuilder(namespaceName.length() + 32);
				sb.append("as.environment(\""); //$NON-NLS-1$
				sb.append(namespaceName);
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
			else if (a == null) {
				return sb.toString();
			}
			else {
				return null;
			}
		}
		else {
			String firstName;
			final int type = a.getType();
			switch (type) {
			case MAIN_DEFAULT:
			case MAIN_CLASS:
			case SUB_NAMEDPART:
			case SUB_NAMEDSLOT:
				firstName = a.getSegmentName();
				if (firstName != null) {
					sb = appendSymbol(sb, firstName);
				}
				else {
					firstName = ""; //$NON-NLS-1$
				}
				a = a.getNextSegment();
				if (a == null) {
					return (sb != null) ? sb.toString() : firstName;
				}
				if (sb == null) {
					sb = new StringBuilder(firstName);
				}
				break;
			case MAIN_PACKAGE:
				firstName = a.getSegmentName();
				if (firstName != null) {
					return "package:"+firstName;
				}
				else {
					return "package:<unknown>";
				}
			case MAIN_PROJECT:
				firstName = a.getSegmentName();
				if (firstName != null) {
					return "project:"+firstName;
				}
				else {
					return "project:<unknown>";
				}
			case MAIN_SEARCH_ENV:
				firstName = a.getSegmentName();
				if (firstName != null) {
					return firstName;
				}
				return null;
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
		}
		
		APPEND_SUB : while (a != null) {
			String name;
			switch (a.getType()) {
			case MAIN_DEFAULT:
			case MAIN_CLASS:
			case SUB_NAMEDPART:
				if (((options & DISPLAY_EXACT) != 0) && a instanceof IndexElementName) {
					sb.append("[[");
					sb.append(((IndexElementName) a).getIndex());
					sb.append("]]");
				}
				else {
					sb.append('$');
					name = a.getSegmentName();
					if (name != null) {
						appendSymbol(sb, name);
					}
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
	
	
	private static class DefaultImpl extends RElementName implements Serializable {
		
		
		private static final long serialVersionUID = 315497720879434929L;
		
		
		private final int fType;
		private final String fSegmentName;
		private RElementName fNamespace;
		private RElementName fNextSegment;
		
		
		public DefaultImpl(final int type, final String segmentName) {
			fType = type;
			fSegmentName = segmentName;
			fNextSegment = null;
		}
		
		public DefaultImpl(final int type, final RElementName namespace, final String segmentName, final RElementName next) {
			fType = type;
			fSegmentName = segmentName;
			fNamespace = namespace;
			fNextSegment = next;
		}
		
		public DefaultImpl(final int type, final String segmentName, final RElementName next) {
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
		
		@Override
		public RElementName getNamespace() {
			return fNamespace;
		}
		
		@Override
		public RElementName getNextSegment() {
			return fNextSegment;
		}
		
	}
	
	private static class DualImpl extends DefaultImpl implements IndexElementName {
		
		private static final long serialVersionUID = 7040207683623992047L;
		
		private final int fIdx;
		
		public DualImpl(final int type, final String segmentName, final int idx) {
			super(type, segmentName);
			fIdx = idx;
		}
		
		public DualImpl(final int type, final String segmentName, final int idx,
				final RElementName next) {
			super(type, segmentName, next);
			fIdx = idx;
		}
		
		
		@Override
		protected DefaultImpl cloneSegment0(final RElementName next) {
			return new DualImpl(getType(), getSegmentName(), fIdx, next);
		}
		
		
		public int getIndex() {
			return fIdx;
		}
		
	}
	
	/**
	 * Lexer for RScanner.
	 */
	private static class ParseLexer extends RLexer {
		
		
		public ParseLexer(final SourceParseInput input) {
			super(input);
		}
		
		
		@Override
		protected void createSymbolToken() {
			fFoundType = RTerminal.SYMBOL;
			fFoundText = fInput.substring(1, fFoundNum);
			fFoundStatus = STATUS_OK;
		}
		
		@Override
		protected void createQuotedSymbolToken(final RTerminal type, final int status) {
			fFoundType = type;
			fFoundText = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
					fInput.substring(2, fFoundNum-2) : fInput.substring(2, fFoundNum-1);
			fFoundStatus = status;
		}
		
		@Override
		protected void createStringToken(final RTerminal type, final int status) {
			fFoundType = type;
			fFoundText = ((status & STATUS_MASK_12) != STATUS2_SYNTAX_TOKEN_NOT_CLOSED) ?
					fInput.substring(2, fFoundNum-2) : fInput.substring(2, fFoundNum-1);
			fFoundStatus = status;
		}
		
		@Override
		protected void createNumberToken(final RTerminal type, final int status) {
			fFoundType = type;
			fFoundText = fInput.substring(1, fFoundNum);
			fFoundStatus = status;
		}
		
		@Override
		protected void createWhitespaceToken() {
			fFoundType = null;
		}
		
		@Override
		protected void createCommentToken(final RTerminal type) {
			fFoundType = null;
		}
		
		@Override
		protected void createLinebreakToken(final String text) {
			fFoundType = null;
		}
		
		@Override
		protected void createUnknownToken(final String text) {
			fFoundType = RTerminal.UNKNOWN;
			fFoundText = text;
			fFoundStatus = STATUS_OK;
		}
		
	}
	
	public static RElementName create(final int type, final String segmentName) {
		return new DefaultImpl(type, segmentName);
	}
	
	public static RElementName create(final int type, final String segmentName, final int idx) {
		if (!(type == SUB_NAMEDPART || type == SUB_INDEXED_D)) {
			throw new IllegalArgumentException();
		}
		return new DualImpl(type, segmentName, idx);
	}
	
	public static RElementName parseDefault(final String code) {
		final ParseLexer lexer = new ParseLexer(new StringParseInput(code));
		lexer.setFull();
		
		int mode = MAIN_DEFAULT;
		DefaultImpl main = null;
		DefaultImpl last = null;
		while (true) {
			DefaultImpl tmp = null;
			RTerminal type = lexer.next();
			if (type == null || type == RTerminal.EOF) {
				if (mode >= 0) {
					tmp = new DefaultImpl(mode, ""); //$NON-NLS-1$
					if (main == null) {
						main = last = tmp;
					}
					else {
						last.fNextSegment = tmp;
						last = tmp;
					}
				}
				return main;
			}
			else {
				switch(type) {
				case IF:
				case ELSE:
				case FOR:
				case IN:
				case WHILE:
				case REPEAT:
				case NEXT:
				case BREAK:
				case FUNCTION:
				case TRUE:
				case FALSE:
				case NA:
				case NA_INT:
				case NA_REAL:
				case NA_CPLX:
				case NA_CHAR:
				case NULL:
				case NAN:
				case INF:
					if (mode != MAIN_DEFAULT && mode != MAIN_PACKAGE
							&& mode != SUB_NAMEDPART && mode != SUB_NAMEDSLOT) {
						return null;
					}
					tmp = new DefaultImpl(mode, type.text);
					if (main == null) {
						main = last = tmp;
					}
					else {
						last.fNextSegment = tmp;
						last = tmp;
					}
					type = lexer.next();
					if (type == null || type == RTerminal.EOF) {
						return main; // valid prefix
					}
					else {
						return null; // invalid
					}
				case SYMBOL:
				case SYMBOL_G:
					if (mode != MAIN_DEFAULT && mode != MAIN_PACKAGE
							&& mode != SUB_NAMEDPART && mode != SUB_NAMEDSLOT) {
						return null;
					}
					tmp = new DefaultImpl(mode, lexer.getText());
					if (main == null) {
						main = last = tmp;
					}
					else {
						last.fNextSegment = tmp;
						last = tmp;
					}
					mode = -1;
					continue;
				case NUM_INT:
				case NUM_NUM:
					if (mode != SUB_INDEXED_S && mode != SUB_INDEXED_D) {
						return null;
					}
					tmp = new DefaultImpl(mode, lexer.getText());
					type = lexer.next();
					if (type != RTerminal.SUB_INDEXED_CLOSE) {
						return null;
					}
					if (main == null) {
						main = last = tmp;
					}
					else {
						last.fNextSegment = tmp;
						last = tmp;
					}
					mode = -2;
					continue;
				case SUB_NAMED_PART:
					if (main == null || mode >= 0) {
						return null;
					}
					mode = SUB_NAMEDPART;
					continue;
				case SUB_NAMED_SLOT:
					if (main == null || mode >= 0) {
						return null;
					}
					mode = SUB_NAMEDSLOT;
					continue;
				case SUB_INDEXED_S_OPEN:
					if (main == null || mode >= 0) {
						return null;
					}
					mode = SUB_INDEXED_S;
					continue;
				case SUB_INDEXED_D_OPEN:
					if (main == null || mode >= 0) {
						return null;
					}
					mode = SUB_INDEXED_S;
					continue;
				case SUB_INDEXED_CLOSE:
					if (mode != -2) {
						return null;
					}
					continue;
				case NS_GET:
				case NS_GET_INT:
					if (main != null || mode >= 0) {
						return null;
					}
					mode = MAIN_PACKAGE;
					continue;
				default:
					return null;
				}
			}
		}
	}
	
	public static RElementName cloneName(RElementName name, final boolean withNamespace) {
		if (name == null) {
			return null;
		}
		RElementName namespace = (withNamespace) ? name.getNamespace() : null;
		if (namespace != null) {
			namespace = new DefaultImpl(namespace.getType(), namespace.getSegmentName(), null);
		}
		final DefaultImpl main = new DefaultImpl(name.getType(), namespace, name.getSegmentName(), null);
		DefaultImpl last = main;
		name = name.getNextSegment();
		while (name != null) {
			final DefaultImpl copy = name.cloneSegment0(null);
			last.fNextSegment = copy;
			last = copy;
			name = name.getNextSegment();
		}
		return main;
	}
	
	public static RElementName cloneSegment(final RElementName name) {
		return name.cloneSegment0(null);
	}
	
	public static RElementName concat(final List<RElementName> segments) {
		if (segments.size() > 0) {
			int first = 0;
			RElementName namespace = segments.get(first);
			if (namespace.getType() == MAIN_SEARCH_ENV || namespace.getType() == MAIN_PACKAGE) {
				first++;
			}
			else {
				namespace = null;
			}
			if (segments.size() > first) {
				RElementName next = null;
				for (int i = segments.size()-1; i > first; i--) {
					next = segments.get(i).cloneSegment0(next);
				}
				next = new DefaultImpl(segments.get(first).getType(), namespace, segments.get(first).getSegmentName(), next);
				return next;
			}
		}
		return null;
	}
	
	
	public abstract RElementName getNamespace();
	public abstract RElementName getNextSegment();
	
	public String getDisplayName() {
		return createDisplayName(this, 0);
	}
	
	protected RElementName.DefaultImpl cloneSegment0(final RElementName next) {
		return new DefaultImpl(getType(), getSegmentName(), next);
	}
	
	
	@Override
	public final int hashCode() {
		final String name = getSegmentName();
		final IElementName next = getNextSegment();
		if (next != null) {
			return getType() * ((name != null) ? name.hashCode() : 1) * (next.hashCode()+7);
		}
		else {
			return getType() * ((name != null) ? name.hashCode() : 1);
		}
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
