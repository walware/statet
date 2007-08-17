/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.rd;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * Double click strategy aware of R identifier syntax rules, Comments and String (all in one).
 * <p>
 * Select content inside matching brackets or, if matching pairs found,
 * a single identifier. 
 */
public class RdDoubleClickStrategy implements ITextDoubleClickStrategy {

	private static final String PARTITIONING = IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING;
	private static final char[][] BRACKETS = { {'{', '}'} };
	
	protected PairMatcher fPairMatcher;
	
	public RdDoubleClickStrategy() {
		super();
		fPairMatcher = new PairMatcher(BRACKETS, 
				PARTITIONING, IRDocumentPartitions.RDOC_DEFAULT,
				'\\');
	}
	
	/**
	 * @see ITextDoubleClickStrategy#doubleClicked
	 */
	public void doubleClicked(ITextViewer textViewer) {
		
		int offset = textViewer.getSelectedRange().x;
		
		if (offset < 0)
			return;
		
		IDocument document = textViewer.getDocument();
		try {
			ITypedRegion partition = TextUtilities.getPartition(document, PARTITIONING, offset, true);
			String type = partition.getType();
			
			// Bracket-Pair-Matching in Code-Partitions
			if (IRDocumentPartitions.RDOC_DEFAULT.equals(type)) {
				IRegion region = fPairMatcher.match(document, offset);
				if (region != null && region.getLength() >= 2) {
					textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
					return;
				}
			}

			// For other partitions, use prefere new partitions (instead opend)
			partition = TextUtilities.getPartition(document, PARTITIONING, offset, false);
			type = partition.getType();
			// Start in Comment-Partitions
			if (IRDocumentPartitions.RDOC_COMMENT.equals(type) || IRDocumentPartitions.RDOC_PLATFORM_SPECIF.equals(type)) {
				int partitionOffset = partition.getOffset();
				if (offset == partitionOffset || offset == partitionOffset+1) {
					textViewer.setSelectedRange(partitionOffset, partition.getLength());
					return;
				}
			}
		} catch (BadLocationException e) {
		} catch (NullPointerException e) {
		}
		// else
		IRegion region = getDefaultWordSelection(document, offset);
		textViewer.setSelectedRange(region.getOffset(), region.getLength());
	}

	protected IRegion getDefaultWordSelection(IDocument document, int anchor) {
		
		try {
	
			int offset = anchor;
			char c;
	
			while (offset >= 0) {
				c = document.getChar(offset);
				if (!Character.isLetterOrDigit(c))
					break;
				--offset;
			}
	
			int start = offset;
	
			offset = anchor;
			int length = document.getLength();
	
			while (offset < length) {
				c = document.getChar(offset);
				if (!Character.isLetterOrDigit(c))
					break;
				++offset;
			}
			
			int end = offset;
			
			if (start < end)
				return new Region (start + 1, end - start - 1);
			
		} catch (BadLocationException x) {
		}
		return new Region(anchor, 0);
	}
	
}
