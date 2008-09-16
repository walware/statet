/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.text;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;


/**
 * {@link IDocumentSetupParticipant} for {@link Partitioner}
 */
public abstract class PartitionerDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	
	public void setup(final IDocument document) {
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			
			// Setup the document scanner
			final Partitioner partitioner = createDocumentPartitioner();
			partitioner.connect(document, true);
			
			if (partitioner.equals(extension3.getDocumentPartitioner(getPartitioningId()))) {
				partitioner.disconnect();
				return;
			}
			extension3.setDocumentPartitioner(getPartitioningId(), partitioner);
		}
	}
	
	protected abstract String getPartitioningId();
	
	protected abstract Partitioner createDocumentPartitioner();
	
}
