/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;

import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.r.internal.debug.ui.REditorDebugHover;


public class REditorHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {
	
	
	private ISourceEditor fEditor;
	private ISourceEditorHover fHover;
	
	
	public REditorHover(final ISourceEditor editor) {
		fEditor = editor;
	}
	
	
	protected boolean ensureHover() {
		if (fHover == null) {
			fHover = new REditorDebugHover();
			fHover.setEditor(fEditor);
		}
		return true;
	}
	
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		if (ensureHover()) {
			return fHover.getHoverRegion(offset);
		}
		return null;
	}
	
	public IInformationControlCreator getHoverControlCreator() {
		if (ensureHover()) {
			return fHover.getHoverControlCreator();
		}
		return null;
	}
	
	public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		return null;
	}
	
	public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
		if (ensureHover()) {
			return fHover.getHoverInfo(hoverRegion);
		}
		return null;
	}
	
}
