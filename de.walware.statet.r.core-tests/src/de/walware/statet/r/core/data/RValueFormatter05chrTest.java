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


public class RValueFormatter05chrTest {
	
	
	private final RValueFormatter formatter= new RValueFormatter();
	
	
	public RValueFormatter05chrTest() {
	}
	
	
	private void assertStringD(final String expected) {
		final String s= this.formatter.getString();
		Assert.assertEquals('\"', s.charAt(0));
		Assert.assertEquals('\"', s.charAt(s.length() - 1));
		Assert.assertEquals(expected, s.substring(1, s.length() - 1));
	}
	
	
	@Test
	public void printRequireEscapeD() {
		this.formatter.clear();
		this.formatter.appendStringD("\\");
		assertStringD("\\\\");
		
		this.formatter.clear();
		this.formatter.appendStringD("\"");
		assertStringD("\\\"");
	}
	
	@Test
	public void printControlBasicSpecial() {
		this.formatter.clear();
		this.formatter.appendStringD("\u0007");
		assertStringD("\\a");
		
		this.formatter.clear();
		this.formatter.appendStringD("\u0008");
		assertStringD("\\b");
		
		this.formatter.clear();
		this.formatter.appendStringD("\t");
		assertStringD("\\t");
		
		this.formatter.clear();
		this.formatter.appendStringD("\n");
		assertStringD("\\n");
		
		this.formatter.clear();
		this.formatter.appendStringD("\u000B");
		assertStringD("\\v");
		
		this.formatter.clear();
		this.formatter.appendStringD("\u000C");
		assertStringD("\\f");
		
		this.formatter.clear();
		this.formatter.appendStringD("\r");
		assertStringD("\\r");
		
		this.formatter.clear();
		this.formatter.appendStringD("abc \r\n\t e123.");
		assertStringD("abc \\r\\n\\t e123.");
	}
	
	@Test
	public void printControlBasicOther() {
		this.formatter.clear();
		this.formatter.appendStringD("\u0001");
		assertStringD("\\u{1}");
		
		this.formatter.clear();
		this.formatter.appendStringD("\u001F");
		assertStringD("\\u{1F}");
		
		this.formatter.clear();
		this.formatter.appendStringD("\u007F");
		assertStringD("\\u{7F}");
		
		this.formatter.clear();
		this.formatter.appendStringD("abc \u0001 \u001F \u007F e123.");
		assertStringD("abc \\u{1} \\u{1F} \\u{7F} e123.");
	}
	
	@Test
	public void printPrintableBasic() {
		final char[] chars= new char[0xFF];
		int idx= 0;
		for (char c= 0x20; c <= 0xFF; c++) {
			switch (c) {
			case '\u007F':
			case '\\':
			case '\"':
				continue;
			default:
				chars[idx++]= c;
			}
		}
		final String s= new String(chars, 0, idx);
		this.formatter.clear();
		this.formatter.appendStringD(s);
		assertStringD(s);
	}
	
	@Test
	public void printNonPrintableExt() {
		this.formatter.clear();
		this.formatter.appendStringD("\uFFF0");
		assertStringD("\\u{FFF0}");
		
		this.formatter.clear();
		this.formatter.appendStringD("\uD834\uDD73");
		assertStringD("\\U{1D173}");
		
		this.formatter.clear();
		this.formatter.appendStringD("abc \uFFF0 \uD834\uDD73 e123.");
		assertStringD("abc \\u{FFF0} \\U{1D173} e123.");
	}
	
	@Test
	public void printPrintableExt() {
		this.formatter.clear();
		this.formatter.appendStringD("\uAAAA");
		assertStringD("\uAAAA");
		
		this.formatter.clear();
		this.formatter.appendStringD("\uD834\uDD1E");
		assertStringD("\uD834\uDD1E");
		
		this.formatter.clear();
		this.formatter.appendStringD("abc \uAAAA \uD834\uDD1E e123.");
		assertStringD("abc \uAAAA \uD834\uDD1E e123.");
	}
	
}
