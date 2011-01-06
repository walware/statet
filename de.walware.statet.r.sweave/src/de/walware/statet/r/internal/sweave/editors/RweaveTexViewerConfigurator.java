/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.text.PartitioningConfiguration;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


/**
 * Configurator for Sweave (LaTeX/R) code source viewers.
 */
public class RweaveTexViewerConfigurator extends RSourceViewerConfigurator {
	
	
	public RweaveTexViewerConfigurator(final IRCoreAccess core) {
		super(core);
	}
	
	
	@Override
	public PartitioningConfiguration getPartitioning() {
		return Rweave.TEX_PARTITIONING_CONFIG;
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RweaveTexDocumentSetupParticipant();
	}
	
	@Override
	public void setConfiguration(final SourceEditorViewerConfiguration config) {
		super.setConfiguration(config);
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		if (groupIds != null && groupIds.contains(SweaveEditorOptions.GROUP_ID) && getREditor() != null) {
			fUpdateCompleteConfig = true;
			SpellingProblem.removeAllInActiveEditor(getREditor(), null);
		}
		super.handleSettingsChanged(groupIds, options);
	}
	
}
