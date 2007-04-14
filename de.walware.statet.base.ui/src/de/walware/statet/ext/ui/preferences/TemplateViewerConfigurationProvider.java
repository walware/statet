/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;


/**
 * Configures the EditTemplateDialog for your language.
 * 
 * @author Stephan Wahlbrink
 */
public class TemplateViewerConfigurationProvider {


	private StatextSourceViewerConfiguration fSourceViewerConfiguration;
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	private IPreferenceStore fPreferenceStore;
	
	/**
	 * @param sourceViewerConfiguration
	 * @param documentSetupParticipant
	 * @param preferenceStore
	 */
	public TemplateViewerConfigurationProvider(
			StatextSourceViewerConfiguration sourceViewerConfiguration,
			IDocumentSetupParticipant documentSetupParticipant,
			IPreferenceStore preferenceStore) {
		fSourceViewerConfiguration = sourceViewerConfiguration;
		fDocumentSetupParticipant = documentSetupParticipant;
		fPreferenceStore = preferenceStore;
	}

	/**
	 * @return Returns the DocumentSetupParticipant.
	 */
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return fDocumentSetupParticipant;
	}
	
	/**
	 * @return Returns the SourceViewerConfiguration.
	 */
	public StatextSourceViewerConfiguration getSourceViewerConfiguration() {
		return fSourceViewerConfiguration;
	}
	
	/**
	 * @return Returns the PreferenceStore.
	 */
	public IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}
}
