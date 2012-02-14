/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;

import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.nico.AbstractRDbgController;


public class StepIntoSelectionHyperlink implements IHyperlink {
	
	
	private final IRegion fRegion;
	
	private final ISourceEditor fEditor;
	private final AbstractDocument fDocument;
	private final RElementAccess fAccess;
	
	private final AbstractRDbgController fController;
	
	
	public StepIntoSelectionHyperlink(final ISourceEditor editor, final RElementAccess access,
			final AbstractRDbgController controller) {
		assert (editor != null);
		assert (access != null);
		assert (controller != null);
		
		fRegion = access.getNameNode();
		fEditor = editor;
		fDocument = (AbstractDocument) editor.getViewer().getDocument();
		fAccess = access;
		
		fController = controller;
	}
	
	
	@Override
	public String getTypeLabel() {
		return null;
	}
	
	@Override
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}
	
	@Override
	public String getHyperlinkText() {
		return null;
	}
	
	@Override
	public void open() {
		StepIntoSelectionHandler.exec(fController, fDocument, fAccess, fEditor.getWorkbenchPart());
	}
	
}
