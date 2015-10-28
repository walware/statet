/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.console;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.internal.ui.console.NIConsolePartitioner.PendingPartition;


/**
 * Console stream processor handles control chars BEL, BS, LF, VT, FF and CR.
 */
final class StreamProcessor {
	
	
	private static final char BEL=      0x07;
	private static final char BS=       0x08;
	private static final char LF=       0x0A;
	private static final char VT=       0x0B;
	private static final char FF=       0x0C;
	private static final char CR=       0x0D;
	
	private static final int CHAR_BUFFER_SIZE= 0x2000;
	
	private static final byte S_CREATED=                    0b0_0000_0001;
	private static final byte S_APPLIED=                    0b0_0000_0010;
	private static final byte S_DONE=                       0b0_0000_0100;
	
	
	private final NIConsolePartitioner partitioner;
	
	private byte state;
	
	private String docLF;
	private String docVT;
	
	private boolean finish;
	
	private int docLength;
	
	private final StringBuilder text= new StringBuilder(CHAR_BUFFER_SIZE);
	private int textOffsetInDoc;
	private int lineStartInText;
	
	private int lastPartitionInsertGap;
	
	private final char[] charBuffer= new char[CHAR_BUFFER_SIZE];
	
	
	public StreamProcessor(final NIConsolePartitioner partitioner) {
		this.partitioner= partitioner;
	}
	
	
	public void prepareUpdate(final ImList<PendingPartition> pendingPartitions,
			final int pendingLength) {
		this.text.setLength(0);
		
		if ((this.state & S_APPLIED) == 0) { // not applied
			this.lastPartitionInsertGap= 0;
		}
		clear();
		
		final AbstractDocument document= this.partitioner.getDocument();
		if (document != null) { // connected
			this.docLength= document.getLength();
			boolean mayCombineLast= true;
			this.textOffsetInDoc= this.docLength;
			this.lineStartInText= 0;
			this.text.ensureCapacity(pendingLength);
			
			for (final PendingPartition pp : pendingPartitions) {
				if (pp != null) {
					processPartition(pp, mayCombineLast);
					mayCombineLast= false;
				}
				else {
					this.finish= true;
				}
			}
			this.state|= S_CREATED;
		}
		else {
			for (final PendingPartition pp : pendingPartitions) {
				if (pp == null) {
					this.finish= true;
					break;
				}
			}
		}
	}
	
	public void updateApplied() {
		this.state|= S_APPLIED;
	}
	
	public void updateDone() {
		this.state|= S_DONE;
		
		if ((this.finish) ?
				(this.text.capacity() > 2 * CHAR_BUFFER_SIZE) :
				(this.text.capacity() > 0x100000 && this.text.capacity() / 2 > this.text.length()) ) {
			if (this.text.length() < CHAR_BUFFER_SIZE) {
				this.text.append(this.charBuffer, 0, CHAR_BUFFER_SIZE - this.text.length());
			}
			else {
				this.text.setLength(CHAR_BUFFER_SIZE);
			}
			this.text.trimToSize();
		}
	}
	
	public void clear() {
		this.state= 0;
	}
	
	
	/**
	 * Returns the text to insert into the document.
	 * 
	 * @return the text
	 */
	public String getText() {
		return this.text.toString();
	}
	
	/**
	 * Returns the offset for {@link #getText()}.
	 * 
	 * @return offset in the document.
	 */
	public int getTextOffsetInDoc() {
		return this.textOffsetInDoc;
	}
	
	/**
	 * Returns the length of text to be replaced by {@link #getText()}.
	 * 
	 * @return the length in the document.
	 */
	public int getTextReplaceLengthInDoc() {
		return this.docLength - this.textOffsetInDoc;
	}
	
	public boolean wasFinished() {
		return this.finish;
	}
	
	
	private void processPartition(final PendingPartition pp, boolean mayCombineLast) {
		final StringBuilder pText= pp.getText();
		final StringBuilder text= this.text;
		final int pOffset= text.length();
		int insertIdx= pOffset;
		int readIdx= 0, doneIdx= 0;
		int pLineStart= pOffset;
		
		if (mayCombineLast && pLineStart == 0 && this.lastPartitionInsertGap > 0) {
			mayCombineLast= false;
			prependLastDocLine(pp, text);
			insertIdx= Math.max(text.length() - this.lastPartitionInsertGap, pLineStart);
		}
		
		while (readIdx < pText.length()) {
			final char c= pText.charAt(readIdx);
			switch (c) {
			case BEL:
				insertIdx+= copy(pText, doneIdx, readIdx, text, insertIdx);
				
				bell();
				
				readIdx++;
				doneIdx= readIdx;
				continue;
			case BS:
				insertIdx+= copy(pText, doneIdx, readIdx, text, insertIdx);
				
				if (mayCombineLast && pLineStart == 0) {
					mayCombineLast= false;
					insertIdx+= prependLastDocLine(pp, text);
				}
				
				if (insertIdx > pLineStart) {
					insertIdx--;
				}
				readIdx++;
				doneIdx= readIdx;
				continue;
			case LF:
				if (insertIdx < text.length()) {
					copy(pText, doneIdx, readIdx, text, insertIdx);
					
					insertIdx= text.length();
					doneIdx= readIdx;
				}
				
				readIdx++;
				pLineStart= insertIdx + readIdx - doneIdx;
				continue;
			case VT:
				copy(pText, doneIdx, readIdx, text, insertIdx);
				
				if (this.docVT == null) {
					initDocTemplates();
				}
				text.append(this.docVT);
				
				insertIdx= text.length();
				pLineStart= insertIdx;
				readIdx++;
				doneIdx= readIdx;
				continue;
			case FF:
				insertIdx+= copy(pText, doneIdx, readIdx, text, insertIdx);
				
				if (this.docLF == null) {
					initDocTemplates();
				}
				{	int count;
					if (pLineStart == pOffset) {
						count= insertIdx - this.lineStartInText;
						if (this.lineStartInText == 0) {
							count+= getLastDocLineLength();
						}
					}
					else {
						count= insertIdx - pLineStart;
					}
					
					text.append(this.docLF);
					insertIdx= text.length();
					pLineStart= insertIdx;
					insertIdx+= append(' ', count, text);
				}
				
				readIdx++;
				doneIdx= readIdx;
				continue;
			case CR:
				if (readIdx + 1 < pText.length() && pText.charAt(readIdx + 1) == LF) {
					if (insertIdx < text.length()) {
						copy(pText, doneIdx, readIdx, text, insertIdx);
						
						insertIdx= text.length();
						doneIdx= readIdx;
					}
					
					readIdx++;
					continue;
				}
				
				copy(pText, doneIdx, readIdx, text, insertIdx);
				
				if (mayCombineLast && pLineStart == 0) {
					mayCombineLast= false;
					prependLastDocLine(pp, text);
				}
				
				insertIdx= pLineStart;
				readIdx++;
				doneIdx= readIdx;
				continue;
			default:
				readIdx++;
				continue;
			}
		}
		
		this.lineStartInText= pLineStart;
		
		if (doneIdx == 0 && text.length() == pOffset) { // nothing special found
			text.append(pText);
			
			this.lastPartitionInsertGap= 0;
		}
		else {
			insertIdx+= copy(pText, doneIdx, readIdx, text, insertIdx);
			
			// copy back to partition
			pText.setLength(0);
			copy(text, pOffset, text.length(), pText, 0);
			
			this.lastPartitionInsertGap= text.length() - insertIdx;
		}
	}
	
	/**
	 * @return the length of text copied (= srcEnd - srcStart)
	 */
	private int copy(final StringBuilder src, int srcStart, final int srcEnd,
			final StringBuilder dest, final int destIdx) {
		final int length= srcEnd - srcStart;
		if (length == 0) {
			return 0;
		}
		if (destIdx == dest.length()) {
			if (length == 1) {
				dest.append(src.charAt(srcStart));
			}
			else if (length <= 16) {
				dest.append(src, srcStart, srcEnd);
			}
			else {
				for (int n; (n= Math.min(srcEnd - srcStart, CHAR_BUFFER_SIZE)) != 0; srcStart+= n) {
					src.getChars(srcStart, srcStart + n, this.charBuffer, 0);
					dest.append(this.charBuffer, 0, n);
				}
			}
		}
		else {
			if (length == 1) {
				dest.setCharAt(destIdx, src.charAt(srcStart));
			}
			else if (destIdx + length < dest.length()) {
				dest.replace(destIdx, destIdx + length, src.substring(srcStart, srcEnd));
			}
			else {
				dest.setLength(destIdx);
				for (int n; (n= Math.min(srcEnd - srcStart, CHAR_BUFFER_SIZE)) != 0; srcStart+= n) {
					src.getChars(srcStart, srcStart + n, this.charBuffer, 0);
					dest.append(this.charBuffer, 0, n);
				}
			}
		}
		return length;
	}
	
	/**
	 * @return the length of text appended (= count)
	 */
	private int append(final char c, final int count, final StringBuilder dest) {
		for (int i= 0; i < count; i++) {
			dest.append(c);
		}
		return count;
	}
	
	private void bell() {
		final Display display= UIAccess.getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				display.beep();
			};
		});
	}
	
	/**
	 * Prepends the last line (or tail of last line) of the document, if it matches the specified
	 * partition.
	 * 
	 * @return the length of text prepended
	 */
	private int prependLastDocLine(final PendingPartition pp, final StringBuilder dest) {
		final NIConsolePartition lastPartition= this.partitioner.getLastPartition();
		if (lastPartition != null && lastPartition.getStream() == pp.getStream()
				&& lastPartition.getOffset() + lastPartition.getLength() == this.docLength ) {
			try {
				final AbstractDocument document= this.partitioner.getDocument();
				final int start= Math.max(lastPartition.getOffset(),
						document.getLineOffset(document.getNumberOfLines() - 1) );
				final int length= this.docLength - start;
				if (length > 0) {
					dest.insert(0, document.get(start, length));
					this.textOffsetInDoc= start;
					return length;
				}
			}
			catch (final BadLocationException e) {}
		}
		return 0;
	}
	
	/**
	 * Returns the length of the last line of the document independent of its partitions,
	 * except the text already reused ({@link #prependLastDocLine(PendingPartition, StringBuilder)}).
	 * 
	 * @return the length of the last line.
	 */
	private int getLastDocLineLength() {
		try {
			final AbstractDocument document= this.partitioner.getDocument();
			final int start= document.getLineOffset(document.getNumberOfLines() - 1);
			final int length= this.textOffsetInDoc - start;
			if (length > 0) {
				return length;
			}
		}
		catch (final BadLocationException e) {}
		return 0;
	}
	
	private void initDocTemplates() {
		this.docLF= this.partitioner.getConsole().getProcess().getWorkspaceData().getLineSeparator();
		final StringBuilder sb= new StringBuilder();
		sb.append(this.docLF);
		sb.append(this.docLF);
		sb.append(this.docLF);
		sb.append(this.docLF);
		this.docVT= sb.toString();
	}
	
}
