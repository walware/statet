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

import de.walware.ecommons.text.core.input.StringParserInput;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RComplexStore;
import de.walware.rj.data.RFactorStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RLogicalStore;
import de.walware.rj.data.RNumericStore;
import de.walware.rj.data.RRawStore;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.IRSourceConstants;
import de.walware.statet.r.core.rsource.RLexer;


public class RValueValidator {
	
	
	private static final byte INVALID= 0;
	private static final byte UNKNOWN= 1;
	private static final byte VALID= 2;
	
	
	private int addSign(final int sign, final int value) {
		return (sign == -1) ? -value : value;
	}
	
	private double addSign(final int sign, final double value) {
		return (sign == -1) ? -value : value;
	}
	
	
	private final RLexer lexer= new RLexer(RLexer.ENABLE_NUM_VALUE);
	
	private final RLogicalStore logiValue= RObjectFactoryImpl.INSTANCE.createLogiData(1);
	private final RIntegerStore intValue= RObjectFactoryImpl.INSTANCE.createIntData(1);
	private final RNumericStore numValue= RObjectFactoryImpl.INSTANCE.createNumData(1);
	private final RComplexStore complexValue= RObjectFactoryImpl.INSTANCE.createCplxData(1);
	private final RCharacterStore charValue= RObjectFactoryImpl.INSTANCE.createCharData(1);
	private final RRawStore rawValue= RObjectFactoryImpl.INSTANCE.createRawData(1);
	
	private RValueFormatter formatter;
	
	
	public RValueValidator() {
	}
	
	
	public boolean isValid(final RStore<?> store, String expression) {
		switch (store.getStoreType()) {
		case RStore.LOGICAL:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
				return true;
			case RStore.NUMERIC:
				return (real2logi());
			default:
				return false;
			}
			
		case RStore.INTEGER:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
			case RStore.INTEGER:
				return true;
			case RStore.NUMERIC:
				return (real2int());
			default:
				return false;
			}
			
		case RStore.NUMERIC:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
			case RStore.INTEGER:
			case RStore.NUMERIC:
				return true;
			default:
				return false;
			}
			
		case RStore.COMPLEX:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
			case RStore.INTEGER:
			case RStore.NUMERIC:
			case RStore.COMPLEX:
				return true;
			default:
				return false;
			}
			
		case RStore.CHARACTER:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
			case RStore.INTEGER:
			case RStore.NUMERIC:
			case RStore.COMPLEX:
			case RStore.CHARACTER:
				return true;
			default:
				return false;
			}
			
		case RStore.RAW:
			expression= expression.trim();
			switch (parseSimpleHexInt(expression)) {
			case VALID:
				return (int2raw());
			case UNKNOWN:
				switch (parseValueType(expression)) {
				case RStore.LOGICAL:
					return (!this.logiValue.isNA(0));
				case RStore.NUMERIC:
					return (expression.startsWith("0x") && real2int() && int2raw());
				case RStore.INTEGER:
					return (int2raw());
				default:
					return false;
				}
			default:
				return false;
			}
		
		case RStore.FACTOR:
			return parseFactorLevel(expression, ((RFactorStore) store).getLevels());
		
		default:
			return false;
		}
	}
	
	public RStore<?> toRData(final RStore<?> store, String expression) {
		switch (store.getStoreType()) {
		case RStore.LOGICAL:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
				return this.logiValue;
			case RStore.NUMERIC:
				if (real2logi()) {
					return this.logiValue;
				}
				return null;
			default:
				return null;
			}
			
		case RStore.INTEGER:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
				return this.logiValue;
			case RStore.INTEGER:
				return this.intValue;
			case RStore.NUMERIC:
				if (real2int()) {
					return this.intValue;
				}
				return null;
			default:
				return null;
			}
			
		case RStore.NUMERIC:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
				return this.logiValue;
			case RStore.INTEGER:
				return this.intValue;
			case RStore.NUMERIC:
				return this.numValue;
			default:
				return null;
			}
			
		case RStore.COMPLEX:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
				return this.logiValue;
			case RStore.INTEGER:
				return this.intValue;
			case RStore.NUMERIC:
				return this.numValue;
			case RStore.COMPLEX:
				return this.complexValue;
			default:
				return null;
			}
			
		case RStore.CHARACTER:
			expression= expression.trim();
			switch (parseValueType(expression)) {
			case RStore.LOGICAL:
				return this.logiValue;
			case RStore.INTEGER:
				return this.intValue;
			case RStore.NUMERIC:
				return this.numValue;
			case RStore.COMPLEX:
				return this.complexValue;
			case RStore.CHARACTER:
				return this.charValue;
			default:
				return null;
			}
			
		case RStore.RAW:
			expression= expression.trim();
			switch (parseSimpleHexInt(expression)) {
			case VALID:
				if (int2raw()) {
					return this.rawValue;
				}
				return null;
			case UNKNOWN:
				switch (parseValueType(expression)) {
				case RStore.LOGICAL:
					if (!this.logiValue.isNA(0)) {
						this.rawValue.setRaw(0, this.logiValue.getRaw(0));
						return this.rawValue;
					}
					return null;
				case RStore.NUMERIC:
					if (expression.startsWith("0x") && real2int() && int2raw()) {
						return this.rawValue;
					}
					return null;
				case RStore.INTEGER:
					if (int2raw()) {
						return this.rawValue;
					}
					return null;
				default:
					return null;
				}
			default:
				return null;
			}
		
		case RStore.FACTOR:
			if (parseFactorLevel(expression, ((RFactorStore) store).getLevels())) {
				return this.charValue;
			}
			return null;
			
		default:
			return null;
		}
	}
	
	
	private byte parseValueType(final String expression) {
		this.lexer.reset(new StringParserInput(expression).init());
		
		RTerminal nextToken= this.lexer.next();
		
		int sign;
		switch (nextToken) {
		case PLUS:
			sign= +1;
			nextToken= this.lexer.next();
			break;
		case MINUS:
			sign= -1;
			nextToken= this.lexer.next();
			break;
		default:
			sign= 0;
			break;
		}
		
		if ((this.lexer.getFlags() & IRSourceConstants.STATUSFLAG_REAL_ERROR) != 0) {
			return INVALID;
		}
		
		switch (nextToken) {
		case TRUE:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.logiValue.setLogi(0, true);
					return RStore.LOGICAL;
				}
				break;
			default:
				break;
			}
			return INVALID;
		case FALSE:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.logiValue.setLogi(0, false);
					return RStore.LOGICAL;
				}
				break;
			default:
				break;
			}
			return INVALID;
		case NA:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.logiValue.setNA(0);
					return RStore.LOGICAL;
				}
				break;
			default:
				break;
			}
			return INVALID;
		
		case NUM_INT: {
			final int value= (int) this.lexer.getNumValue();
			switch (this.lexer.next()) {
			case EOF:
				this.intValue.setInt(0, addSign(sign, value));
				return RStore.INTEGER;
			default:
				break;
			}
			return INVALID; }
		case NA_INT:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.intValue.setNA(0);
					return RStore.INTEGER;
				}
				break;
			default:
				break;
			}
			return INVALID;
		
		case NUM_NUM:
		case INF: {
			double value= this.lexer.getNumValue();
			switch (this.lexer.next()) {
			case EOF:
				this.numValue.setNum(0, addSign(sign, value));
				return RStore.NUMERIC;
			case PLUS:
			case MINUS:
				this.complexValue.setNum(0, addSign(sign, value));
				sign= (this.lexer.getType() == RTerminal.PLUS) ? 1 : -1;
				nextToken= this.lexer.next();
				if ((this.lexer.getFlags() & IRSourceConstants.STATUSFLAG_REAL_ERROR) != 0) {
					return INVALID;
				}
				switch (nextToken) {
				case NUM_CPLX:
					value= this.lexer.getNumValue();
					switch (this.lexer.next()) {
					case EOF:
						this.complexValue.setCplx(0, this.complexValue.getCplxRe(0),
								addSign(sign, value) );
						return RStore.COMPLEX;
					default:
						break;
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			return INVALID; }
		case NAN:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.numValue.setNum(0, Double.NaN);
					return RStore.NUMERIC;
				}
				break;
			default:
				break;
			}
			return INVALID;
		case NA_REAL:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.numValue.setNA(0);
					return RStore.NUMERIC;
				}
				break;
			default:
				break;
			}
			return INVALID;
		
		case NUM_CPLX: {
			final double value= this.lexer.getNumValue();
			switch (this.lexer.next()) {
			case EOF:
				this.complexValue.setCplx(0, 0, addSign(sign, value));
				return RStore.COMPLEX;
			default:
				break;
			}
			return INVALID; }
		case NA_CPLX:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.complexValue.setNA(0);
					return RStore.COMPLEX;
				}
				break;
			default:
				break;
			}
			return INVALID;
		
		case STRING_D:
		case STRING_S: {
			final String value= this.lexer.getText();
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.charValue.setChar(0, value);
					return RStore.CHARACTER;
				}
				break;
			default:
				break;
			}
			return INVALID; }
		case NA_CHAR:
			switch (this.lexer.next()) {
			case EOF:
				if (sign == 0) {
					this.charValue.setNA(0);
					return RStore.CHARACTER;
				}
				break;
			default:
				break;
			}
			return INVALID;
			
		default:
			return INVALID;
		}
	}
	
	private boolean parseFactorLevel(String expression, final RCharacterStore levels) {
		if (expression.indexOf('\\') >= 0) {
			if (this.formatter == null) {
				this.formatter= new RValueFormatter();
			}
			expression= this.formatter.quoteName(expression);
			this.lexer.reset(new StringParserInput(expression).init());
			
			if (this.lexer.next() != RTerminal.STRING_D
					|| (this.lexer.getFlags() & IRSourceConstants.STATUSFLAG_REAL_ERROR) != 0) {
				return false;
			}
			expression= this.lexer.getText();
			switch (this.lexer.next()) {
			case EOF:
				break;
			default:
				return false;
			}
		}
		
		if (levels.contains(expression)
				|| (expression != (expression= expression.trim()) && levels.contains(expression)) ) {
			this.charValue.setChar(0, expression);
			return true;
		}
		if (expression.equals("NA")
				|| (expression.equals("<NA>") && levels.containsNA()) ) {
			this.charValue.setNA(0);
			return true;
		}
		return false;
	}
	
	private byte parseSimpleHexInt(final String expression) {
		final int l= expression.length();
		if (l == 0) {
			return INVALID;
		}
		for (int i= 0; i < l; i++) {
			switch (expression.charAt(i)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				continue;
			default:
				return UNKNOWN;
			}
		}
		try {
			this.intValue.setInt(0, Integer.parseInt(expression, 16));
			return VALID;
		}
		catch (final NumberFormatException e) {
			return INVALID;
		}
	}
	
	private boolean real2logi() {
		if (!this.numValue.isNA(0)) {
			final double d= this.numValue.getNum(0);
			final int i= (int) d;
			if (d == i && i >= 0 && i <= 1) {
				this.logiValue.setInt(0, i);
				return true;
			}
		}
		return false;
	}
	
	private boolean real2int() {
		if (!this.numValue.isNA(0)) {
			final double d= this.numValue.getNum(0);
			final int i= (int) d;
			if (d == i) {
				this.intValue.setInt(0, i);
				return true;
			}
		}
		return false;
	}
	
	private boolean int2raw() {
		final int i;
		if (!this.intValue.isNA(0)
				&& (i= this.intValue.getInt(0)) >= 0 && i <= 255) {
			this.rawValue.setRaw(0, (byte) i);
			return true;
		}
		return false;
	}
	
}
