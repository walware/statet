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

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * The document factory for R
 */
public class RDocumentFactory  implements IDocumentFactory {

	public RDocumentFactory() {
	}
		
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
	 */
	public IDocument createDocument() {
		return new Document();
	}
}