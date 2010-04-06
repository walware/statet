/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import de.walware.ecommons.text.Partitioner;
import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;

import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.sweave.text.CatPartitioner;
import de.walware.statet.r.sweave.text.MultiCatPartitionScanner;
import de.walware.statet.r.sweave.text.RweaveChunkPartitionScanner;
import de.walware.statet.r.sweave.text.TexChunkPartitionScanner;


/**
 * The document setup participant for Sweave (LaTeX).
 */
public class RweaveTexDocumentSetupParticipant extends PartitionerDocumentSetupParticipant {
	
	
	private boolean fTemplateMode;
	
	
	public RweaveTexDocumentSetupParticipant() {
		this(false);
	}
	
	public RweaveTexDocumentSetupParticipant(final boolean templateMode) {
		fTemplateMode = templateMode;
	}
	
	
	@Override
	public String getPartitioningId() {
		return Rweave.R_TEX_PARTITIONING;
	}
	
	@Override
	protected Partitioner createDocumentPartitioner() {
		return new CatPartitioner(new MultiCatPartitionScanner(Rweave.R_TEX_PARTITIONING,
				new TexChunkPartitionScanner(fTemplateMode), new RweaveChunkPartitionScanner()),
				Rweave.ALL_PARTITION_TYPES);
	}
	
}
