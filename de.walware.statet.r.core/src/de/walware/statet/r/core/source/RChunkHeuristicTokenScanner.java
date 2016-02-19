/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.source;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import de.walware.ecommons.text.core.IPartitionConstraint;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.core.treepartitioner.TreePartitionUtil;


public class RChunkHeuristicTokenScanner extends RHeuristicTokenScanner {
	
	
	public RChunkHeuristicTokenScanner(final IDocContentSections documentContentInfo) {
		super(documentContentInfo);
	}
	
	
	@Override
	protected int createForwardBound(final int start) throws BadLocationException {
		final IPartitionConstraint matcher= getPartitionConstraint();
		if (matcher.matches(IRDocumentConstants.R_DEFAULT_CONTENT_TYPE)) {
			final IRegion rCodeRegion= getRCodeRegion(start);
			if (rCodeRegion != null) {
				return rCodeRegion.getOffset() + rCodeRegion.getLength();
			}
		}
		final ITypedRegion partition= TextUtilities.getPartition(this.fDocument, getDocumentPartitioning(), start, false);
		return partition.getOffset() + partition.getLength();
	}
	
	@Override
	protected int createBackwardBound(final int start) throws BadLocationException {
		final IPartitionConstraint matcher= getPartitionConstraint();
		if (matcher.matches(IRDocumentConstants.R_DEFAULT_CONTENT_TYPE)) {
			final IRegion rCodeRegion= getRCodeRegion(start);
			if (rCodeRegion != null) {
				return rCodeRegion.getOffset();
			}
		}
		final ITypedRegion partition= TextUtilities.getPartition(this.fDocument, getDocumentPartitioning(), start, false);
		return partition.getOffset();
	}
	
	private IRegion getRCodeRegion(final int offset) throws BadLocationException {
		return TreePartitionUtil.searchNode(this.fDocument, getDocumentPartitioning(), offset, true,
				IRDocumentConstants.R_DEFAULT_CONTENT_TYPE );
	}
	
}
