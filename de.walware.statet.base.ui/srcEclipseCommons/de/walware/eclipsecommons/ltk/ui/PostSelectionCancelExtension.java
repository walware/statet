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

package de.walware.eclipsecommons.ltk.ui;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public abstract class PostSelectionCancelExtension implements ISelectionChangedListener, ITextInputListener, IDocumentListener {
	
	
	PostSelectionWithElementInfoController fController;
	
	
	public abstract void init();
	public abstract void dispose();
	
	public void selectionChanged(SelectionChangedEvent event) {
//		fController.cancel();
	}
	
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput != null) {
			oldInput.removeDocumentListener(this);
		}
		fController.cancel();
	}
	
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput != null) {
			newInput.addDocumentListener(this);
		}
	}
	
	public void documentAboutToBeChanged(DocumentEvent event) {
		fController.cancel();
	}
	
	public void documentChanged(DocumentEvent event) {
	}
	
}
