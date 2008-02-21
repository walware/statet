/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.sweave.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.sweave.Rweave;


/**
 * 
 */
public class RweaveChunkHeuristicScanner extends RHeuristicTokenScanner {
	
	
	public RweaveChunkHeuristicScanner() {
		super(Rweave.R_TEX_PARTITIONING);
	}
	
	@Override
	protected int createForwardBound(final int start) throws BadLocationException {
		final PartitionMatcher matcher = getPartitionMatcher();
		if (matcher.matches(IDocument.DEFAULT_CONTENT_TYPE)) {
			return UNBOUND;
		}
		if (matcher.matches(Rweave.TEX_DEFAULT_CONTENT_TYPE)) {
			final ITypedRegion cat = Rweave.R_TEX_CAT_UTIL.getCat(fDocument, start);
			return cat.getOffset()+cat.getLength();
		}
		final ITypedRegion partition = TextUtilities.getPartition(fDocument, getPartitioning(), start, false);
		return partition.getOffset()+partition.getLength();
	}
	
	@Override
	protected int createBackwardBound(final int start) throws BadLocationException {
		final PartitionMatcher matcher = getPartitionMatcher();
		if (matcher.matches(Rweave.TEX_DEFAULT_CONTENT_TYPE)) {
			return -1;
		}
		if (matcher.matches(Rweave.R_DEFAULT_CONTENT_TYPE)) {
			final ITypedRegion cat = Rweave.R_TEX_CAT_UTIL.getCat(fDocument, start);
			return cat.getOffset();
		}
		final ITypedRegion partition = TextUtilities.getPartition(fDocument, getPartitioning(), start, false);
		return partition.getOffset();
	}
	
}
