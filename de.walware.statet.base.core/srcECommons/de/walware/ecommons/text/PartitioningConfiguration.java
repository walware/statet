/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.text;


public class PartitioningConfiguration {
	
	
	private final String fPartitioning;
	
	private final IPartitionConstraint fDefaultPartitions;
	
	
	public PartitioningConfiguration(final String partitioning,
			final IPartitionConstraint defaultPartitionConstraint) {
		fPartitioning = partitioning;
		fDefaultPartitions = defaultPartitionConstraint;
	}
	
	
	public String getPartitioning() {
		return fPartitioning;
	}
	
	public IPartitionConstraint getDefaultPartitionConstraint() {
		return fDefaultPartitions;
	}
	
}
