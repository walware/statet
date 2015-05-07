/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.text.IDocumentPartitioner;

import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;
import de.walware.ecommons.text.core.treepartitioner.TreePartitioner;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.ui.text.r.RPartitionNodeScanner;


/**
 * The document setup participant for R.
 */
public class RDocumentSetupParticipant extends PartitionerDocumentSetupParticipant {
	
	
	private static final String[] CONTENT_TYPES= IRDocumentConstants.R_CONTENT_TYPES.toArray(
			new String[IRDocumentConstants.R_CONTENT_TYPES.size()] );
	
	
	public RDocumentSetupParticipant() {
	}
	
	
	@Override
	public String getPartitioningId() {
		return IRDocumentConstants.R_PARTITIONING;
	}
	
	@Override
	protected IDocumentPartitioner createDocumentPartitioner() {
		return new TreePartitioner(new RPartitionNodeScanner(), CONTENT_TYPES);
	}
	
}
