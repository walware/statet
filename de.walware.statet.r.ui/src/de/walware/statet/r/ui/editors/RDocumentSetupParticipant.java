/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import de.walware.ecommons.text.Partitioner;
import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.ui.text.r.RFastPartitionScanner;


/**
 * The document setup participant for R.
 */
public class RDocumentSetupParticipant extends PartitionerDocumentSetupParticipant {
	
	
	public RDocumentSetupParticipant() {
	}
	
	
	@Override
	public String getPartitioningId() {
		return IRDocumentPartitions.R_PARTITIONING;
	}
	
	@Override
	protected Partitioner createDocumentPartitioner() {
		return new Partitioner(
				new RFastPartitionScanner(), IRDocumentPartitions.R_PARTITIONS);
	}
	
}
