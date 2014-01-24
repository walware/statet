/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource;

import de.walware.ecommons.text.IPartitionConstraint;
import de.walware.ecommons.text.PartitioningConfiguration;


/**
 * Matches all R code partitions including string but no comments and non-R partitions.
 */
public class RCodePartitionConstraint implements IPartitionConstraint {
	
	
	private final IPartitionConstraint fRDefaultPartitionConstraint;
	
	
	public RCodePartitionConstraint(final IPartitionConstraint defaultPartitionConstraint) {
		fRDefaultPartitionConstraint = defaultPartitionConstraint;
	}
	
	public RCodePartitionConstraint(final PartitioningConfiguration partitionConfiguration) {
		fRDefaultPartitionConstraint = partitionConfiguration.getDefaultPartitionConstraint();
	}
	
	
	@Override
	public boolean matches(final String partitionType) {
		return (fRDefaultPartitionConstraint.matches(partitionType)
				|| partitionType == IRDocumentPartitions.R_STRING
				|| partitionType == IRDocumentPartitions.R_QUOTED_SYMBOL
				|| partitionType == IRDocumentPartitions.R_INFIX_OPERATOR );
	}
	
}
