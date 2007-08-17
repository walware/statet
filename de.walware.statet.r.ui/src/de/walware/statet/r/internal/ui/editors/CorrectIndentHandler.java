/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.ui.editors.REditor;


/**
 *
 */
public class CorrectIndentHandler extends AbstractHandler {
	
	
	private RSourceIndenter fIndenter;

	private REditor fEditor;
	private IEditorAdapter fEditorAdapter;
	
	
	/**
	 * 
	 */
	public CorrectIndentHandler(REditor editor) {
		fEditor = editor;
		fEditorAdapter = (IEditorAdapter) editor.getAdapter(IEditorAdapter.class);
	}
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (fIndenter == null) {
			fIndenter = new RSourceIndenter();
		}
		
		
		return null;
	}
	
}
