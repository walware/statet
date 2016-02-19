/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import de.walware.ecommons.text.Partitioner;
import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.ui.text.rd.RdFastPartitionScanner;


/**
 * The document setup participant for Rd.
 */
public class RdDocumentSetupParticipant extends PartitionerDocumentSetupParticipant {
	
	
	private static final String[] CONTENT_TYPES= IRDocumentConstants.RDOC_CONTENT_TYPES.toArray(
			new String[IRDocumentConstants.RDOC_CONTENT_TYPES.size()] );
	
	
	public RdDocumentSetupParticipant() {
	}
	
	
	@Override
	public String getPartitioningId() {
		return IRDocumentConstants.RDOC_PARTITIONING;
	}
	
	@Override
	protected Partitioner createDocumentPartitioner() {
		return new Partitioner(new RdFastPartitionScanner(), CONTENT_TYPES);
	}
	
}
