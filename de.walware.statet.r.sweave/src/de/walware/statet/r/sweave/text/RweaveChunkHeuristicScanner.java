/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.text.IPartitionConstraint;

import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;


/**
 * 
 */
public class RweaveChunkHeuristicScanner extends RHeuristicTokenScanner {
	
	
	public RweaveChunkHeuristicScanner() {
		super(Rweave.R_PARTITIONING_CONFIG);
	}
	
	
	@Override
	protected int createForwardBound(final int start) throws BadLocationException {
		final IPartitionConstraint matcher = getPartitionConstraint();
		assert (!matcher.matches(IDocument.DEFAULT_CONTENT_TYPE));
		if (matcher.matches(Rweave.LTX_DEFAULT_CONTENT_TYPE)) {
			final ITypedRegion cat = Rweave.R_TEX_CAT_UTIL.getCat(fDocument, start);
			return cat.getOffset()+cat.getLength();
		}
		final ITypedRegion partition = TextUtilities.getPartition(fDocument, getPartitioning(), start, false);
		return partition.getOffset()+partition.getLength();
	}
	
	@Override
	protected int createBackwardBound(final int start) throws BadLocationException {
		final IPartitionConstraint matcher = getPartitionConstraint();
		assert (!matcher.matches(IDocument.DEFAULT_CONTENT_TYPE));
		if (matcher.matches(Rweave.R_DEFAULT_CONTENT_TYPE)) {
			final ITypedRegion cat = Rweave.R_TEX_CAT_UTIL.getCat(fDocument, start);
			return cat.getOffset();
		}
		final ITypedRegion partition = TextUtilities.getPartition(fDocument, getPartitioning(), start, false);
		return partition.getOffset();
	}
	
}
