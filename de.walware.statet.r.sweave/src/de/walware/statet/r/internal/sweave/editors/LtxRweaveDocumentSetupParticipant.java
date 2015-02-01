/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.editors;

import de.walware.ecommons.text.Partitioner;
import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;

import de.walware.docmlet.tex.core.source.ITexDocumentConstants;

import de.walware.statet.r.sweave.text.CatPartitioner;
import de.walware.statet.r.sweave.text.LtxChunkPartitionScanner;
import de.walware.statet.r.sweave.text.MultiCatPartitionScanner;
import de.walware.statet.r.sweave.text.Rweave;
import de.walware.statet.r.sweave.text.RweaveChunkPartitionScanner;


/**
 * The document setup participant for Sweave (LaTeX).
 */
public class LtxRweaveDocumentSetupParticipant extends PartitionerDocumentSetupParticipant {
	
	
	private final boolean fTemplateMode;
	
	
	public LtxRweaveDocumentSetupParticipant() {
		this(false);
	}
	
	public LtxRweaveDocumentSetupParticipant(final boolean templateMode) {
		fTemplateMode = templateMode;
	}
	
	
	@Override
	public String getPartitioningId() {
		return Rweave.LTX_R_PARTITIONING;
	}
	
	@Override
	protected Partitioner createDocumentPartitioner() {
		return new CatPartitioner(new MultiCatPartitionScanner(Rweave.LTX_R_PARTITIONING,
				new LtxChunkPartitionScanner(fTemplateMode), new RweaveChunkPartitionScanner()),
				Rweave.ALL_PARTITION_TYPES) {
			@Override
			protected String getPrefereOpenType(final String open, final String opening) {
				if ((open == ITexDocumentConstants.LTX_MATH_CONTENT_TYPE || open == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE)
						|| (opening == ITexDocumentConstants.LTX_MATH_CONTENT_TYPE || opening == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE) ) {
					return ITexDocumentConstants.LTX_MATH_CONTENT_TYPE;
				}
				return super.getPrefereOpenType(open, opening);
			}
		};
	}
	
}
