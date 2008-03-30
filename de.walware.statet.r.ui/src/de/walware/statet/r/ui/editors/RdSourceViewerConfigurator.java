/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
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
import de.walware.eclipsecommons.ui.text.PairMatcher;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;
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
	
	private static final Set<String> INPUT_CHANGE_GROUPS = new HashSet<String>(Arrays.asList(new String[] {
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private IRCoreAccess fSourceCoreAccess;
	private RdSourceViewerConfiguration fConfig;
	
	
	public RdSourceViewerConfigurator(final IRCoreAccess core, final IPreferenceStore store) {
		setPairMatcher(new PairMatcher(BRACKETS,
				IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING, new String[] { IRDocumentPartitions.RDOC_DEFAULT }, '\\'));
		setSource(core);
		setPreferenceStore(store);
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RdDocumentSetupParticipant();
	}
	
	public void setConfiguration(final RdSourceViewerConfiguration config) {
		fConfig = config;
		super.setConfiguration(config);
	}
	
	public void setTarget(final RdEditor editor) {
		fIsConfigured = true;
		setTarget((IEditorAdapter) editor.getAdapter(IEditorAdapter.class), false);
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
	
	@Override
	public boolean handleSettingsChanged(Set<String> groupIds, final Object options) {
		final ISourceViewer viewer = getSourceViewer();
		if (viewer == null || fConfig == null) {
			return false;
		}
		if (groupIds == null) {
			groupIds = INPUT_CHANGE_GROUPS;
		}
		final Point selectedRange = viewer.getSelectedRange();
		
		final boolean affectsPresentation = fConfig.handleSettingsChanged(groupIds, viewer);
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
