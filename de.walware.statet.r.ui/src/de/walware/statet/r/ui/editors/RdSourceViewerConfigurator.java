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

package de.walware.statet.r.ui.editors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;


/**
 * Configurator for Rd source viewers.
 */
public class RdSourceViewerConfigurator extends SourceEditorViewerConfigurator
		implements IRCoreAccess {
	
	
	private static final Set<String> INPUT_CHANGE_GROUPS = new HashSet<String>(
			Arrays.asList(new String[] { TaskTagsPreferences.GROUP_ID, }));
	
	private IRCoreAccess fSourceCoreAccess;
	private RdSourceViewerConfiguration fConfig;
	
	
	public RdSourceViewerConfigurator(final IRCoreAccess core) {
		setSource(core);
	}
	
	
	@Override
	public PartitioningConfiguration getPartitioning() {
		return IRDocumentPartitions.RDOC_PARTITIONING_CONFIG;
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RdDocumentSetupParticipant();
	}
	
	public void setConfiguration(final RdSourceViewerConfiguration config) {
		fConfig = config;
		super.setConfiguration(config);
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
	public void handleSettingsChanged(Set<String> groupIds,
			Map<String, Object> options) {
		final ISourceViewer viewer = getSourceViewer();
		if (viewer == null || fConfig == null) {
			return;
		}
		if (groupIds == null) {
			groupIds = INPUT_CHANGE_GROUPS;
		}
		if (options == null) {
			options = new HashMap<String, Object>();
		}
		final Point selectedRange = viewer.getSelectedRange();
		
		options.put(ISettingsChangedHandler.VIEWER_KEY, viewer);
		fConfig.handleSettingsChanged(groupIds, options);
		if (options
				.containsKey(ITextPresentationConstants.SETTINGSCHANGE_AFFECTSPRESENTATION_KEY)) {
			viewer.invalidateTextPresentation();
		}
		
		viewer.setSelectedRange(selectedRange.x, selectedRange.y);
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return null;
	}
	
	@Override
	public IPreferenceAccess getPrefs() {
		return fSourceCoreAccess.getPrefs();
	}
	
}
