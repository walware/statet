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

package de.walware.eclipsecommons.ltk;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DocumentRewriteSessionType;


/**
 * Runnable to execute a document operation in a special context.
 * 
 * @see ISourceUnit#syncExec(SourceDocumentRunnable)
 */
public abstract class SourceDocumentRunnable {
	
	
	private AbstractDocument fDocument;
	private long fStamp;
	private DocumentRewriteSessionType fRewriteSessionType;
	
	
	public SourceDocumentRunnable(final AbstractDocument document, final long assertedStamp, final DocumentRewriteSessionType rewriteSessionType) {
		fDocument = document;
		fStamp = assertedStamp;
		fRewriteSessionType = rewriteSessionType;
	}
	
	
	public final DocumentRewriteSessionType getRewriteSessionType() {
		return fRewriteSessionType;
	}
	
	public final void setNext(final AbstractDocument document, final long assertedStamp) {
		fDocument = document;
		fStamp = assertedStamp;
	}
	
	public final AbstractDocument getDocument() {
		return fDocument;
	}
	
	public final long getStampAssertion() {
		return fStamp;
	}
	
	
	public abstract void run() throws InvocationTargetException;
	
}
