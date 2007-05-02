/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.ui.IRDocumentPartitions;


/**
 * Double click strategy aware of R identifier syntax rules, Comments and String (all in one).
 * <p>
 * Select content inside matching brackets or, if matching pairs found,
 * a single identifier. 
 */
public class RDoubleClickStrategy implements ITextDoubleClickStrategy {

	private static final String PARTITIONING = IRDocumentPartitions.R_DOCUMENT_PARTITIONING;

	
	private PairMatcher fPairMatcher;
	private RHeuristicTokenScanner fScanner;
	
	
	public RDoubleClickStrategy() {
		super();
		fScanner = new RHeuristicTokenScanner();
		fPairMatcher = new RBracketPairMatcher(fScanner);
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
			if (IRDocumentPartitions.R_DEFAULT.equals(type)) {
				IRegion region = fPairMatcher.match(document, offset);
				if (region != null && region.getLength() >= 2) {
					textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
					return;
				}
			}

			// For other partitions, use prefere new partitions (instead opend)
			partition = TextUtilities.getPartition(document, PARTITIONING, offset, false);
			type = partition.getType();
			// Start or End in String-Partitions
			if (IRDocumentPartitions.R_STRING.equals(type)) {
				int partitionOffset = partition.getOffset();
				int partitionEnd = partitionOffset + partition.getLength();
				if (offset == partitionOffset || offset == partitionOffset+1
						|| offset == partitionEnd || offset == partitionEnd-1) {
					textViewer.setSelectedRange(partitionOffset + 1, partition.getLength() - 2);
				} else {
					fScanner.configure(document, null);
					IRegion region = fScanner.findCommonWord(offset);
					if (region != null) {
						textViewer.setSelectedRange(region.getOffset(), region.getLength());
					}
					else {
						textViewer.setSelectedRange(offset, 0);
					}
				}
				return;
			}
			// Start in Comment-Partitions
			if (IRDocumentPartitions.R_COMMENT.equals(type)) {
				int partitionOffset = partition.getOffset();
				if (offset == partitionOffset || offset == partitionOffset+1) {
					textViewer.setSelectedRange(partitionOffset, partition.getLength());
					return;
				}
			}
			if (IRDocumentPartitions.R_INFIX_OPERATOR.equals(type)) {
				textViewer.setSelectedRange(partition.getOffset(), partition.getLength());
				return;
			}
			// Spezialfall: End String-Partition 
			if (partition.getOffset() == offset && offset > 0 
					&& IRDocumentPartitions.R_STRING.equals(
							(partition = TextUtilities.getPartition(document, PARTITIONING, offset-1, true)).getType()
					)) {
				textViewer.setSelectedRange(partition.getOffset() + 1, partition.getLength() - 2);
				return;
			}

			fScanner.configure(document, null);
			IRegion region = fScanner.findRWord(offset, true);
			if (region != null) {
				textViewer.setSelectedRange(region.getOffset(), region.getLength());
				return;
			}
			region = fScanner.findHorizontalWhitespace(offset);
			if (region != null) {
				textViewer.setSelectedRange(region.getOffset(), region.getLength());
				return;
			}
		} catch (BadLocationException e) {
		} catch (NullPointerException e) {
		}
		// else
		textViewer.setSelectedRange(offset, 0);
	}
	
}
