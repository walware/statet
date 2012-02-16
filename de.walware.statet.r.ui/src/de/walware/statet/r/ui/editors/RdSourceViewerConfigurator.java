/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.text.PartitioningConfiguration;

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
	
	
	private static final Set<String> RESET_GROUPS_IDS = new HashSet<String>(Arrays.asList(new String[] {
			TaskTagsPreferences.GROUP_ID, 
	}));
	
	
	private IRCoreAccess fSourceCoreAccess;
	
	
	public RdSourceViewerConfigurator(final IRCoreAccess core,
			final RdSourceViewerConfiguration config) {
		super(config);
		config.setCoreAccess(this);
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
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUPS_IDS;
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
	public RCodeStyleSettings getRCodeStyle() {
		return null;
	}
	
	@Override
	public IPreferenceAccess getPrefs() {
		return fSourceCoreAccess.getPrefs();
	}
	
}
