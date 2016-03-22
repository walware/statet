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


public class RValueFormatter06rawTest {
	
	
	private final RValueFormatter formatter= new RValueFormatter();
	
	
	public RValueFormatter06rawTest() {
	}
	
	
	@Test
	public void print() {
		this.formatter.clear();
		this.formatter.appendRaw((byte) 0);
		Assert.assertEquals("00", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendRaw((byte) 1);
		Assert.assertEquals("01", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendRaw((byte) 0x0F);
		Assert.assertEquals("0F", this.formatter.getString());
		
		this.formatter.clear();
		this.formatter.appendRaw((byte) 0xFF);
		Assert.assertEquals("FF", this.formatter.getString());
	}
	
}
