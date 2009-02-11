/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import net.sourceforge.texlipse.editor.TexFastPartitionScanner;

import de.walware.statet.r.internal.sweave.Rweave;


/**
 * Paritition scanner for TeX chunks (stops if find '&lt;&lt;' at column 0).
 */
public class TexChunkPartitionScanner extends TexFastPartitionScanner
		implements ICatPartitionTokenScanner {
	
	
	private boolean fIsInChunk;
	
	
	public TexChunkPartitionScanner() {
		super();
	}
	
	public TexChunkPartitionScanner(final boolean templateMode) {
		super(templateMode);
	}
	
	
	@Override
	public void setPartialRange(final IDocument document, final int offset, final int length, final String contentType, final int partitionOffset) {
		fIsInChunk = true;
		super.setPartialRange(document, offset, length, contentType, partitionOffset);
	}
	
	@Override
	protected void handleChar(final int state, final int c) {
		if (c == '<' && fLast == LAST_NEWLINE) {
			if (readChar('<')) {
				fIsInChunk = false;
				forceReturn(S_DEFAULT, 2);
				return;
			}
		}
		super.handleChar(state, c);
	}
	
	public void setParent(final MultiCatPartitionScanner parent) {
	}
	
	public String[] getContentTypes() {
		return Rweave.TEX_PARTITION_TYPES;
	}
	
	public boolean isInCat() {
		return fIsInChunk;
	}
	
}
