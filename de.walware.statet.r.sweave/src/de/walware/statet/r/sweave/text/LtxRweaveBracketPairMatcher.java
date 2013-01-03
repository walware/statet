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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.text.ICharPairMatcher;

import de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner;
import de.walware.docmlet.tex.ui.text.LtxBracketPairMatcher;

import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


public class LtxRweaveBracketPairMatcher implements ICharPairMatcher {
	
	
	public static RBracketPairMatcher createRChunkPairMatcher(final RweaveChunkHeuristicScanner scanner) {
		return new RBracketPairMatcher(
				scanner, Rweave.LTX_R_PARTITIONING, new String[] {
					Rweave.R_DEFAULT_CONTENT_TYPE,
					Rweave.CHUNK_CONTROL_CONTENT_TYPE
				} );
	}
	
	
	private final LtxBracketPairMatcher fTexPairMatcher;
	private final RBracketPairMatcher fRPairMatcher;
	
	private int fAnchor;
	
	
	public LtxRweaveBracketPairMatcher() {
		fTexPairMatcher = new LtxBracketPairMatcher(
				new LtxHeuristicTokenScanner(Rweave.LTX_PARTITIONING_CONFIG) );
		fRPairMatcher = createRChunkPairMatcher(new RweaveChunkHeuristicScanner());
	}
	
	
	@Override
	public void dispose() {
		fTexPairMatcher.dispose();
		fRPairMatcher.dispose();
	}
	
	@Override
	public void clear() {
		fTexPairMatcher.clear();
		fRPairMatcher.clear();
	}
	
	@Override
	public IRegion match(final IDocument document, final int offset) {
		fAnchor = -1;
		final ICharPairMatcher matcher =
				(Rweave.R_TEX_CAT_UTIL.getCat(document, offset).getType() == Rweave.TEX_CAT) ?
						fTexPairMatcher : fRPairMatcher;
		final IRegion region = matcher.match(document, offset);
		fAnchor = matcher.getAnchor();
		return region;
	}
	
	@Override
	public IRegion match(final IDocument document, final int offset, final boolean auto) {
		fAnchor = -1;
		final ICharPairMatcher matcher =
				(Rweave.R_TEX_CAT_UTIL.getCat(document, offset).getType() == Rweave.TEX_CAT) ?
						fTexPairMatcher : fRPairMatcher;
		final IRegion region = matcher.match(document, offset, auto);
		fAnchor = matcher.getAnchor();
		return region;
	}
	
	@Override
	public int getAnchor() {
		return fAnchor;
	}
	
}
