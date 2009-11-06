/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.console;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsoleViewer;

import de.walware.ecommons.ui.util.UIAccess;


public class OutputViewer extends TextConsoleViewer {
	
	
	/**
	 * will always scroll with output if value is true.
	 */
	private boolean fAutoScroll = true;
	
	private IDocumentListener fDocumentListener;
	
	
	public OutputViewer(final Composite parent, final TextConsole console) {
		super(parent, console);
		setEditable(false);
	}
	
	
	public boolean isAutoScroll() {
		return fAutoScroll;
	}
	
	public void setAutoScroll(final boolean scroll) {
		fAutoScroll = scroll;
	}
	
	@Override
	public void setDocument(final IDocument document) {
		final IDocument oldDocument = getDocument();
		if (oldDocument != null) {
			oldDocument.removeDocumentListener(getDocumentListener());
		}
		
		super.setDocument(document);
		
		if (document != null) {
			document.addDocumentListener(getDocumentListener());
		}
	}
	
	private IDocumentListener getDocumentListener() {
		if (fDocumentListener == null) {
			fDocumentListener = new IDocumentListener() {
				public void documentAboutToBeChanged(final DocumentEvent event) {
				}
				
				public void documentChanged(final DocumentEvent event) {
					if (fAutoScroll) {
						revealEndOfDocument();
					}
				}
			};
		}
		return fDocumentListener;
	}
	
	@Override
	public void revealEndOfDocument() {
		UIAccess.getDisplay().asyncExec(new Runnable() {
			public void run() {
				final StyledText textWidget = getTextWidget();
				if (UIAccess.isOkToUse(textWidget)) {
					final int lineCount = textWidget.getLineCount();
					final int lineToShow = ((lineCount > 1 && 
							textWidget.getCharCount() == textWidget.getOffsetAtLine(lineCount - 1)) ?
							(lineCount - 2) : (lineCount - 1));
					final int visiblePixel = textWidget.getClientArea().height;
					final int linePixel = textWidget.getLineHeight();
					final int topPixel = linePixel * (lineToShow) - visiblePixel + 
							(int) (linePixel * 1.33) + 2;
					if (topPixel >= 0) {
						textWidget.setTopPixel(topPixel);
					}
				}
			}
		});
	}
	
}
