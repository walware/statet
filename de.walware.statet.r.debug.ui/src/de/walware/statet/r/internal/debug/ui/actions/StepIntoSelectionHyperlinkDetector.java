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

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;

import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.internal.debug.ui.RDebugUIUtils;
import de.walware.statet.r.nico.AbstractRDbgController;


public class StepIntoSelectionHyperlinkDetector extends AbstractHyperlinkDetector {
	
	
	public StepIntoSelectionHyperlinkDetector() {
	}
	
	
	@Override
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer,
			final IRegion region, final boolean canShowMultipleHyperlinks) {
		final ISourceEditor editor = (ISourceEditor) getAdapter(ISourceEditor.class);
		if (editor == null) {
			return null;
		}
		final AbstractRDbgController controller = RDebugUIUtils.getRDbgController(editor);
		if (controller == null) {
			return null;
		}
		final RElementAccess access = StepIntoSelectionHandler.searchAccess(editor, region);
		if (access != null) {
			return new IHyperlink[] {
					new StepIntoSelectionHyperlink(editor, access, controller) };
		}
		return null;
	}
	
}
