/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.LinkedPosition;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.text.ui.BracketLevel;


public final class RBracketLevel extends BracketLevel {
	
	
	public static final class CurlyBracketPosition extends InBracketPosition {
		
		public CurlyBracketPosition(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '{';
		}
		
		@Override
		public char getCloseChar() {
			return '}';
		}
		
	}
	
	public static final class RoundBracketPosition extends InBracketPosition {
		
		public RoundBracketPosition(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '(';
		}
		
		@Override
		public char getCloseChar() {
			return ')';
		}
		
	}
	
	public static final class SquareBracketPosition extends InBracketPosition {
		
		public SquareBracketPosition(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '[';
		}
		
		@Override
		public char getCloseChar() {
			return ']';
		}
		
	}
	
	
	public final static class StringDPosition extends InBracketPosition {
		
		public StringDPosition(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '"';
		}
		
		@Override
		public char getCloseChar() {
			return '"';
		}
		
		@Override
		public boolean insertCR(final int charOffset) {
			return false;
		}
		
		@Override
		protected boolean isEscaped(final int offset) throws BadLocationException {
			return (TextUtil.countBackward(getDocument(), offset, '\\') % 2 == 1);
		}
		
	}
	
	public final static class StringSPosition extends InBracketPosition {
		
		public StringSPosition(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '\'';
		}
		
		@Override
		public char getCloseChar() {
			return '\'';
		}
		
		@Override
		public boolean insertCR(final int charOffset) {
			return false;
		}
		
		@Override
		protected boolean isEscaped(final int offset) throws BadLocationException {
			return (TextUtil.countBackward(getDocument(), offset, '\\') % 2 == 1);
		}
		
	}
	
	public final static class QuotedPosition extends InBracketPosition {
		
		public QuotedPosition(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '`';
		}
		
		@Override
		public char getCloseChar() {
			return '`';
		}
		
		@Override
		public boolean insertCR(final int charOffset) {
			return false;
		}
		
		@Override
		protected boolean isEscaped(final int offset) throws BadLocationException {
			return (TextUtil.countBackward(getDocument(), offset, '\\') % 2 == 1);
		}
		
	}
	
	public final static class InfixLevel extends InBracketPosition {
		
		public InfixLevel(final IDocument doc, final int offset, final int length,
				final int sequence) {
			super(doc, offset, length, sequence);
		}
		
		@Override
		public char getOpenChar() {
			return '%';
		}
		
		@Override
		public char getCloseChar() {
			return '%';
		}
		
		@Override
		public boolean insertCR(final int charOffset) {
			return false;
		}
		
		@Override
		public boolean matchesClose(final BracketLevel level, final int offset, final char character) {
			return (getOffset() + getLength() == offset && getCloseChar() == character);
		}
		
	}
	
	
	public static final InBracketPosition createPosition(final char c,
			final IDocument document, final int offset, final int length, final int sequence) {
		switch (c) {
		case '"':
			return new StringDPosition(document, offset, length, sequence);
		case '\'':
			return new StringSPosition(document, offset, length, sequence);
		case '`':
			return new QuotedPosition(document, offset, length, sequence);
		case '{':
			return new CurlyBracketPosition(document, offset, length, sequence);
		case '(':
			return new RoundBracketPosition(document, offset, length, sequence);
		case '[':
			return new SquareBracketPosition(document, offset, length, sequence);
		case '%':
			return new InfixLevel(document, offset, length, sequence);
		}
		throw new IllegalArgumentException();
	}
	
	
	public RBracketLevel(final IDocument document, final String partitioning, final InBracketPosition position,
			final boolean consoleMode, final boolean autoDelete) {
		this(document, partitioning, ImCollections.<LinkedPosition>newList(position),
				((consoleMode) ? CONSOLE_MODE : 0) | ((autoDelete) ? AUTODELETE : 0));
	}
	
	public RBracketLevel(final IDocument document, final String partitioning, final List<LinkedPosition> positions,
			final int mode) {
		super(document, partitioning, positions, mode);
	}
	
}
