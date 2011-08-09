/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.Date;
import java.util.Locale;

import com.ibm.icu.math.BigDecimal;
import com.ibm.icu.math.MathContext;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import de.walware.rj.data.RCharacterStore;

import de.walware.statet.r.internal.ui.intable.InfoString;


public class RDataFormatter {
	
	
	private static final char[] HEX_CHARS = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	public static final int MILLIS_PER_SECOND = 1000;
	public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	public static final int MILLIS_PER_HOUR = 60 * 60 * MILLIS_PER_MINUTE;
	public static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
	
	private final StringBuilder fCurrentText = new StringBuilder();
	
	private DecimalFormat fNumFormat;
	private int fNumMaxExpDigits;
	private MathContext fMathContext;
	
	private DateFormat fDateFormat;
	private long fDateValueMillis;
	
	private RCharacterStore fFactorLevels;
	
	private InfoString fNA = new InfoString("NA");
	
	private int fAutoWidth = -1;
	
	
	
	public RDataFormatter() {
	}
	
	
	protected void clean() {
		fNumFormat = null;
		fMathContext = null;
		fDateFormat = null;
		fFactorLevels = null;
	}
	
	
	protected void appendByteHexFormat(final int b) {
		fCurrentText.append(HEX_CHARS[((b >> 2) & 0xf)]);
		fCurrentText.append(HEX_CHARS[(b & 0xf)]);
	}
	
	protected void appendNum(final double num) {
		if (Double.isNaN(num) || Double.isInfinite(num)) {
			fCurrentText.append(fNumFormat.format(num));
			return;
		}
		BigDecimal decimal = new BigDecimal(num);
		if (fMathContext != null) {
			decimal = decimal.multiply(BigDecimal.ONE, fMathContext);
		}
		fCurrentText.append(fNumFormat.format(decimal));
	}
	
	public Object modelToDisplayValue(Object modelValue) {
		if (modelValue == null) {
			return fNA;
		}
		final Class<?> clazz = modelValue.getClass();
		if (clazz == InfoString.class) {
			return modelValue;
		}
		if (clazz == Double.class) {
			if (fNumFormat != null) {
				fCurrentText.setLength(0);
				appendNum((Double) modelValue);
				return fCurrentText.toString();
			}
			if (fDateFormat != null) {
				return fDateFormat.format(new Date((long)
						((Double) modelValue).doubleValue() * fDateValueMillis) );
			}
		}
		if (clazz == Boolean.class) {
			return ((Boolean) modelValue).booleanValue() ? "TRUE" : "FALSE";
		}
		INT: if (clazz == Integer.class){
			if (fFactorLevels != null) {
				final int value = ((Integer) modelValue).intValue() - 1;
				if (value >= 0 && value < fFactorLevels.getLength()) {
					modelValue = fFactorLevels.getChar(value);
					break INT;
				}
				else {
					return new InfoString("?" + modelValue + "?");
				}
			}
			if (fDateFormat != null) {
				return fDateFormat.format(new Date(
						((Integer) modelValue).longValue() * fDateValueMillis) );
			}
			return modelValue.toString();
		}
		if (clazz == String.class) {
			final String text = (String) modelValue;
			fCurrentText.setLength(0);
			
			int beginIdx = 0;
			int i = 0;
			final int length = text.length();
			while (i < length) {
				final char c = text.charAt(i);
				switch (c) {
				case 10:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\n");
					beginIdx = ++i;
					continue;
				case 13:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\r");
					beginIdx = ++i;
					continue;
				case 9:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\t");
					beginIdx = ++i;
					continue;
				case 8:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\b");
					beginIdx = ++i;
					continue;
				case 7:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\a");
					beginIdx = ++i;
					continue;
				case 12:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\f");
					beginIdx = ++i;
					continue;
				case 11:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\v");
					beginIdx = ++i;
					continue;
				case 92:
					if (i > beginIdx) {
						fCurrentText.append(text, beginIdx, i);
					}
					fCurrentText.append("\\\\");
					beginIdx = ++i;
					continue;
				default:
					if (c < 0x20) {
						if (i > beginIdx) {
							fCurrentText.append(text, beginIdx, i);
						}
						fCurrentText.append("\\0x");
						appendByteHexFormat(c);
						beginIdx = ++i;
						continue;
					}
					i++;
					continue;
				}
			}
			if (beginIdx > 0) {
				if (beginIdx < length) {
					fCurrentText.append(text, beginIdx, length);
				}
				return fCurrentText.toString();
			}
			else {
				return text;
			}
		}
		if (clazz == Byte.class) {
			fCurrentText.setLength(0);
			appendByteHexFormat(((Byte) modelValue).intValue());
			return fCurrentText.toString();
		}
		return modelValue;
	}
	
	public void initNumFormat(final int maxFractionalDigits, final int maxExponentDigits) {
		clean();
		
		fNumMaxExpDigits = maxExponentDigits;
		final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
		symbols.setExponentSeparator("e");
		symbols.setNaN("NaN");
		symbols.setInfinity("Inf");
		
		final DecimalFormat decimalFormat = new DecimalFormat("0.", symbols);
		fNumFormat = decimalFormat;
		decimalFormat.setDecimalSeparatorAlwaysShown(false);
		decimalFormat.setGroupingUsed(false);
		decimalFormat.setExponentSignAlwaysShown(true);
		decimalFormat.setSignificantDigitsUsed(false);
		decimalFormat.setMinimumFractionDigits(maxFractionalDigits);
		decimalFormat.setMaximumFractionDigits(maxFractionalDigits);
		
		if (maxExponentDigits > 0) {
			fMathContext = new MathContext(maxFractionalDigits+1, MathContext.SCIENTIFIC, false, MathContext.ROUND_HALF_UP);
			decimalFormat.setScientificNotation(true);
			decimalFormat.setMinimumExponentDigits((byte) maxExponentDigits);
		}
		else {
			decimalFormat.setScientificNotation(false);
			decimalFormat.setRoundingMode(MathContext.ROUND_HALF_UP);
			decimalFormat.setRoundingIncrement(BigDecimal.valueOf(1L, maxFractionalDigits));
		}
	}
	
	public void initDateFormat(final int millis) {
		clean();
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		fDateFormat = dateFormat;
		
		fDateValueMillis = millis;
		fDateFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC"), ULocale.ENGLISH));
	}
	
	public void initDateTimeFormat(final int millis) {
		clean();
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zz");
		fDateFormat = dateFormat;
		
		fDateValueMillis = millis;
	}
	
	public void setDateTimeZone(final TimeZone zone) {
		if (fDateFormat == null) {
			throw new IllegalStateException();
		}
		fDateFormat.setTimeZone(zone);
	}
	
	public void initFactorLevels(final RCharacterStore levels) {
		clean();
		
		fFactorLevels = levels;
	}
	
	public RCharacterStore getFactorLevels() {
		return fFactorLevels;
	}
	
	
	public void setNAString(final String text) {
		fNA = new InfoString(text);
	}
	
	
	public void setAutoWidth(final int width) {
		fAutoWidth = width;
	}
	
	public int getAutoWidth() {
		return fAutoWidth;
	}
	
	
}
