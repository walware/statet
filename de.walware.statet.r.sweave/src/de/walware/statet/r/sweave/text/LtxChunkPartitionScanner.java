/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.docmlet.tex.ui.text.LtxFastPartitionScanner;


/**
 * Paritition scanner for LaTeX chunks (stops if find '&lt;&lt;' at column 0).
 */
public class LtxChunkPartitionScanner extends LtxFastPartitionScanner
		implements ICatPartitionTokenScanner {
	
	
	private boolean fIsInChunk;
	
	
	public LtxChunkPartitionScanner() {
		super(Rweave.LTX_R_PARTITIONING);
	}
	
	public LtxChunkPartitionScanner(final boolean templateMode) {
		super(Rweave.LTX_R_PARTITIONING, templateMode);
	}
	
	
	@Override
	public void setPartialRange(final IDocument document, final int offset, final int length, final String contentType, final int partitionOffset) {
		fIsInChunk = true;
		super.setPartialRange(document, offset, length, contentType, partitionOffset);
	}
	
	@Override
	protected void searchDefault() {
		if (fLast == LAST_NEWLINE) {
			if (readCharsTemp('<', '<')) {
				fIsInChunk = false;
				forceReturn(0);
				return;
			}
		}
		super.searchDefault();
	}
	
	@Override
	protected void searchMathSpecial() {
		if (fLast == LAST_NEWLINE) {
			if (readCharsTemp('<', '<')) {
				fIsInChunk = false;
				forceReturn(0);
				return;
			}
		}
		super.searchMathSpecial();
	}
	
	@Override
	protected void searchMathEnv() {
		if (fLast == LAST_NEWLINE) {
			if (readCharsTemp('<', '<')) {
				fIsInChunk = false;
				forceReturn(0);
				return;
			}
		}
		super.searchMathEnv();
	}
	
	@Override
	public void setParent(final MultiCatPartitionScanner parent) {
	}
	
	@Override
	public String[] getContentTypes() {
		return Rweave.LTX_PARTITION_TYPES;
	}
	
	@Override
	public boolean isInCat() {
		return fIsInChunk;
	}
	
}
