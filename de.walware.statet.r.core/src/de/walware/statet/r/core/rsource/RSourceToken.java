/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource;

import de.walware.statet.r.core.rlang.RTerminal;


public abstract class RSourceToken {
	
	
	public static RSourceToken createFix(final RTerminal type, final int start, final int status) {
		final FixToken token = new FixToken(type, start, type.text.length(), status);
		return token;
	}
	
	public static RSourceToken create(final RTerminal type, final int start, final String text, final int status) {
		final IndToken token = new IndToken(type, start, text.length(), status);
		token.fText = text;
		return token;
	}
	
	public static RSourceToken create(final RTerminal type, final int start, final int length, final CharSequence source, final int status) {
		final IndToken token = new IndToken(type, start, length, status);
		token.fInput = source;
		return token;
	}
	
	public static class IndToken extends RSourceToken {
		
		private String fText;
		private CharSequence fInput;
		
		public IndToken(final RTerminal type, final int start, final int length, final int status) {
			super(type, start, length, status);
		}
		
		@Override
		public String getText() {
			if (fText == null && fInput != null) {
				fText = fInput.subSequence(fStart, fLength).toString();
			}
			return fText;
		}
		
	}
	
	public static class FixToken extends RSourceToken {
		
		public FixToken(final RTerminal type, final int start, final int length, final int status) {
			super(type, start, length, status);
		}
		
		@Override
		public final String getText() {
			return fType.text;
		}
		
	}
	
	
	protected final RTerminal fType;
	protected int fStart;
	protected int fLength;
	protected int fStatus;
	
	
	protected RSourceToken(final RTerminal type, final int start, final int length, final int status) {
		fType = type;
		fStart = start;
		fLength = length;
		fStatus = status;
	}
	
	public final RTerminal getTokenType() {
		return fType;
	}
	
	public final int getOffset() {
		return fStart;
	}
	
	public final int getLength() {
		return fLength;
	}
	
	public final int getStatusCode() {
		return fStatus;
	}
	
	public abstract String getText();
	
}
