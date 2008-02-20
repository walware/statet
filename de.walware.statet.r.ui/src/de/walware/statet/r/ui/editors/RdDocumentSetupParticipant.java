/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;

import de.walware.eclipsecommons.ui.text.Partitioner;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.ui.text.rd.RdFastPartitionScanner;


/**
 * The document setup participant for Rd.
 */
public class RdDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	public RdDocumentSetupParticipant() {
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			// Setup the document scanner
			IDocumentPartitioner partitioner = createDocumentPartitioner();
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}
	
	private IDocumentPartitioner createDocumentPartitioner() {
		return new Partitioner(
				new RdFastPartitionScanner(), IRDocumentPartitions.RDOC_PARTITIONS);
	}
	
}
