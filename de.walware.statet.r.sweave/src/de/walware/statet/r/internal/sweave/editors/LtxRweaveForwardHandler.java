/*******************************************************************************
 * Copyright (c) 2011-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.text.source.SourceViewer;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;

import de.walware.statet.r.sweave.text.LtxRweaveSwitch;


public class LtxRweaveForwardHandler extends AbstractHandler {
	
	
	private final ISourceEditor fEditor;
	
	private final IHandler2 fRHandler;
	private final IHandler2 fControlHandler;
	private final IHandler2 fTexHandler;
	
	
	public LtxRweaveForwardHandler(final ISourceEditor editor,
			final IHandler2 texHandler, final IHandler2 rHandler) {
		fEditor = editor;
		
		fTexHandler = texHandler;
		fControlHandler = null;
		fRHandler = rHandler;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final SourceViewer viewer = fEditor.getViewer();
		if (viewer == null) {
			return null;
		}
		switch(LtxRweaveSwitch.get(viewer.getDocument(), viewer.getSelectedRange().x)) {
		case LTX:
			if (fTexHandler != null) {
				fTexHandler.execute(event);
			}
			break;
		case CHUNK_CONTROL:
			if (fControlHandler != null) {
				fControlHandler.execute(event);
			}
			break;
		case R:
			if (fRHandler != null) {
				fRHandler.execute(event);
			}
			break;
		default:
			break;
		}
		return null;
	}
	
}
