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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.actions.AbstractOpenDeclarationHandler;

import de.walware.docmlet.tex.ui.sourceediting.LtxOpenDeclarationHandler;

import de.walware.statet.r.sweave.text.LtxRweaveSwitch;
import de.walware.statet.r.ui.sourceediting.ROpenDeclarationHandler;


public class LtxRweaveOpenDeclarationHandler extends AbstractOpenDeclarationHandler {
	
	
	private final ROpenDeclarationHandler fRHandler = new ROpenDeclarationHandler();
	private final LtxOpenDeclarationHandler fTexHandler = new LtxOpenDeclarationHandler();
	
	
	public LtxRweaveOpenDeclarationHandler() {
	}
	
	
	@Override
	public boolean execute(final ISourceEditor editor, final IRegion selection) {
		switch (LtxRweaveSwitch.get(editor.getViewer().getDocument(), selection.getOffset())) {
		case LTX:
			return fTexHandler.execute(editor, new Region(selection.getOffset(), selection.getLength()));
		case R:
			return fRHandler.execute(editor, new Region(selection.getOffset(), selection.getLength()));
		default:
			return false;
		}
	}
	
}
