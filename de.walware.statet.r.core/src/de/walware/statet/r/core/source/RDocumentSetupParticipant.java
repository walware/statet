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

package de.walware.statet.r.core.source;

import org.eclipse.jface.text.IDocumentPartitioner;

import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;
import de.walware.ecommons.text.core.treepartitioner.TreePartitioner;


/**
 * The document setup participant for R.
 */
public class RDocumentSetupParticipant extends PartitionerDocumentSetupParticipant {
	
	
	public RDocumentSetupParticipant() {
	}
	
	
	@Override
	public String getPartitioningId() {
		return IRDocumentConstants.R_PARTITIONING;
	}
	
	@Override
	protected IDocumentPartitioner createDocumentPartitioner() {
		return new TreePartitioner(getPartitioningId(),
				new RPartitionNodeScanner(),
				IRDocumentConstants.R_CONTENT_TYPES );
	}
	
}
