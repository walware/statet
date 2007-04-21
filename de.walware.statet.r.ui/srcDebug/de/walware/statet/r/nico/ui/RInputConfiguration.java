/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.eclipsecommons.ui.preferences.ICombinedPreferenceStore;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


public class RInputConfiguration implements IEditorConfiguration {

	
	RInputConfiguration() {
	}
	
	public StatextSourceViewerConfiguration getSourceViewerConfiguration() {
		
		ICombinedPreferenceStore store = RSourceViewerConfiguration.createCombinedPreferenceStore(
				RUIPlugin.getDefault().getPreferenceStore(), null);
		return new RSourceViewerConfiguration(null, 
				StatetUIServices.getSharedColorManager(), store);
	}
	
	public PairMatcher getPairMatcher() {
		
		return new RBracketPairMatcher();
	}

	public void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		
	}
	
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		
		return new RDocumentSetupParticipant();
	}
	
}
