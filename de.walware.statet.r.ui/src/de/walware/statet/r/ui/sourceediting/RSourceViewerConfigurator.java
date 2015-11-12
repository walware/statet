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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.source.RDocumentSetupParticipant;
import de.walware.statet.r.core.util.RCoreAccessWrapper;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;
import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Configurator for R code source viewers.
 */
public class RSourceViewerConfigurator extends SourceEditorViewerConfigurator {
	
	
	private static final Set<String> RESET_GROUP_IDS= new HashSet<>(ImCollections.newList(
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID ));
	
	
	private RCoreAccessWrapper rCoreAccess;
	
	
	public RSourceViewerConfigurator(final IRCoreAccess coreAccess,
			final RSourceViewerConfiguration config) {
		super(config);
		this.rCoreAccess= new RCoreAccessWrapper(coreAccess) {
			private final RCodeStyleSettings codeStyle= new RCodeStyleSettings(1);
			@Override
			public RCodeStyleSettings getRCodeStyle() {
				return this.codeStyle;
			}
		};
		config.setCoreAccess(this.rCoreAccess);
		
		this.rCoreAccess.getRCodeStyle().load(this.rCoreAccess.getParent().getRCodeStyle());
		this.rCoreAccess.getRCodeStyle().resetDirty();
		this.rCoreAccess.getRCodeStyle().addPropertyChangeListener(this);
	}
	
	
	public final IRCoreAccess getRCoreAccess() {
		return this.rCoreAccess;
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RDocumentSetupParticipant();
	}
	
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
	}
	
	
	public void setSource(final IRCoreAccess rCoreAccess) {
		if (rCoreAccess != null) {
			this.rCoreAccess.setParent(rCoreAccess);
			handleSettingsChanged(null, null);
		}
	}
	
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		
		this.rCoreAccess.getRCodeStyle().resetDirty();
	}
	
	@Override
	protected void checkSettingsChanges(final Set<String> groupIds, final Map<String, Object> options) {
		super.checkSettingsChanges(groupIds, options);
		
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			this.rCoreAccess.getRCodeStyle().load(
					this.rCoreAccess.getParent().getRCodeStyle() );
		}
		if (groupIds.contains(REditorOptions.GROUP_ID)) {
			fUpdateCompleteConfig = true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			fUpdateInfoHovers = true;
		}
	}
	
}
