/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import de.walware.statet.r.ui.IRDocumentPartitions;
import de.walware.statet.r.ui.text.r.RFastPartitionScanner;


/**
 * The document setup participant for R.
 */
public class RDocumentSetupParticipant implements IDocumentSetupParticipant {


	public static class ExtFastPartitioner extends FastPartitioner {
		
		private IPartitionTokenScanner fScanner;
		
		public ExtFastPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
			
			super(scanner, legalContentTypes);
			fScanner = scanner;
		}
		
		public IPartitionTokenScanner getPartitionTokenScanner() {
			
			return fScanner;
		}
	}
	
	
	public RDocumentSetupParticipant() {
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			// Setup the document scanner
			IDocumentPartitioner partitioner = createDocumentPartitioner();
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(IRDocumentPartitions.R_DOCUMENT_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}
	
	private IDocumentPartitioner createDocumentPartitioner() {
		return new ExtFastPartitioner(
				new RFastPartitionScanner(), IRDocumentPartitions.R_PARTITIONS);
	}
}
