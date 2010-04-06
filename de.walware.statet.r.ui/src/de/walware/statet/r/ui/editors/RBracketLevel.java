/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.LinkedPosition;

import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.text.ui.BracketLevel;
import de.walware.ecommons.text.ui.BracketLevel.DefaultBracketLevel;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;


public final class RBracketLevel {
	
	public static final class CurlyBracketLevel extends DefaultBracketLevel {
		
		public CurlyBracketLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		protected int getCloseChar() {
			return '}';
		}
		
	}
	
	public static final class RoundBracketLevel extends DefaultBracketLevel {
		
		public RoundBracketLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		protected int getCloseChar() {
			return ')';
		}
		
	}
	
	public static final class SquareBracketLevel extends DefaultBracketLevel {
		
		public SquareBracketLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		protected int getCloseChar() {
			return ']';
		}
		
	}
	
	private static abstract class AbstractStringLevel extends BracketLevel {
		
		public AbstractStringLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		protected abstract int getSpeparatorChar();
		
		protected abstract String getExpectedPartitionType();
		
		@Override
		public boolean insertCR(final int charOffset) {
			return false;
		}
		
		@Override
		public boolean matchesEnd(final char c, int charOffset) throws BadLocationException {
			if (c == getSpeparatorChar() && getPartitionType(charOffset) == getExpectedPartitionType()) {
				int count = -1;
				do {
					count++;
					charOffset--;
				} while (fDocument.getChar(charOffset) == '\\');
				return ((count % 2) == 0);
			}
			return false;
		}
		
	}
	
	public final static class StringDLevel extends AbstractStringLevel {
		
		public StringDLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		protected int getSpeparatorChar() {
			return '"';
		}
		
		@Override
		protected String getExpectedPartitionType() {
			return IRDocumentPartitions.R_STRING;
		}
		
	}
	
	public final static class StringSLevel extends AbstractStringLevel {
		
		public StringSLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		protected int getSpeparatorChar() {
			return '\'';
		}
		
		@Override
		protected String getExpectedPartitionType() {
			return IRDocumentPartitions.R_STRING;
		}
		
	}
	
	public final static class QuotedLevel extends AbstractStringLevel {
		
		public QuotedLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		protected int getSpeparatorChar() {
			return '`';
		}
		
		@Override
		protected String getExpectedPartitionType() {
			return IRDocumentPartitions.R_QUOTED_SYMBOL;
		}
		
	}
	
	public final static class InfixLevel extends BracketLevel {
		
		public InfixLevel(final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
			super(doc, partitioning, position, consoleMode);
		}
		
		@Override
		public boolean insertCR(final int charOffset) {
			return false;
		}
		
		@Override
		public boolean matchesEnd(final char c, final int charOffset) throws BadLocationException {
			return (c == '%');
		}
		
	}
	
	
	public static final BracketLevel createBracketLevel(final char c,
			final IDocument doc, final PartitioningConfiguration partitioning, final LinkedPosition position, final boolean consoleMode) {
		switch (c) {
		case '"':
			return new StringDLevel(doc, partitioning, position, consoleMode);
		case '\'':
			return new StringSLevel(doc, partitioning, position, consoleMode);
		case '`':
			return new QuotedLevel(doc, partitioning, position, consoleMode);
		case '{':
			return new CurlyBracketLevel(doc, partitioning, position, consoleMode);
		case '(':
			return new RoundBracketLevel(doc, partitioning, position, consoleMode);
		case '[':
			return new SquareBracketLevel(doc, partitioning, position, consoleMode);
		case '%':
			return new InfixLevel(doc, partitioning, position, consoleMode);
		}
		throw new IllegalArgumentException();
	}
	
}
