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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import de.walware.rj.data.RStore;


public abstract class AbstractRValueValidatorTest {
	
	
	protected static final byte UNSUPPORTED= 0;
	
	
	protected final RValueValidator validator= new RValueValidator();
	
	protected final RStore<?> targetType;
	
	
	public AbstractRValueValidatorTest(final RStore<?> targetType) {
		this.targetType= targetType;
	}
	
	
	protected void testValidate(final boolean expected, final String expression) {
		assertEquals(expected, this.validator.isValid(this.targetType, expression));
		
		final RStore<?> data= this.validator.toRData(this.targetType, expression);
		if (expected) {
			Assert.assertNotNull(data);
			
			final byte storeType= data.getStoreType();
			switch (this.targetType.getStoreType()) {
			case RStore.LOGICAL:
			case RStore.INTEGER:
			case RStore.NUMERIC:
			case RStore.COMPLEX:
			case RStore.CHARACTER:
				assertTrue(storeType > 0 && storeType <= this.targetType.getStoreType());
				return;
				
			case RStore.RAW:
				assertEquals(RStore.RAW, storeType);
				return;
				
			case RStore.FACTOR:
				assertEquals(RStore.CHARACTER, storeType);
				return;
				
			default:
				throw new IllegalStateException();
			}
		}
		else {
			Assert.assertNull(data);
		}
	}
	
	
	protected boolean isValidSource(final byte type) {
		return (this.targetType.getStoreType() < RStore.RAW
				&& type <= this.targetType.getStoreType() );
	}
	
	protected boolean isValidSourceNA(final byte type) {
		return isValidSource(type);
	}
	
	
	@Test
	public void parseLogi() {
		boolean expected= isValidSource(RStore.LOGICAL);
		testValidate(expected, "TRUE");
		testValidate(expected, "FALSE");
		
		expected= isValidSourceNA(RStore.LOGICAL);
		testValidate(expected, "NA");
	}
	
	@Test
	public void parseLogi_invalid() {
		testValidate(false, "+TRUE");
		testValidate(false, "-TRUE");
		testValidate(false, "+FALSE");
		testValidate(false, "-FALSE");
		testValidate(false, "TRUE + 1");
		testValidate(false, "+NA");
		testValidate(false, "-NA");
		testValidate(false, "NA + 1");
	}
	
	
	@Test
	public void parseInt() {
		boolean expected= isValidSource(RStore.INTEGER);
		testValidate(expected, "0L");
		testValidate(expected, "1L");
		testValidate(expected, "0x7FFFFFFL");
		testValidate(expected, "-0xfL");
		
		expected= isValidSourceNA(RStore.INTEGER);
		testValidate(expected, "NA_integer_");
	}
	
	@Test
	public void parseInt_invalid() {
		testValidate(false, "1eL");
		testValidate(false, "0xFFpL");
		testValidate(false, "1L + 1");
		testValidate(false, "+NA_integer_");
		testValidate(false, "-NA_integer_");
		testValidate(false, "NA_integer_ + 1");
	}
	
	
	@Test
	public void parseNum() {
		boolean expected= isValidSource(RStore.NUMERIC);
		testValidate(expected, "0");
		testValidate(expected, "1");
		testValidate(expected, "0x7FFFFFF");
		testValidate(expected, "0x7FFFFFFFFL");
		testValidate(expected, "-1.55");
		testValidate(expected, "-1.0e-5");
		
		expected= isValidSourceNA(RStore.NUMERIC);
		testValidate(expected, "NA_real_");
	}
	
	@Test
	public void parseNum_invalid() {
		testValidate(false, "0x7FFFFFFFFp");
		testValidate(false, "-1.55e");
		testValidate(false, "-1.0e-5 + 1");
		testValidate(false, "+NA_real_");
		testValidate(false, "-NA_real_");
		testValidate(false, "NA_real_ + 1");
		testValidate(false, "NA_real_ + 1i");
	}
	
	
	@Test
	public void parseCplx() {
		final boolean expected= isValidSource(RStore.COMPLEX);
		testValidate(expected, "1-1i");
		testValidate(expected, "0x7FFFFFF+0xAAi");
		testValidate(expected, "-0x7FFFFFFFFi");
		testValidate(expected, "-1.55i");
		testValidate(expected, "-1.0e-5i");
		testValidate(expected, "NA_complex_");
	}
	
	@Test
	public void parseCplx_invalid() {
		testValidate(false, "1ei");
		testValidate(false, "1e+i");
		testValidate(false, "1i + 1");
		testValidate(false, "1 + 1i + 1");
		testValidate(false, "+NA_complex_");
		testValidate(false, "-NA_complex_");
		testValidate(false, "NA_complex_ + 1");
		testValidate(false, "NA_complex_ + 1i");
	}
	
	
	@Test
	public void parseChar() {
		boolean expected= isValidSource(RStore.CHARACTER);
		testValidate(expected, "\"\"");
		testValidate(expected, "\"1\"");
		testValidate(expected, "'Hello'");
		testValidate(expected, "\"Unicode>\\uAAAA<\"");
		
		expected= isValidSourceNA(RStore.CHARACTER);
		testValidate(expected, "NA_character_");
	}
	
	@Test
	public void parseChar_invalid() {
		testValidate(false, "+\"Hello\"");
		testValidate(false, "-\"Hello\"");
		testValidate(false, "\"Hello\" + 1");
		testValidate(false, "+NA_character_");
		testValidate(false, "-NA_character_");
		testValidate(false, "NA_character_ + 1");
	}
	
}
