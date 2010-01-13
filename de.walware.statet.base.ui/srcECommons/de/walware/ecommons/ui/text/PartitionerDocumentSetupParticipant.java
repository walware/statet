/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.ECommonsUI;


/**
 * {@link IDocumentSetupParticipant} for {@link Partitioner}
 */
public abstract class PartitionerDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	
	public void setup(final IDocument document) {
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			
			if (extension3.getDocumentPartitioner(getPartitioningId()) == null) {
				// Setup the document scanner
				final Partitioner partitioner = createDocumentPartitioner();
				partitioner.connect(document, true);
				extension3.setDocumentPartitioner(getPartitioningId(), partitioner);
			}
			else {
				final Partitioner partitioner = createDocumentPartitioner();
				partitioner.connect(document, true);
				if (!Partitioner.equalPartitioner(partitioner, extension3.getDocumentPartitioner(getPartitioningId()))) {
					StatusManager.getManager().handle(new Status(IStatus.WARNING, ECommonsUI.PLUGIN_ID,
							"Different partitioner for same partitioning!")); //$NON-NLS-1$
				}
				partitioner.disconnect();
			}
		}
	}
	
	protected abstract String getPartitioningId();
	
	protected abstract Partitioner createDocumentPartitioner();
	
}
