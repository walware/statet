/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;

import de.walware.ecommons.text.CharacterScannerReader;
import de.walware.ecommons.text.core.rules.BufferedDocumentScanner;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNode;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNodeScan;
import de.walware.ecommons.text.core.treepartitioner.ITreePartitionNodeScanner;


/**
 * This scanner recognizes the comments, platform specif., verbatim-like section
 * (and other/usual Rd code).
 */
public class RPartitionNodeScanner implements ITreePartitionNodeScanner {
	
	
	/**
	 * Enum of states of the scanner.
	 * Note: id is index in array of tokens
	 * 0-7 are reserved.
	 **/
	protected static final int S_DEFAULT= 0;
	protected static final int S_QUOTED_SYMBOL= 1;
	protected static final int S_INFIX_OPERATOR= 2;
	protected static final int S_STRING_S= 3;
	protected static final int S_STRING_D= 4;
	protected static final int S_COMMENT= 5;
	protected static final int S_ROXYGEN= 6;
	
	
	/** Enum of last significant characters read. */
	protected static final byte LAST_OTHER= 0;
	protected static final byte LAST_EOF= 1;
	protected static final byte LAST_NEWLINE= 2;
	
	
	private final boolean isRoxygenEnabled;
	
	protected final CharacterScannerReader reader= new CharacterScannerReader(
			new BufferedDocumentScanner(1024) );
	
	private ITreePartitionNodeScan scan;
	
	/** The current node */
	private ITreePartitionNode node;
	/** The current node type */
	private RPartitionNodeType type;
	/** The last significant characters read. */
	protected byte last;
	
	
	public RPartitionNodeScanner() {
		this.isRoxygenEnabled= true;
	}
	
	
	@Override
	public int getRestartOffset(final ITreePartitionNode node, final IDocument document,
			final int offset) throws BadLocationException {
		return offset;
	}
	
	@Override
	public RPartitionNodeType getRootType() {
		return RPartitionNodeType.DEFAULT_ROOT;
	}
	
	@Override
	public void execute(final ITreePartitionNodeScan scan) {
		this.scan= scan;
		
		setRange(scan.getBeginOffset(), scan.getEndOffset());
		
		this.node= null;
		
		init();
		
		process();
	}
	
	protected ITreePartitionNodeScan getScan() {
		return this.scan;
	}
	
	protected void setRange(final int beginOffset, final int endOffset) {
		this.reader.setRange(getScan().getDocument(), beginOffset, endOffset - beginOffset);
		updateLast();
	}
	
	protected void init() {
		final ITreePartitionNode beginNode= getScan().getBeginNode();
		if (beginNode.getType() instanceof RPartitionNodeType) {
			this.node= beginNode;
			this.type= (RPartitionNodeType) beginNode.getType();
		}
		else {
			this.node= beginNode;
			addNode(getRootType(), getScan().getBeginOffset());
		}
	}
	
	private void updateLast() {
		if (this.reader.getOffset() > 0) {
			this.last= LAST_OTHER;
			try {
				final char c= getScan().getDocument().getChar(this.reader.getOffset() - 1);
				switch (c) {
				case '\r':
				case '\n':
					this.last= LAST_NEWLINE;
					break;
				default:
					break;
				}
			}
			catch (final BadLocationException e) {}
		}
		else {
			this.last= LAST_NEWLINE;
		}
	}
	
	
	protected final void addNode(final RPartitionNodeType type, final int offset) {
		this.node= this.scan.add(type, this.node, offset);
		this.type= type;
	}
	
	protected final ITreePartitionNode getNode() {
		return this.node;
	}
	
	protected final void exitNode(final int offset) {
		this.scan.expand(this.node, offset, true);
		this.node= this.node.getParent();
		this.type= (RPartitionNodeType) this.node.getType();
	}
	
	protected final void exitNode() {
		this.node= this.node.getParent();
		this.type= (RPartitionNodeType) this.node.getType();
	}
	
	
	private void process() {
		while (true) {
			switch (this.last) {
			case LAST_EOF:
				handleEOF(this.type);
				this.scan.expand(this.node, this.reader.getOffset(), true);
				return;
			case LAST_NEWLINE:
				handleNewLine(this.type);
				break;
			default:
				break;
			}
			
			switch (this.type.getScannerState()) {
			case S_DEFAULT:
				processDefault();
				continue;
			case S_QUOTED_SYMBOL:
				processQuotedSymbol();
				continue;
			case S_INFIX_OPERATOR:
				processInfixOperator();
				continue;
			case S_STRING_S:
				processStringS();
				continue;
			case S_STRING_D:
				processStringD();
				continue;
			case S_COMMENT:
			case S_ROXYGEN:
				processComment();
				continue;
			default:
				processExt(this.type);
				continue;
			}
		}
	}
	
	protected void processDefault() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				this.last= LAST_NEWLINE;
				return;
			case '"':
				addNode(RPartitionNodeType.STRING_D, this.reader.getOffset() - 1);
				this.last= LAST_OTHER;
				return;
			case '\'':
				addNode(RPartitionNodeType.STRING_S, this.reader.getOffset() - 1);
				this.last= LAST_OTHER;
				return;
			case '`':
				addNode(RPartitionNodeType.QUOTED_SYMBOL, this.reader.getOffset() - 1);
				this.last= LAST_OTHER;
				return;
			case '#':
				if (this.isRoxygenEnabled && this.reader.read('\'')) {
					addNode(RPartitionNodeType.ROXYGEN, this.reader.getOffset() - 2);
					this.last= LAST_OTHER;
					return;
				}
				else {
					addNode(RPartitionNodeType.COMMENT, this.reader.getOffset() - 1);
					this.last= LAST_OTHER;
					return;
				}
			case '%':
				addNode(RPartitionNodeType.INFIX_OPERATOR, this.reader.getOffset() - 1);
				this.last= LAST_OTHER;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	protected void processInfixOperator() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				exitNode(this.reader.getOffset() - 1);
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				exitNode(this.reader.getOffset() - 1);
				this.last= LAST_NEWLINE;
				return;
			case '%':
				exitNode(this.reader.getOffset());
				this.last= LAST_OTHER;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	private void processBackslash() {
		this.last= LAST_OTHER;
		switch (this.reader.read()) {
		case ICharacterScanner.EOF:
			return;
		case '\r':
		case '\n':
			this.reader.unread();
			return;
		default:
			return;
		}
	}
	
	protected void processQuotedSymbol() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				this.last= LAST_NEWLINE;
				return;
			case '\\':
				processBackslash();
				continue;
			case '`':
				exitNode(this.reader.getOffset());
				this.last= LAST_OTHER;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	protected void processStringD() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				this.last= LAST_NEWLINE;
				return;
			case '\\':
				processBackslash();
				continue;
			case '\"':
				exitNode(this.reader.getOffset());
				this.last= LAST_OTHER;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	protected void processStringS() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				this.last= LAST_NEWLINE;
				return;
			case '\\':
				processBackslash();
				continue;
			case '\'':
				exitNode(this.reader.getOffset());
				this.last= LAST_OTHER;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	protected void processComment() {
		LOOP: while (true) {
			switch (this.reader.read()) {
			case ICharacterScanner.EOF:
				this.last= LAST_EOF;
				return;
			case '\r':
				exitNode(this.reader.getOffset() - 1);
				this.reader.read('\n');
				this.last= LAST_NEWLINE;
				return;
			case '\n':
				exitNode(this.reader.getOffset() - 1);
				this.last= LAST_NEWLINE;
				return;
			default:
				continue LOOP;
			}
		}
	}
	
	protected void processExt(final RPartitionNodeType type) {
		throw new IllegalStateException("state= " + type.getScannerState());
	}
	
	protected void handleNewLine(final RPartitionNodeType type) {
	}
	
	protected void handleEOF(final RPartitionNodeType type) {
	}
	
}
