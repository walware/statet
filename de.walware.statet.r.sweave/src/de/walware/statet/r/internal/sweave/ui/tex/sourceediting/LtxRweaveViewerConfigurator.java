/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.ui.tex.sourceediting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.text.PartitioningConfiguration;

import de.walware.docmlet.tex.core.TexCodeStyleSettings;
import de.walware.docmlet.tex.core.TexCore;
import de.walware.docmlet.tex.core.commands.TexCommandSet;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.sweave.editors.LtxRweaveDocumentSetupParticipant;
import de.walware.statet.r.internal.sweave.editors.SweaveEditorOptions;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;
import de.walware.statet.r.sweave.ITexRweaveCoreAccess;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;
import de.walware.statet.r.sweave.text.Rweave;


/**
 * Configurator for Sweave (LaTeX/R) code source viewers.
 */
public class LtxRweaveViewerConfigurator extends SourceEditorViewerConfigurator implements ITexRweaveCoreAccess {
	
	
	private static final Set<String> RESET_GROUP_IDS= new HashSet<>(Arrays.asList(new String[] {
			TexCodeStyleSettings.INDENT_GROUP_ID,
			RCodeStyleSettings.INDENT_GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private ITexRweaveCoreAccess fSourceCoreAccess;
	
	private final TexCodeStyleSettings fTexCodeStyleCopy;
	private final RCodeStyleSettings fRCodeStyleCopy;
	
	
	public LtxRweaveViewerConfigurator(final ITexRweaveCoreAccess coreAccess,
			final LtxRweaveViewerConfiguration config) {
		super(config);
		this.fTexCodeStyleCopy= new TexCodeStyleSettings(1);
		this.fRCodeStyleCopy= new RCodeStyleSettings(1);
		config.setCoreAccess(this);
		setSource(coreAccess);
		
		this.fTexCodeStyleCopy.load(this.fSourceCoreAccess.getTexCodeStyle());
		this.fTexCodeStyleCopy.resetDirty();
		this.fTexCodeStyleCopy.addPropertyChangeListener(this);
		
		this.fRCodeStyleCopy.load(this.fSourceCoreAccess.getRCodeStyle());
		this.fRCodeStyleCopy.resetDirty();
		this.fRCodeStyleCopy.addPropertyChangeListener(this);
	}
	
	
	@Override
	public PartitioningConfiguration getPartitioning() {
		return Rweave.LTX_PARTITIONING_CONFIG;
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new LtxRweaveDocumentSetupParticipant();
	}
	
	@Override
	protected Set<String> getResetGroupIds() {
		return RESET_GROUP_IDS;
	}
	
	
	public void setSource(ITexRweaveCoreAccess newAccess) {
		if (newAccess == null) {
			newAccess= new TexRweaveCoreAccess(
					TexCore.getWorkbenchAccess(), RCore.getWorkbenchAccess() );
		}
		if (this.fSourceCoreAccess != newAccess) {
			this.fSourceCoreAccess= newAccess;
			handleSettingsChanged(null, null);
		}
	}
	
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		super.handleSettingsChanged(groupIds, options);
		
		this.fTexCodeStyleCopy.resetDirty();
		this.fRCodeStyleCopy.resetDirty();
	}
	
	@Override
	protected void checkSettingsChanges(final Set<String> groupIds, final Map<String, Object> options) {
		super.checkSettingsChanges(groupIds, options);
		
		if (groupIds.contains(TexCodeStyleSettings.INDENT_GROUP_ID)) {
			this.fTexCodeStyleCopy.load(this.fSourceCoreAccess.getTexCodeStyle());
		}
		if (groupIds.contains(RCodeStyleSettings.INDENT_GROUP_ID)
				|| groupIds.contains(RCodeStyleSettings.WS_GROUP_ID)) {
			this.fRCodeStyleCopy.load(this.fSourceCoreAccess.getRCodeStyle());
		}
		if (groupIds.contains(SweaveEditorOptions.GROUP_ID)) {
			this.fUpdateCompleteConfig= true;
		}
		if (groupIds.contains(RUIPreferenceInitializer.REDITOR_HOVER_GROUP_ID)) {
			this.fUpdateInfoHovers= true;
		}
	}
	
	
	@Override
	public IPreferenceAccess getPrefs() {
		return this.fSourceCoreAccess.getPrefs();
	}
	
	@Override
	public RCodeStyleSettings getRCodeStyle() {
		return this.fRCodeStyleCopy;
	}
	
	@Override
	public TexCommandSet getTexCommandSet() {
		return this.fSourceCoreAccess.getTexCommandSet();
	}
	
	@Override
	public TexCodeStyleSettings getTexCodeStyle() {
		return this.fTexCodeStyleCopy;
	}
	
}
