/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;

import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.sweave.text.CatPartitioner;
import de.walware.statet.r.sweave.text.MultiCatPartitionScanner;
import de.walware.statet.r.sweave.text.RweaveChunkPartitionScanner;
import de.walware.statet.r.sweave.text.TexChunkPartitionScanner;


/**
 * The document setup participant for Sweave (LaTeX).
 */
public class RweaveTexDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	
	private boolean fTemplateMode;
	
	
	public RweaveTexDocumentSetupParticipant() {
		this(false);
	}
	
	public RweaveTexDocumentSetupParticipant(final boolean templateMode) {
		fTemplateMode = templateMode;
	}
	
	
	public void setup(final IDocument document) {
		if (document instanceof IDocumentExtension3) {
			// Setup the document scanner
			final IDocumentPartitioner partitioner = createDocumentPartitioner();
			final IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(Rweave.R_TEX_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}
	
	private IDocumentPartitioner createDocumentPartitioner() {
		return new CatPartitioner(new MultiCatPartitionScanner(Rweave.R_TEX_PARTITIONING,
				new TexChunkPartitionScanner(fTemplateMode), new RweaveChunkPartitionScanner()),
				Rweave.R_TEX_PARTITIONS);
	}
	
}
