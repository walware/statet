/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.statet.ext.ui.text.PairMatcher;


public interface IEditorConfiguration {

	public StatextSourceViewerConfiguration getSourceViewerConfiguration();

	/**
	 * PairMatcher used for pairmatching decoration and 
	 * goto matching bracket action.
	 * 
	 * @return the pair matcher of <code>null</code>.
	 */
	public PairMatcher getPairMatcher();
	
	/**
	 * Chance to configure the SourceViewerDecorationSupport.
	 * 
	 * @param support the SourceViewerDecorationSupport to configure.
	 */
	public void configureSourceViewerDecorationSupport(
			SourceViewerDecorationSupport support);

	/**
	 * A setup participant for the document of the editor.
	 * 
	 * @return a document setup participant or <code>null</code>.
	 */
	public IDocumentSetupParticipant getDocumentSetupParticipant();

}