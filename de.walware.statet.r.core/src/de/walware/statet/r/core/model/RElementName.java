/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.ibm.icu.text.Collator;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RLexer;


/**
 * Base class for R element names
 * 
 * Defines type constants and provides static utility methods.
 */
public abstract class RElementName implements IElementName {
	
	
	public static final int RESOURCE=                       0x0_0f;
	
	public static final int MAIN_OTHER=                     0x0_10;
	public static final int MAIN_DEFAULT=                   0x0_11;
	public static final int MAIN_CLASS=                     0x0_15;
	
	public static final int SUB_NAMEDSLOT=                  0x0_19;
	public static final int SUB_NAMEDPART=                  0x0_1a;
	public static final int SUB_INDEXED_S=                  0x0_1b;
	public static final int SUB_INDEXED_D=                  0x0_1c;
	
	public static final int SCOPE_NS=                       0x0_21;
	public static final int SCOPE_NS_INT=                   0x0_22;
	
	public static final int SCOPE_SEARCH_ENV=               0x0_25;
	public static final int SCOPE_PACKAGE=                  0x0_26;
	public static final int SCOPE_SYSFRAME=                 0x0_27;
	public static final int SCOPE_PROJECT=                  0x0_29;
	
	public static final int ANONYMOUS=                      0x0_30;
	
	public static final int DISPLAY_FQN=                    1 << 0;
	public static final int DISPLAY_EXACT=                  1 << 1;
	
	
	public static boolean isMainType(final int type) {
		return (type >= MAIN_OTHER && type < SUB_NAMEDSLOT);
	}
	
	public static boolean isRegularMainType(final int type) {
		return (type > MAIN_OTHER && type < SUB_NAMEDSLOT);
	}
	
	public static boolean isScopeType(final int type) {
		return ((type & 0x0_F0) == 0x0_20);
	}
	
	public static boolean isNamespaceScopeType(final int type) {
		return (type >= SCOPE_NS && type < SCOPE_SEARCH_ENV);
	}
	
	public static boolean isSearchScopeType(final int type) {
		return (type >= SCOPE_SEARCH_ENV && type < ANONYMOUS);
	}
	
	public static boolean isPackageFacetScopeType(final int type) {
		switch (type) {
		case SCOPE_NS:
		case SCOPE_NS_INT:
		case SCOPE_PACKAGE:
			return true;
		default:
			return false;
		}
	}
	
	
	/**
	 * Element names providing the exact index as number (for SUB_INDEXED_D).
	 */
	public static interface IndexElementName extends IElementName {
		
		int getIndex();
		
	}
	
	public static String createDisplayName(RElementName elementName, final int options) {
		if (elementName == null) {
			throw new NullPointerException("elementName"); //$NON-NLS-1$
		}
		StringBuilder sb= null;
		
		if ((options & DISPLAY_FQN) != 0) {
			RElementName scopeName= elementName.getScope();
			if (scopeName == null && isScopeType(elementName.getType())) {
				scopeName= elementName;
				elementName= elementName.getNextSegment();
			}
			if (scopeName != null) {
				if (elementName != null && elementName.getType() != MAIN_DEFAULT) {
					return null;
				}
				sb= new StringBuilder(32);
				if (!printScopeFQ(scopeName, sb, options, elementName != null)) {
					return null;
				}
				if (elementName != null) {
					final String name= elementName.getSegmentName();
					if (name != null) {
						appendSymbol(sb, name);
					}
					elementName= elementName.getNextSegment();
				}
			}
		}
		if (sb == null) {
			String firstName;
			final int type= elementName.getType();
			switch (type) {
			case MAIN_DEFAULT:
			case MAIN_CLASS:
			case SUB_NAMEDPART:
			case SUB_NAMEDSLOT:
				firstName= elementName.getSegmentName();
				if (firstName != null) {
					sb= appendSymbol(sb, firstName);
				}
				else {
					firstName= ""; //$NON-NLS-1$
				}
				elementName= elementName.getNextSegment();
				if (elementName == null) {
					return (sb != null) ? sb.toString() : firstName;
				}
				if (sb == null) {
					sb= new StringBuilder(firstName);
				}
				break;
			case SCOPE_NS:
				if (elementName.getNextSegment() == null) {
					return printScopeUI("namespace:", elementName.getSegmentName(), options); //$NON-NLS-1$
				}
				else {
					printScopeFQ(elementName, sb, options, true);
					break;
				}
			case SCOPE_NS_INT:
				if (elementName.getNextSegment() == null) {
					return printScopeUI("namespace-env:", elementName.getSegmentName(), options); //$NON-NLS-1$
				}
				else {
					printScopeFQ(elementName, sb, options, true);
					break;
				}
			case SCOPE_SEARCH_ENV:
				if (elementName.getNextSegment() == null) {
					firstName= elementName.getSegmentName();
					if (firstName != null) {
						return firstName;
					}
				}
				return null;
			case SCOPE_SYSFRAME:
				if (elementName.getNextSegment() == null) {
					return printScopeUI("frame:", elementName.getSegmentName(), options); //$NON-NLS-1$
				}
				else {
					return null;
				}
			case SCOPE_PACKAGE:
				if (elementName.getNextSegment() == null) {
					return printScopeUI("package:", elementName.getSegmentName(), options); //$NON-NLS-1$
				}
				else {
					return null;
				}
			case SCOPE_PROJECT:
				if (elementName.getNextSegment() == null) {
					return printScopeUI("project:", elementName.getSegmentName(), options); //$NON-NLS-1$
				}
				else {
					return null;
				}
			case SUB_INDEXED_D:
				if (elementName instanceof DefaultImpl) {
					sb= new StringBuilder("[["); //$NON-NLS-1$
					sb.append(elementName.getSegmentName());
					sb.append("]]"); //$NON-NLS-1$
					elementName= elementName.getNextSegment();
					break;
				}
				return null;
			case RESOURCE:
			case MAIN_OTHER:
				return elementName.getSegmentName();
			case ANONYMOUS:
				if ((options & DISPLAY_EXACT) == 0) {
					return "<anonymous>"; //$NON-NLS-1$
				}
				else {
					return null;
				}
			default:
				return null;
			}
		}
		
		APPEND_SUB : while (elementName != null) {
			String name;
			switch (elementName.getType()) {
			case MAIN_DEFAULT:
			case MAIN_CLASS:
			case SUB_NAMEDPART:
				if (((options & DISPLAY_EXACT) != 0) && elementName instanceof IndexElementName) {
					sb.append("[["); //$NON-NLS-1$
					sb.append(((IndexElementName) elementName).getIndex());
					sb.append("L]]"); //$NON-NLS-1$
				}
				else {
					sb.append('$');
					name= elementName.getSegmentName();
					if (name != null) {
						appendSymbol(sb, name);
					}
				}
				elementName= elementName.getNextSegment();
				continue APPEND_SUB;
			case SUB_NAMEDSLOT:
				sb.append('@');
				name= elementName.getSegmentName();
				if (name != null) {
					appendSymbol(sb, name);
				}
				elementName= elementName.getNextSegment();
				continue APPEND_SUB;
			case SUB_INDEXED_S:
				if (((options & DISPLAY_EXACT) != 0)) {
					return null;
				}
				sb.append("[…]"); //$NON-NLS-1$
				break APPEND_SUB;
			case SUB_INDEXED_D:
				if (elementName instanceof DefaultImpl) {
					sb.append("[["); //$NON-NLS-1$
					sb.append(elementName.getSegmentName());
					sb.append("]]"); //$NON-NLS-1$
					elementName= elementName.getNextSegment();
					continue APPEND_SUB;
				}
				else if ((options & DISPLAY_EXACT) == 0) {
					sb.append("[[…]]"); //$NON-NLS-1$
					elementName= elementName.getNextSegment();
					continue APPEND_SUB;
				}
				else {
					return null;
				}
			default:
				if (((options & DISPLAY_EXACT) == 0)) {
					sb.append(" …"); //$NON-NLS-1$
					break APPEND_SUB;
				}
				return null;
			}
		}
		return sb.toString();
	}
	
	private static boolean isValidSymbol(final String name) {
		final int l= name.length();
		if (l == 0) {
			return false;
		}
		final char c0= name.charAt(0);
		int check;
		if (Character.isLetter(c0)) {
			check= 1;
		}
		else if (c0 == '.') {
			if (l == 1) {
				check= 1;
			}
			else {
				final char c1= name.charAt(1);
				if (c1 == '.' || c1 == '_' || Character.isLetter(c1)) {
					check= 2;
				}
				else {
					return false;
				}
			}
		}
		else {
			return false;
		}
		for (; check < l; check++) {
			final char cn= name.charAt(check);
			if ((cn < 'a' || cn > 'z') && cn != '.' && cn != '_'  && !Character.isLetterOrDigit(cn)) {
				return false;
			}
		}
		return true;
	}
	
	private static StringBuilder appendSymbol(StringBuilder sb, final String name) {
		if (isValidSymbol(name)) {
			return (sb != null) ? sb.append(name) : null;
		}
		if (sb == null) {
			sb= new StringBuilder(name.length() + 18);
		}
		else {
			sb.ensureCapacity(name.length() + 2);
		}
		if (name.isEmpty()) {
			sb.append("``"); //$NON-NLS-1$
		}
		else {
			sb.append('`');
			sb.append(name);
			sb.append('`');
		}
		return sb;
	}
	
	private static String printScopeUI(final String itemPrefix, String segmentName,
			final int options) {
		if (segmentName == null) {
			if ((options & DISPLAY_EXACT) == 0) {
				segmentName= "<unknown>"; //$NON-NLS-1$
			}
			else {
				return null;
			}
		}
		final StringBuilder sb= new StringBuilder(itemPrefix.length() + segmentName.length());
		sb.append(itemPrefix);
		sb.append(segmentName);
		return sb.toString();
	}
	
	private static boolean printScopeFQ(final RElementName a, StringBuilder sb,
			final int options, final boolean operator) {
		final String scopeName;
		switch (a.getType()) {
		case SCOPE_NS:
			scopeName= a.getSegmentName();
			if (scopeName == null) {
				return false;
			}
			if (operator) {
				appendSymbol(sb, scopeName);
				sb.append("::"); //$NON-NLS-1$
			}
			else {
				return false;
			}
			return true;
		case SCOPE_NS_INT:
			scopeName= a.getSegmentName();
			if (scopeName == null) {
				return false;
			}
			if (operator) {
				appendSymbol(sb, scopeName);
				sb.append(":::"); //$NON-NLS-1$
			}
			else {
				sb.append("getNamespace(\""); //$NON-NLS-1$
				sb.append(scopeName);
				sb.append("\")"); //$NON-NLS-1$
			}
			return true;
		case SCOPE_SEARCH_ENV:
			scopeName= a.getSegmentName();
			if (scopeName == null) {
				return false;
			}
			sb.append("as.environment(\""); //$NON-NLS-1$
			sb.append(scopeName);
			sb.append("\")"); //$NON-NLS-1$
			if (operator) {
				sb.append('$');
			}
			return true;
		case SCOPE_PACKAGE:
			scopeName= a.getSegmentName();
			if (scopeName == null) {
				return false;
			}
			sb.append("as.environment(\"package:"); //$NON-NLS-1$
			sb.append(scopeName);
			sb.append("\")"); //$NON-NLS-1$
			if (operator) {
				sb.append('$');
			}
			return true;
		case SCOPE_SYSFRAME:
			scopeName= a.getSegmentName();
			if (scopeName == null) {
				return false;
			}
			sb.append("sys.frame("); //$NON-NLS-1$
			sb.append(scopeName);
			sb.append(((options & DISPLAY_EXACT) != 0) ? "L)" : ")"); //$NON-NLS-1$ //$NON-NLS-2$
			if (operator) {
				sb.append('$');
			}
			return true;
		case SCOPE_PROJECT:
			sb= new StringBuilder(44);
			sb.append(".GlobalEnv"); //$NON-NLS-1$
			if (operator) {
				sb.append('$');
			}
			return true;
		default:
			return false;
		}
	}
	
	
	private static final Collator NAME_COLLATOR= RSymbolComparator.R_NAMES_COLLATOR;
	
	public static final Comparator<IElementName> NAMEONLY_COMPARATOR= new Comparator<IElementName>() {
		
		@Override
		public int compare(IElementName o1, IElementName o2) {
			final String n1= o1.getSegmentName();
			final String n2= o2.getSegmentName();
			if (n1 != null) {
				if (n2 != null) {
					final int diff= NAME_COLLATOR.compare(n1, n2);
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
			
			o1= o1.getNextSegment();
			o2= o2.getNextSegment();
			if (o1 != null) {
				if (o2 != null) {
					final int diff= o1.getType() - o2.getType();
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
		
		
		private static final long serialVersionUID= 315497720879434929L;
		
		
		private final int type;
		private final String segmentName;
		private RElementName scope;
		private RElementName nextSegment;
		
		
		public DefaultImpl(final int type, final String segmentName) {
			this.type= type;
			this.segmentName= segmentName;
			this.nextSegment= null;
		}
		
		public DefaultImpl(final int type, final RElementName scope, final String segmentName, final RElementName next) {
			this.type= type;
			this.segmentName= segmentName;
			this.scope= scope;
			this.nextSegment= next;
		}
		
		public DefaultImpl(final int type, final String segmentName, final RElementName next) {
			this.type= type;
			this.segmentName= segmentName;
			this.nextSegment= next;
		}
		
		
		@Override
		public int getType() {
			return this.type;
		}
		
		@Override
		public String getSegmentName() {
			return this.segmentName;
		}
		
		@Override
		public RElementName getScope() {
			return this.scope;
		}
		
		@Override
		public RElementName getNextSegment() {
			return this.nextSegment;
		}
		
	}
	
	private static class DualImpl extends DefaultImpl implements IndexElementName {
		
		private static final long serialVersionUID= 7040207683623992047L;
		
		private final int idx;
		
		public DualImpl(final int type, final String segmentName, final int idx) {
			super(type, segmentName);
			this.idx= idx;
		}
		
		public DualImpl(final int type, final String segmentName, final int idx,
				final RElementName next) {
			super(type, segmentName, next);
			this.idx= idx;
		}
		
		
		@Override
		protected DefaultImpl cloneSegment0(final RElementName next) {
			return new DualImpl(getType(), getSegmentName(), this.idx, next);
		}
		
		
		@Override
		public int getIndex() {
			return this.idx;
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
	
	public static RElementName create(final List<RElementName> segments) {
		if (segments.size() > 0) {
			int first= 0;
			RElementName scopeName= null;
			if (segments.size() > 1) {
				scopeName= segments.get(0);
				if (isScopeType(scopeName.getType())) {
					first= 1;
				}
				else {
					scopeName= null;
				}
			}
			if (segments.size() > first) {
				RElementName next= null;
				for (int i= segments.size() - 1; i > first; i--) {
					next= segments.get(i).cloneSegment0(next);
				}
				next= new DefaultImpl(segments.get(first).getType(), scopeName, segments.get(first).getSegmentName(), next);
				return next;
			}
		}
		return null;
	}
	
	/**
	 * Creates a copy of segments of the specified element. The copy starts with the first element
	 * of the element name and ends at the specified end segment (exclusive).
	 * 
	 * @param name the element name to copy
	 * @param end the end segment (exlusive) or <code>null</code>, to copy the complete name
	 * @param withScope to include the scope in the copy, if available
	 * @return the copy of the element name
	 */
	public static RElementName create(RElementName name, final RElementName end,
			final boolean withScope) {
		if (name == null) {
			return null;
		}
		RElementName scopeName= (withScope) ? name.getScope() : null;
		if (scopeName != null) {
			scopeName= new DefaultImpl(scopeName.getType(), scopeName.getSegmentName(), null);
		}
		final DefaultImpl main= new DefaultImpl(name.getType(), scopeName, name.getSegmentName(), null);
		DefaultImpl last= main;
		name= name.getNextSegment();
		while (name != null && name != end) {
			final DefaultImpl copy= name.cloneSegment0(null);
			last.nextSegment= copy;
			last= copy;
			name= name.getNextSegment();
		}
		return main;
	}
	
	
	private static final int PARSE_OP= -1;
	private static final int PARSE_EXIT= -3;
	
	public static RElementName parseDefault(final String code) {
		final RLexer lexer= new RLexer((RLexer.DEFAULT |
						RLexer.SKIP_WHITESPACE | RLexer.SKIP_LINEBREAK | RLexer.SKIP_COMMENT ));
		lexer.reset(new StringParserInput(code).init());
		
		int mode= MAIN_DEFAULT;
		DefaultImpl main= null;
		DefaultImpl last= null;
		while (mode != PARSE_EXIT) {
			DefaultImpl tmp= null;
			RTerminal type= lexer.next();
			if (type == null || type == RTerminal.EOF) {
				if (mode < 0) {
					return main;
				}
				tmp= new DefaultImpl(mode, ""); //$NON-NLS-1$
				mode= PARSE_EXIT;
			}
			else {
				switch (type) {
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
					if (mode != MAIN_DEFAULT
							&& mode != SUB_NAMEDPART && mode != SUB_NAMEDSLOT) {
						return null;
					}
					tmp= new DefaultImpl(mode, type.text);
					type= lexer.next();
					if (type != null && type != RTerminal.EOF) {
						return null;
					}
					mode= PARSE_EXIT; // valid prefix
					break;
				case SYMBOL:
				case SYMBOL_G:
					if (mode != MAIN_DEFAULT
							&& mode != SUB_NAMEDPART && mode != SUB_NAMEDSLOT) {
						return null;
					}
					tmp= new DefaultImpl(mode, lexer.getText());
					mode= PARSE_OP;
					break;
				case STRING_S:
				case STRING_D:
					if (mode != MAIN_DEFAULT
							&& mode != SUB_NAMEDPART && mode != SUB_NAMEDSLOT) {
						return null;
					}
					tmp= new DefaultImpl(mode, lexer.getText());
					mode= PARSE_OP;
					break;
				case NUM_INT:
				case NUM_NUM:
					if (mode != SUB_INDEXED_S && mode != SUB_INDEXED_D) {
						return null;
					}
					tmp= new DefaultImpl(mode, lexer.getText());
					type= lexer.next();
					if (type != RTerminal.SUB_INDEXED_CLOSE) {
						return null;
					}
					if (mode == SUB_INDEXED_D) {
						type= lexer.next();
						if (type != RTerminal.SUB_INDEXED_CLOSE) {
							return null;
						}
					}
					mode= PARSE_OP;
					break;
				case SUB_NAMED_PART:
					if (main == null || mode >= 0) {
						return null;
					}
					mode= SUB_NAMEDPART;
					continue;
				case SUB_NAMED_SLOT:
					if (main == null || mode >= 0) {
						return null;
					}
					mode= SUB_NAMEDSLOT;
					continue;
				case SUB_INDEXED_S_OPEN:
					if (main == null || mode >= 0) {
						return null;
					}
					mode= SUB_INDEXED_S;
					continue;
				case SUB_INDEXED_D_OPEN:
					if (main == null || mode >= 0) {
						return null;
					}
					mode= SUB_INDEXED_D;
					continue;
//				case SUB_INDEXED_CLOSE:
//					return null;
				case NS_GET:
					if (main == null || main != last || mode >= 0) {
						return null;
					}
					if (main.getType() == MAIN_DEFAULT) {
						main= new DefaultImpl(SCOPE_NS, main.getSegmentName());
					}
					else {
						return null;
					}
					
					mode= MAIN_DEFAULT;
					continue;
				case NS_GET_INT:
					if (main == null || main != last || mode >= 0) {
						return null;
					}
					if (main.getType() == MAIN_DEFAULT) {
						main= new DefaultImpl(SCOPE_NS_INT, main.getSegmentName());
					}
					else {
						return null;
					}
					
					mode= MAIN_DEFAULT;
					continue;
				default:
					return null;
				}
				
			}
			
			if (main == null) {
				main= last= tmp;
			}
			else if (isScopeType(main.getType())) {
				tmp.scope= main;
				main= last= tmp;
			}
			else {
				last.nextSegment= tmp;
				last= tmp;
			}
		}
		return main;
	}
	
	
	/**
	 * Creates a copy of the specified element name.
	 * 
	 * @param name the element name to copy
	 * @param withScope to include the scope in the copy, if available
	 * @return the copy of the element name
	 */
	public static RElementName cloneName(RElementName name,
			final boolean withScope) {
		if (name == null) {
			return null;
		}
		RElementName scopeName= (withScope) ? name.getScope() : null;
		if (scopeName != null) {
			scopeName= new DefaultImpl(scopeName.getType(), scopeName.getSegmentName(), null);
		}
		final DefaultImpl main= new DefaultImpl(name.getType(), scopeName, name.getSegmentName(), null);
		DefaultImpl last= main;
		name= name.getNextSegment();
		while (name != null) {
			final DefaultImpl copy= name.cloneSegment0(null);
			last.nextSegment= copy;
			last= copy;
			name= name.getNextSegment();
		}
		return main;
	}
	
	/**
	 * Creates a copy of the first segment of the specified element name.
	 * 
	 * @param name the element name to copy
	 * @return the copy of the element name
	 */
	public static RElementName cloneSegment(final RElementName name) {
		return name.cloneSegment0(null);
	}
	
	public static RElementName addScope(final RElementName name, final RElementName scope) {
		if (!isScopeType(scope.getType())) {
			throw new IllegalArgumentException("scope.type= " + scope.getType()); //$NON-NLS-1$
		}
		return new DefaultImpl(name.getType(),
				(scope.getNextSegment() == null) ? scope : cloneSegment(scope),
				name.getSegmentName(), name.getNextSegment() );
	}
	
	public static RElementName normalize(final RElementName name) {
		if (name != null && name.getScope() == null && isScopeType(name.getType())
				&& name.getNextSegment() != null) {
			return addScope(name.getNextSegment(), name);
		}
		return name;
	}
	
	
	protected RElementName() {
	}
	
	
	public abstract RElementName getScope();
	@Override
	public abstract RElementName getNextSegment();
	
	@Override
	public String getDisplayName() {
		return createDisplayName(this, 0);
	}
	
	public String getDisplayName(final int options) {
		return createDisplayName(this, options);
	}
	
	
	protected RElementName.DefaultImpl cloneSegment0(final RElementName next) {
		return new DefaultImpl(getType(), getSegmentName(), next);
	}
	
	
	@Override
	public final int hashCode() {
		final String name= getSegmentName();
		final IElementName next= getNextSegment();
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
		final IElementName other= (RElementName) obj;
		final String thisName= getSegmentName();
		final String otherName= other.getSegmentName();
		return ((getType() == other.getType())
				&& ((thisName != null) ? 
						(thisName == otherName || (otherName != null && thisName.hashCode() == otherName.hashCode() && thisName.equals(otherName)) ) : 
						(null == other.getSegmentName()) )
				&& ((getNextSegment() != null) ? 
						(getNextSegment().equals(other.getNextSegment())) :
						(null == other.getNextSegment()) ) );
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
	
}
