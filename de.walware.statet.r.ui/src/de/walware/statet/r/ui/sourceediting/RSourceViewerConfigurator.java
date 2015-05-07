/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.IPreferenceAccess;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Configurator for R code source viewers.
 */
public class RSourceViewerConfigurator extends SourceEditorViewerConfigurator
		implements IRCoreAccess, PropertyChangeListener {
	
	
	private static final Set<String> RESET_GROUP_IDS = new HashSet<String>(Arrays.asList(new String[] {
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private IRCoreAccess fSourceCoreAccess;
	
	private final RCodeStyleSettings fRCodeStyleCopy;
	
	
	public RSourceViewerConfigurator(final IRCoreAccess coreAccess,
			final RSourceViewerConfiguration config) {
		super(config);
		fRCodeStyleCopy = new RCodeStyleSettings(1);
		config.setCoreAccess(this);
		setSource(coreAccess);
		
		fRCodeStyleCopy.load(fSourceCoreAccess.getRCodeStyle());
		fRCodeStyleCopy.resetDirty();
		fRCodeStyleCopy.addPropertyChangeListener(this);
	}
	
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RDocumentSetupParticipant();
	}
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
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
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		
		fRCodeStyleCopy.resetDirty();
	}
	
	@Override
	protected void checkSettingsChanges(final Set<String> groupIds, final Map<String, Object> options) {
		super.checkSettingsChanges(groupIds, options);
		
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			fRCodeStyleCopy.load(fSourceCoreAccess.getRCodeStyle());
		}
		if (groupIds.contains(REditorOptions.GROUP_ID)) {
			fUpdateCompleteConfig = true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			fUpdateInfoHovers = true;
		}
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return fRCodeStyleCopy;
	}
	
	@Override
	public IPreferenceAccess getPrefs() {
		return fSourceCoreAccess.getPrefs();
	}
	
}
