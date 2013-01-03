/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import de.walware.ecommons.text.ICharPairMatcher;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;


/**
 * Double click strategy aware of R identifier syntax rules, Comments and String (all in one).
 * <p>
 * Select content inside matching brackets or, if matching pairs found,
 * a single identifier.
 */
public class RDoubleClickStrategy implements ITextDoubleClickStrategy {
	
	
	private final String fPartitioning;
	private final ICharPairMatcher fPairMatcher;
	private final RHeuristicTokenScanner fScanner;
	
	
	public RDoubleClickStrategy() {
		super();
		fScanner = new RHeuristicTokenScanner();
		fPartitioning = IRDocumentPartitions.R_PARTITIONING;
		fPairMatcher = new RBracketPairMatcher(fScanner);
	}
	
	public RDoubleClickStrategy(final RHeuristicTokenScanner scanner, final ICharPairMatcher pairMatcher) {
		super();
		fScanner = scanner;
		fPartitioning = scanner.getPartitioningConfig().getPartitioning();
		fPairMatcher = pairMatcher;
	}
	
	
	@Override
	public void doubleClicked(final ITextViewer textViewer) {
		
		final int offset = textViewer.getSelectedRange().x;
		
		if (offset < 0) {
			return;
		}
		
		final IDocument document = textViewer.getDocument();
		try {
			ITypedRegion partition = TextUtilities.getPartition(document, fPartitioning, offset, true);
			String type = partition.getType();
			
			// Bracket-Pair-Matching in Code-Partitions
			if (type == IRDocumentPartitions.R_DEFAULT || type == IRDocumentPartitions.R_DEFAULT_EXPL) {
				final IRegion region = fPairMatcher.match(document, offset);
				if (region != null && region.getLength() >= 2) {
					textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
					return;
				}
			}
			
			// For other partitions, use prefere new partitions (instead opened)
			partition = TextUtilities.getPartition(document, fPartitioning, offset, false);
			type = partition.getType();
			// Start or End in String-Partitions
			if (type == IRDocumentPartitions.R_STRING || type == IRDocumentPartitions.R_QUOTED_SYMBOL) {
				final int partitionOffset = partition.getOffset();
				final int partitionEnd = partitionOffset + partition.getLength();
				if (offset == partitionOffset || offset == partitionOffset+1
						|| offset == partitionEnd || offset == partitionEnd-1) {
					selectRegion(textViewer, getStringContent(document, partition));
				} else {
					fScanner.configure(document);
					final IRegion region = fScanner.findCommonWord(offset);
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
			if (type == IRDocumentPartitions.R_COMMENT || type == IRDocumentPartitions.R_ROXYGEN) {
				final int partitionOffset = partition.getOffset();
				if (offset == partitionOffset || offset == partitionOffset+1) {
					textViewer.setSelectedRange(partitionOffset, partition.getLength());
					return;
				}
			}
			if (type == IRDocumentPartitions.R_INFIX_OPERATOR) {
				textViewer.setSelectedRange(partition.getOffset(), partition.getLength());
				return;
			}
			// Spezialfall: End String-Partition
			if ((partition.getOffset() == offset) && (offset > 0)
					&& ( (partition = TextUtilities.getPartition(document, fPartitioning, offset-1, true))
							.getType() == IRDocumentPartitions.R_STRING)
					) {
				selectRegion(textViewer, getStringContent(document, partition));
				return;
			}
			
			fScanner.configure(document);
			IRegion region = fScanner.findRWord(offset, true, false);
			if (region != null) {
				textViewer.setSelectedRange(region.getOffset(), region.getLength());
				return;
			}
			region = fScanner.findBlankRegion(offset, false);
			if (region != null) {
				textViewer.setSelectedRange(region.getOffset(), region.getLength());
				return;
			}
		} catch (final BadLocationException e) {
		} catch (final NullPointerException e) {
		}
		// else
		textViewer.setSelectedRange(offset, 0);
	}
	
	private final void selectRegion(final ITextViewer viewer, final IRegion region) {
		viewer.setSelectedRange(region.getOffset(), region.getLength());
	}
	
	private final IRegion getStringContent(final IDocument document, final ITypedRegion partition) throws BadLocationException {
		final int partitionOffset = partition.getOffset();
		final int partitionLength = partition.getLength();
		if (partitionLength <= 1) {
			return new Region(partitionOffset+1, 0);
		}
		final char c = document.getChar(partitionOffset);
		document.getLength();
		if (document.getChar(partitionOffset+partitionLength-1) != c) {
			return new Region(partitionOffset+1, partitionLength-1);
		}
		return new Region(partitionOffset+1, partitionLength-2);
	}
	
}
