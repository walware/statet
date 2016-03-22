/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.data;

import java.util.Locale;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;

import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RStore;


public class RValueFormatter {
	
	
	protected static final String NA_STRING= "NA"; //$NON-NLS-1$
	
	protected static final String NUM_INF_STRING= "Inf";
	protected static final String NUM_NAN_STRING= "NaN";
	
	protected static final String NAME_NA_STRING= "<NA>"; //$NON-NLS-1$
	
	private static final char[] DIGITS= new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	
	private static final DecimalFormatSymbols NUM_SYMBOLS;
	
	static {
		final DecimalFormatSymbols symbols= new DecimalFormatSymbols(Locale.ENGLISH);
		symbols.setExponentSeparator("e");
		symbols.setNaN(NUM_NAN_STRING);
		symbols.setInfinity(NUM_INF_STRING);
		NUM_SYMBOLS= symbols;
	}
	
	
	private final StringBuilder sb= new StringBuilder(80);
	
	private DecimalFormat numFormat;
	
	
	public RValueFormatter() {
	}
	
	
	private void append(final String s, final int start, final int end) {
		switch (end - start) {
		case 0:
			return;
		case 1:
			this.sb.append(s.charAt(start));
			return;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
			this.sb.append(s, start, end);
			return;
		default:
			this.sb.append(s.substring(start, end));
			return;
		}
	}
	
	private void appendUIntHex(final int value) {
		int pos= Math.max(((Integer.SIZE - Integer.numberOfLeadingZeros(value) + 3) / 4) - 1, 0);
		do {
			this.sb.append(DIGITS[(value >>> 4 * pos) & 0xF]);
		} while (--pos >= 0);
	}
	
	private void appendUIntHex2(final byte value) {
		this.sb.append(DIGITS[(value >>> 4) & 0x0F]);
		this.sb.append(DIGITS[value & 0x0F]);
	}
	
	public void appendNA() {
		this.sb.append(NA_STRING);
	}
	
	public void appendNum(final double value) {
		this.sb.append(formatNum(value));
		
//		if (Double.isInfinite(value)) {
//			if (value < 0.0) {
//				this.sb.append('-');
//			}
//			this.sb.append(NUM_INF_STRING);
//			return;
//		}
//		if (Double.isNaN(value)) {
//			this.sb.append(NUM_NAN_STRING);
//			return;
//		}
//		this.sb.append(value);
//		return;
	}
	
	private String formatNum(final double value) {
		if (this.numFormat == null) {
			this.numFormat= createNumFormat();
		}
		final double abs;
		this.numFormat.setScientificNotation(value != 0
				&& ((abs= Math.abs(value)) < 1e-4 || abs >= 1e5) );
		return this.numFormat.format(value);
	}
	
	protected DecimalFormat createNumFormat() {
		final DecimalFormat format= new DecimalFormat("0.0###################", NUM_SYMBOLS); //$NON-NLS-1$
		format.setDecimalSeparatorAlwaysShown(true);
		format.setGroupingUsed(false);
		format.setExponentSignAlwaysShown(true);
		format.setSignificantDigitsUsed(false);
		return format;
	}
	
	
	private boolean isPrintableChar(final int cp) {
		return (UCharacter.isPrintable(cp));
	}
	
	private void appendEscapedCodepoint(final int cp) {
		this.sb.append((cp <= 0xFFFF) ? "\\u{" : "\\U{"); //$NON-NLS-1$ //$NON-NLS-2$
		appendUIntHex(cp);
		this.sb.append('}');
	}
	
	private void appendEscapedQuoteD(final String value) {
		int idx= 0;
		int start= idx;
		while (idx < value.length()) {
			final char c= value.charAt(idx);
			if (c <= 255) {
				switch (c) {
				case 0x00:
				case 0x01:
				case 0x02:
				case 0x03:
				case 0x04:
				case 0x05:
				case 0x0E:
				case 0x0F:
				case 0x10:
				case 0x11:
				case 0x12:
				case 0x13:
				case 0x14:
				case 0x15:
				case 0x16:
				case 0x17:
				case 0x18:
				case 0x19:
				case 0x1A:
				case 0x1B:
				case 0x1C:
				case 0x1D:
				case 0x1E:
				case 0x1F:
				case 0x7F:
					append(value, start, idx);
					appendEscapedCodepoint(c);
					start= ++idx;
					continue;
				case 0x07:
					append(value, start, idx);
					this.sb.append("\\a"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x08:
					append(value, start, idx);
					this.sb.append("\\b"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x09:
					append(value, start, idx);
					this.sb.append("\\t"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0A:
					append(value, start, idx);
					this.sb.append("\\n"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0B:
					append(value, start, idx);
					this.sb.append("\\v"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0C:
					append(value, start, idx);
					this.sb.append("\\f"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0D:
					append(value, start, idx);
					this.sb.append("\\r"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case '\\':
					append(value, start, idx);
					this.sb.append('\\');
					start= idx++;
					continue;
				case '\"':
					append(value, start, idx);
					this.sb.append('\\');
					start= idx++;
					continue;
				default:
					idx++;
					continue;
				}
			}
			{	final int cp= value.codePointAt(idx);
				if (isPrintableChar(cp)) {
					if (cp != c) {
						idx+= 2;
					}
					else {
						idx++;
					}
					continue;
				}
				else {
					append(value, start, idx);
					appendEscapedCodepoint(cp);
					if (cp != c) {
						start= (idx+= 2);
					}
					else {
						start= ++idx;
					}
					continue;
				}
			}
		}
		append(value, start, idx);
	}
	
	public void appendStringD(final String value) {
		this.sb.ensureCapacity(value.length() + 2);
		this.sb.append('\"');
		appendEscapedQuoteD(value);
		this.sb.append('\"');
	}
	
	private void appendEscaped(final String value) {
		int idx= 0;
		int start= idx;
		while (idx < value.length()) {
			final char c= value.charAt(idx);
			if (c <= 255) {
				switch (c) {
				case 0x00:
				case 0x01:
				case 0x02:
				case 0x03:
				case 0x04:
				case 0x05:
				case 0x0E:
				case 0x0F:
				case 0x10:
				case 0x11:
				case 0x12:
				case 0x13:
				case 0x14:
				case 0x15:
				case 0x16:
				case 0x17:
				case 0x18:
				case 0x19:
				case 0x1A:
				case 0x1B:
				case 0x1C:
				case 0x1D:
				case 0x1E:
				case 0x1F:
				case 0x7F:
					append(value, start, idx);
					appendEscapedCodepoint(c);
					start= ++idx;
					continue;
				case 0x06:
					append(value, start, idx);
					this.sb.append("\\u{6}"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x07:
					append(value, start, idx);
					this.sb.append("\\a"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x08:
					append(value, start, idx);
					this.sb.append("\\b"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x09:
					append(value, start, idx);
					this.sb.append("\\t"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0A:
					append(value, start, idx);
					this.sb.append("\\n"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0B:
					append(value, start, idx);
					this.sb.append("\\v"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0C:
					append(value, start, idx);
					this.sb.append("\\f"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0D:
					append(value, start, idx);
					this.sb.append("\\r"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case '\\':
					append(value, start, idx);
					this.sb.append('\\');
					start= idx++;
					continue;
				default:
					idx++;
					continue;
				}
			}
			{	final int cp= value.codePointAt(idx);
			if (isPrintableChar(cp)) {
				if (cp != c) {
					idx+= 2;
				}
				else {
					idx++;
				}
				continue;
			}
			else {
				append(value, start, idx);
				appendEscapedCodepoint(cp);
				if (cp != c) {
					start= (idx+= 2);
				}
				else {
					start= ++idx;
				}
				continue;
			}
			}
		}
		append(value, start, idx);
	}
	
	private String escapeString(final String s) {
		this.sb.setLength(0);
		
		int idx= 0;
		int start= idx;
		while (idx < s.length()) {
			final char c= s.charAt(idx);
			if (c <= 255) {
				switch (c) {
				case 0x00:
				case 0x01:
				case 0x02:
				case 0x03:
				case 0x04:
				case 0x05:
				case 0x0E:
				case 0x0F:
				case 0x10:
				case 0x11:
				case 0x12:
				case 0x13:
				case 0x14:
				case 0x15:
				case 0x16:
				case 0x17:
				case 0x18:
				case 0x19:
				case 0x1A:
				case 0x1B:
				case 0x1C:
				case 0x1D:
				case 0x1E:
				case 0x1F:
				case 0x7F:
					append(s, start, idx);
					appendEscapedCodepoint(c);
					start= ++idx;
					continue;
				case 0x06:
					append(s, start, idx);
					this.sb.append("\\u{6}"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x07:
					append(s, start, idx);
					this.sb.append("\\a"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x08:
					append(s, start, idx);
					this.sb.append("\\b"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x09:
					append(s, start, idx);
					this.sb.append("\\t"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0A:
					append(s, start, idx);
					this.sb.append("\\n"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0B:
					append(s, start, idx);
					this.sb.append("\\v"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0C:
					append(s, start, idx);
					this.sb.append("\\f"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case 0x0D:
					append(s, start, idx);
					this.sb.append("\\r"); //$NON-NLS-1$
					start= ++idx;
					continue;
				case '\\':
					append(s, start, idx);
					this.sb.append("\\"); //$NON-NLS-1$
					start= idx++;
					continue;
				default:
					idx++;
					continue;
				}
			}
			{	final int cp= s.codePointAt(idx);
				if (isPrintableChar(cp)) {
					if (cp != c) {
						idx+= 2;
					}
					else {
						idx++;
					}
					continue;
				}
				else {
					append(s, start, idx);
					appendEscapedCodepoint(cp);
					if (cp != c) {
						start= (idx+= 2);
					}
					else {
						start= ++idx;
					}
					continue;
				}
			}
		}
		if (start != 0) {
			append(s, start, idx);
			return this.sb.toString();
		}
		return s;
	}
	
	
	public void appendRaw(final byte raw) {
		appendUIntHex2(raw);
	}
	
	
	public void clear() {
		this.sb.setLength(0);
	}
	
	
	public void append(final RStore<?> data, final int idx) {
		if (data.isNA(idx)) {
			this.sb.append(NA_STRING);
			return;
		}
		switch (data.getStoreType()) {
//		case RStore.LOGICAL:
//		case RStore.INTEGER:
		case RStore.NUMERIC:
			appendNum(data.getNum(idx));
			return;
		case RStore.CHARACTER:
			appendStringD(data.getChar(idx));
			return;
//		case RStore.COMPLEX:
		case RStore.RAW:
			appendRaw(data.getRaw(idx));
			return;
		case RStore.FACTOR:
			appendName(((RFactorStore) data).getLevels(), data.getInt(idx) - 1);
			return;
		default:
			this.sb.append(data.getChar(idx));
			return;
		}
	}
	
	public void appendName(final RStore<?> data, final int idx) {
		if (data.isNA(idx)) {
			this.sb.append(NAME_NA_STRING);
		}
		switch (data.getStoreType()) {
//		case RStore.INTEGER:
//		case RStore.NUMERIC:
		case RStore.CHARACTER:
			appendEscaped(data.getChar(idx));
			return;
		default:
			this.sb.append(data.getChar(idx));
			return;
		}
	}
	
	public final String getString() {
		return this.sb.toString();
	}
	
	
	public String format(final RStore<?> data, final int idx) {
		if (data.isNA(idx)) {
			return NA_STRING;
		}
		switch (data.getStoreType()) {
//		case RStore.LOGICAL:
//		case RStore.INTEGER:
		case RStore.NUMERIC:
			return formatNum(data.getNum(idx));
		case RStore.CHARACTER:
			clear();
			appendStringD(data.getChar(idx));
			return getString();
//		case RStore.COMPLEX:
		case RStore.RAW:
			clear();
			appendUIntHex2(data.getRaw(idx));
			return getString();
		case RStore.FACTOR:
			return formatName(((RFactorStore) data).getLevels(), data.getInt(idx) - 1);
		default:
			return data.getChar(idx);
		}
	}
	
	public String formatName(final RStore<?> data, final int idx) {
		if (data.isNA(idx)) {
			return NAME_NA_STRING;
		}
		switch (data.getStoreType()) {
//		case RStore.INTEGER:
//		case RStore.NUMERIC:
		case RStore.CHARACTER:
			return escapeString(data.getChar(idx));
		default:
			return data.getChar(idx);
		}
	}
	
	
	public String quoteName(final String name) {
		clear();
		this.sb.append('"');
		int idx= 0;
		int start= idx;
		while (idx < name.length()) {
			idx= name.indexOf('"', idx);
			if (idx >= 0) {
				append(name, start, idx);
				this.sb.append('\\');
				start= idx++;
			}
			else {
				break;
			}
		}
		append(name, start, name.length());
		this.sb.append('"');
		return getString();
	}
	
}
