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

import org.junit.Assert;
import org.junit.Test;


public class RValueFormatter03numTest {
	
	
	private final RValueFormatter formatter= new RValueFormatter();
	
	
	public RValueFormatter03numTest() {
	}
	
	
	@Test
	public void print() {
		this.formatter.clear();
		this.formatter.appendNum(0);
		Assert.assertEquals("0.0", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(1);
		Assert.assertEquals("1.0", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(10);
		Assert.assertEquals("10.0", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(10000);
		Assert.assertEquals("10000.0", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(100000);
		Assert.assertEquals("1.0e+5", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(1e40);
		Assert.assertEquals("1.0e+40", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(0.1);
		Assert.assertEquals("0.1", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(0.001);
		Assert.assertEquals("0.001", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(0.0001);
		Assert.assertEquals("0.0001", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(0.00001);
		Assert.assertEquals("1.0e-5", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(1e-40);
		Assert.assertEquals("1.0e-40", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(0.00009999999999999);
		Assert.assertEquals("9.999999999999e-5", this.formatter.getString());
	}
	
	@Test
	public void printNegative() {
		this.formatter.clear();
		this.formatter.appendNum(-0.0);
		Assert.assertEquals("-0.0", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(-1);
		Assert.assertEquals("-1.0", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(-1e15);
		Assert.assertEquals("-1.0e+15", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(-1e-15);
		Assert.assertEquals("-1.0e-15", this.formatter.getString());
	}
	
	@Test
	public void checkPrecision() {
		final double[] values= new double[] {
				0.000000000000001,
				1.0000000000000011,
				1.0000000000000012,
				1.0000000000000013,
				4.0/3,
				Double.MIN_VALUE
		};
		for (int i= 0; i < values.length; i++) {
			this.formatter.clear();
			this.formatter.appendNum(values[i]);
			final String expected= Double.toString(values[i]).toLowerCase();
			Assert.assertEquals(expected, this.formatter.getString());
		}
	}
	
	@Test
	public void printSpecial() {
		this.formatter.clear();
		this.formatter.appendNum(Double.NaN);
		Assert.assertEquals("NaN", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(Double.POSITIVE_INFINITY);
		Assert.assertEquals("Inf", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendNum(Double.NEGATIVE_INFINITY);
		Assert.assertEquals("-Inf", this.formatter.getString());
	}
	
}
