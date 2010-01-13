/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.ui.editors.RdDocumentSetupParticipant;


public class RdDocumentProvider extends TextFileDocumentProvider {
	
	
	public RdDocumentProvider() {
		final IDocumentProvider provider = new ForwardingDocumentProvider(IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING,
				new RdDocumentSetupParticipant(), new TextFileDocumentProvider());
		setParentDocumentProvider(provider);
	}
	
}
