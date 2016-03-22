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

import java.util.Arrays;

import com.ibm.icu.lang.UCharacter;

import org.junit.Assert;
import org.junit.Test;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RComplexStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RLogicalStore;
import de.walware.rj.data.RNumericStore;
import de.walware.rj.data.RRawStore;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;


public class RValueFormatterValidatorTest {
	
	
	private final RValueFormatter formatter= new RValueFormatter();
	
	private final RValueValidator validator= new RValueValidator();
	
	
	public RValueFormatterValidatorTest() {
	}
	
	
	@Test
	public void reparse_01logi() {
		final RLogicalStore store= RObjectFactoryImpl.INSTANCE.createLogiData(2);
		store.setLogi(0, true);
		store.setLogi(1, false);
		
		for (int i= 0; i < store.getLength(); i++) {
			final String fString= this.formatter.format(store, i);
//			this.formatter.clear();
//			this.formatter.appendLogi(store.getNum(i));
//			final String aString= this.formatter.getString();
			
			final RStore<?> vData= this.validator.toRData(store, fString);
			Assert.assertEquals(false, vData.isNA(0));
			Assert.assertEquals(store.getLogi(i), vData.getLogi(0));
		}
	}
	
	@Test
	public void reparse_01logi_NA() {
		final RLogicalStore store= RObjectFactoryImpl.INSTANCE.createLogiData(1);
		store.setNA(0);
		
		final String fString= this.formatter.format(store, 0);
		this.formatter.clear();
		this.formatter.appendNA();
		final String aString= this.formatter.getString();
		
		final RStore<?> vData= this.validator.toRData(store, fString);
		Assert.assertEquals(true, vData.isNA(0));
		
		Assert.assertEquals(fString, aString);
	}
	
	@Test
	public void reparse_04int() {
		final RIntegerStore store= RObjectFactoryImpl.INSTANCE.createIntData(new int[] {
				0,
				1,
				-1,
				0xFFFF,
				Integer.MAX_VALUE,
				Integer.MIN_VALUE + 1,
		});
		
		for (int i= 0; i < store.getLength(); i++) {
			final String fString= this.formatter.format(store, i);
//			this.formatter.clear();
//			this.formatter.appendInt(store.getNum(i));
//			final String aString= this.formatter.getString();
			
			final RStore<?> vData= this.validator.toRData(store, fString);
			
			Assert.assertEquals(false, vData.isNA(0));
			Assert.assertEquals(store.getNum(i), vData.getNum(0), 0);
			
//			Assert.assertEquals(fString, aString);
		}
	}
	
	@Test
	public void reparse_02int_NA() {
		final RIntegerStore store= RObjectFactoryImpl.INSTANCE.createIntData(1);
		store.setNA(0);
		
		final String fString= this.formatter.format(store, 0);
		this.formatter.clear();
		this.formatter.appendNA();
		final String aString= this.formatter.getString();
		
		final RStore<?> vData= this.validator.toRData(store, fString);
		Assert.assertEquals(true, vData.isNA(0));
		
		Assert.assertEquals(fString, aString);
	}
	
	@Test
	public void reparse_03num() {
		final RNumericStore store= RObjectFactoryImpl.INSTANCE.createNumData(new double[] {
				0.0,
				-0.0,
				1.0,
				1e15,
				1e-15,
				-1e+15,
				-1e-15,
				1234567890.0,
				0.000000000000001,
				1.0000000000000011,
				1.0000000000000012,
				1.0000000000000013,
				4.0/3,
				Double.MAX_VALUE,
				Double.MIN_VALUE,
				Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY,
				Double.NaN,
		});
		
		for (int i= 0; i < store.getLength(); i++) {
			final String fString= this.formatter.format(store, i);
			this.formatter.clear();
			this.formatter.appendNum(store.getNum(i));
			final String aString= this.formatter.getString();
			
			final RStore<?> vData= this.validator.toRData(store, fString);
			
			Assert.assertEquals(false, vData.isNA(0));
			Assert.assertEquals(store.getNum(i), vData.getNum(0), 0);
			
			Assert.assertEquals(fString, aString);
		}
	}
	
	@Test
	public void reparse_03num_NA() {
		final RNumericStore store= RObjectFactoryImpl.INSTANCE.createNumData(1);
		store.setNA(0);
		
		final String fString= this.formatter.format(store, 0);
		this.formatter.clear();
		this.formatter.appendNA();
		final String aString= this.formatter.getString();
		
		final RStore<?> vData= this.validator.toRData(store, fString);
		Assert.assertEquals(true, vData.isNA(0));
		
		Assert.assertEquals(fString, aString);
	}
	
	@Test
	public void reparse_04num_NA() {
		final RComplexStore store= RObjectFactoryImpl.INSTANCE.createCplxData(1);
		store.setNA(0);
		
		final String fString= this.formatter.format(store, 0);
		this.formatter.clear();
		this.formatter.appendNA();
		final String aString= this.formatter.getString();
		
		final RStore<?> vData= this.validator.toRData(store, fString);
		Assert.assertEquals(true, vData.isNA(0));
		
		Assert.assertEquals(fString, aString);
	}
	
	@Test
	public void reparse_05chr() {
		final char[] chars= new char[0xFFFF + 4];
		Arrays.fill(chars, 'X');
		chars[0]= 'X';
		for (int i= 1; i < 0xFFFF; i++) {
			if (UCharacter.isBMP(i)) {
				chars[i]= (char) i;
			}
			else {
				chars[i]= 'X';
			}
		}
		chars[0xFFFF + 0]= '\uD834';
		chars[0xFFFF + 1]= '\uDD73';
		chars[0xFFFF + 2]= '\uD834';
		chars[0xFFFF + 3]= '\uDD1E';
		final String s= new String(chars);
		
		final RCharacterStore store= RObjectFactoryImpl.INSTANCE.createCharData(1);
		store.setChar(0, s);
		final String fString= this.formatter.format(store, 0);
		this.formatter.clear();
		this.formatter.appendStringD(s);
		final String aString= this.formatter.getString();
		
		final RStore<?> vData= this.validator.toRData(RObjectFactoryImpl.CHR_STRUCT_DUMMY, fString);
		Assert.assertEquals(false, vData.isNA(0));
		Assert.assertEquals(s, vData.getChar(0));
		
		Assert.assertEquals(fString, aString);
	}
	
	@Test
	public void reparse_05chr_NA() {
		final RCharacterStore store= RObjectFactoryImpl.INSTANCE.createCharData(1);
		store.setNA(0);
		
		final String fString= this.formatter.format(store, 0);
		this.formatter.clear();
		this.formatter.appendNA();
		final String aString= this.formatter.getString();
		
		final RStore<?> vData= this.validator.toRData(store, fString);
		Assert.assertEquals(true, vData.isNA(0));
		
		Assert.assertEquals(fString, aString);
	}
	
	@Test
	public void reparse_06raw() {
		final RRawStore store= RObjectFactoryImpl.INSTANCE.createRawData(1);
		for (int i= 0; i <= 0xFF; i++) {
			final byte b= (byte) i;
			store.setRaw(0, b);
			final String fString= this.formatter.format(store, 0);
			this.formatter.clear();
			this.formatter.appendRaw(b);
			final String aString= this.formatter.getString();
			
			final RStore<?> vData= this.validator.toRData(RObjectFactoryImpl.RAW_STRUCT_DUMMY, fString);
			Assert.assertEquals(false, vData.isNA(0));
			Assert.assertEquals(b, vData.getRaw(0));
			
			Assert.assertEquals(fString, aString);
		}
	}
	
}
