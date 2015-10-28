/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.source;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.text.core.sections.AbstractDocContentSections;


public class RdDocumentContentInfo extends AbstractDocContentSections {
	
	
	public static final String RDOC=                        IRDocumentConstants.RDOC_PARTITIONING;
	
	
	public static final RdDocumentContentInfo INSTANCE= new RdDocumentContentInfo();
	
	
	public RdDocumentContentInfo() {
		super(IRDocumentConstants.RDOC_PARTITIONING, RDOC,
				ImCollections.newList(RDOC) );
	}
	
	
	@Override
	public String getTypeByPartition(final String contentType) {
		return RDOC;
	}
	
}
