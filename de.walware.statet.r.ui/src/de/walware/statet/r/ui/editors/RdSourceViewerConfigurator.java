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

package de.walware.statet.r.ui.editors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 *
 */
public class RdSourceViewerConfigurator extends SourceViewerConfigurator
		implements IRCoreAccess {

	private static final char[][] BRACKETS = { {'{', '}'} };

	private static final Set<String> INPUT_CHANGE_CONTEXTS = new HashSet<String>(Arrays.asList(new String[] {
			TaskTagsPreferences.CONTEXT_ID,
	}));

	
	private IRCoreAccess fSourceCoreAccess;
	private RdSourceViewerConfiguration fConfig;
	
	
	public RdSourceViewerConfigurator(IRCoreAccess core, IPreferenceStore store) {
		setPairMatcher(new PairMatcher(BRACKETS, IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING, IRDocumentPartitions.R_DEFAULT, '\\'));
		setSource(core);
		setPreferenceStore(store);
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RdDocumentSetupParticipant();
	}
	
	public void setConfiguration(RdSourceViewerConfiguration config) {
		fConfig = config;
		super.setConfiguration(config);
	}
	
	public void setTarget(RdEditor editor, ISourceViewer viewer) {
		fIsConfigured = true;
		setTarget(viewer, false);
	}

	public void setSource(IRCoreAccess newAccess) {
		if (newAccess == null) {
			newAccess = RCore.getWorkbenchAccess();
		}
		if (fSourceCoreAccess != newAccess) {
			fSourceCoreAccess = newAccess;
			handleSettingsChanged(null, null);
		}
	}

	public boolean handleSettingsChanged(Set<String> contexts, Object options) {
		ISourceViewer viewer = getSourceViewer();
		if (viewer == null || fConfig == null) {
			return false;
		}
		if (contexts == null) {
			contexts = INPUT_CHANGE_CONTEXTS;
		}
		Point selectedRange = viewer.getSelectedRange();
		
		boolean affectsPresentation = fConfig.handleSettingsChanged(contexts, viewer);
		if (affectsPresentation) {
			viewer.invalidateTextPresentation();
		}
		
		viewer.setSelectedRange(selectedRange.x, selectedRange.y);
		return false;
	}
	
	public RCodeStyleSettings getRCodeStyle() {
		return null;
	}
	
	public IPreferenceAccess getPrefs() {
		return fSourceCoreAccess.getPrefs();
	}
}
