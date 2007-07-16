/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;


/**
 *
 */
public class Partitioner extends FastPartitioner {

	public Partitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}

	
	@Override
	public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
		ITypedRegion region= getPartition(offset);
		if (preferOpenPartitions) {
			if (offset > 0) {
				if (offset == fDocument.getLength()) {
					return getPartition(offset-1);
				}
				try {
					char c = fDocument.getChar(offset);
					if (c == '\n' || c == '\r') {
						return getPartition(offset-1);
					}
				} catch (BadLocationException e) {
				}
			}

			if (region.getOffset() == offset && !region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				if (offset > 0) {
					region= getPartition(offset - 1);
					if (region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE))
						return region;
				}
				return new TypedRegion(offset, 0, IDocument.DEFAULT_CONTENT_TYPE);
			}
		}
        return region;
	}
}
