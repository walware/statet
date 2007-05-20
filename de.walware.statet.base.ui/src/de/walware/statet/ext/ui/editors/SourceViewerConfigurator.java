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

package de.walware.statet.ext.ui.editors;

import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.base.ui.util.ISettingsChangedHandler;
import de.walware.statet.ext.ui.text.PairMatcher;


/**
 *
 */
public abstract class SourceViewerConfigurator implements ISettingsChangedHandler {

	
	private PairMatcher fPairMatcher;
	private IPreferenceStore fPreferenceStore;
	private StatextSourceViewerConfiguration fConfiguration;
	private ISourceViewer fViewer;
	
	
	protected SourceViewerConfigurator() {
	}
	
	
	/**
	 * A setup participant for the document of the editor.
	 * 
	 * @return a document setup participant or <code>null</code>.
	 */
	public abstract IDocumentSetupParticipant getDocumentSetupParticipant();
	
	protected void setPreferenceStore(IPreferenceStore store) {
		fPreferenceStore = store;
	}
	
	/**
	 * @return Returns the jface PreferenceStore.
	 */
	public IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}
	
	protected void setPairMatcher(PairMatcher pairMatcher) {
		fPairMatcher = pairMatcher;
	}
	
	/**
	 * PairMatcher used for pairmatching decoration and 
	 * goto matching bracket action.
	 * 
	 * @return the pair matcher of <code>null</code>.
	 */
	public PairMatcher getPairMatcher() {
		return fPairMatcher;
	}
	
	
	protected void setConfiguration(StatextSourceViewerConfiguration config) {
		fConfiguration = config;
	}
	
	public StatextSourceViewerConfiguration getSourceViewerConfiguration() {
		return fConfiguration;
	}
	
	public void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		if (fPairMatcher != null) {
			support.setCharacterPairMatcher(fPairMatcher);
			support.setMatchingCharacterPainterPreferenceKeys(
					IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS, 
					IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
		}
		support.install(getPreferenceStore());
	}


	public void setTarget(ISourceViewer viewer) {
		fViewer = viewer;
		handleSettingsChanged(null, null);
	}
	
	protected ISourceViewer getSourceViewer() {
		return fViewer;
	}
	
	public boolean handleSettingsChanged(Set<String> contexts, Object options) {
		return false;
	}
	
}
