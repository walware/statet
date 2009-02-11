/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.ui.text.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.util.ISettingsChangedHandler;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


/**
 * Configurator for R code source viewers.
 */
public class RSourceViewerConfigurator extends SourceEditorViewerConfigurator
		implements IRCoreAccess, PropertyChangeListener {
	
	
	private static final Set<String> INPUT_CHANGE_GROUPS = new HashSet<String>(Arrays.asList(new String[] {
			RCodeStyleSettings.GROUP_ID,
			TaskTagsPreferences.GROUP_ID,
	}));
	
	
	private REditor fRealEditor;
	private SourceEditorViewerConfiguration fConfig;
	
	private RCodeStyleSettings fRCodeStyleCopy;
	private IRCoreAccess fSourceCoreAccess;
	
	protected boolean fUpdateCompleteConfig;
	private boolean fUpdateTextPresentation;
	private boolean fUpdateTabSize;
	private boolean fUpdateIndent;
	
	
	public RSourceViewerConfigurator(final IRCoreAccess core) {
		setSource(core);
		fRCodeStyleCopy = new RCodeStyleSettings();
		fRCodeStyleCopy.load(fSourceCoreAccess.getRCodeStyle());
		fRCodeStyleCopy.resetDirty();
		fRCodeStyleCopy.addPropertyChangeListener(this);
		
		initialize();
	}
	
	protected void initialize() {
		setPairMatcher(new RBracketPairMatcher());
	}
	
	
	@Override
	public PartitioningConfiguration getPartitioning() {
		return IRDocumentPartitions.R_PARTITIONING_CONFIG;
	}
	
	@Override
	public IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RDocumentSetupParticipant();
	}
	
	@Override
	public void setConfiguration(final SourceEditorViewerConfiguration config) {
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
	public void setTarget(final ISourceEditor sourceEditor) {
		if (sourceEditor instanceof REditor) {
			fRealEditor = (REditor) sourceEditor;
		}
		fUpdateIndent = true;
		super.setTarget(sourceEditor);
	}
	
	protected REditor getREditor() {
		return fRealEditor;
	}
	
	public void propertyChange(final PropertyChangeEvent event) {
		final String name = event.getPropertyName();
		if (name.equals(RCodeStyleSettings.PROP_TAB_SIZE)) {
			fUpdateTabSize = true;
			fUpdateIndent = true;
			return;
		}
		if (name.equals(RCodeStyleSettings.PROP_INDENT_SPACES_COUNT)
				|| name.equals(RCodeStyleSettings.PROP_REPLACE_TABS_WITH_SPACES)
				|| name.equals(RCodeStyleSettings.PROP_INDENT_DEFAULT_TYPE)) {
			fUpdateIndent = true;
			return;
		}
	}
	
	@Override
	public void handleSettingsChanged(Set<String> groupIds, Map<String, Object> options) {
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
		
		if (groupIds.contains(RCodeStyleSettings.GROUP_ID)) {
			fRCodeStyleCopy.load(fSourceCoreAccess.getRCodeStyle());
		}
		if (groupIds.contains(REditorOptions.GROUP_ID) && fRealEditor != null) {
			fUpdateCompleteConfig = true;
			SpellingProblem.removeAllInActiveEditor(fRealEditor, null);
		}
		options.put(ISettingsChangedHandler.VIEWER_KEY, viewer);
		fConfig.handleSettingsChanged(groupIds, options);
		fUpdateTextPresentation = options.containsKey(ITextPresentationConstants.SETTINGSCHANGE_AFFECTSPRESENTATION_KEY);
		
		updateSourceViewer(viewer);
		viewer.setSelectedRange(selectedRange.x, selectedRange.y);
		fRCodeStyleCopy.resetDirty();
	}
	
	protected void updateSourceViewer(final ISourceViewer viewer) {
		if (!fIsConfigured) {
			return;
		}
		if (fUpdateCompleteConfig) {
			reconfigureSourceViewer();
		}
		else {
			if (fUpdateTabSize) {
				viewer.getTextWidget().setTabs(fConfig.getTabWidth(viewer));
			}
			if (fUpdateTextPresentation) {
				viewer.invalidateTextPresentation();
			}
			if (fUpdateIndent && fRealEditor != null) {
				fRealEditor.updateSettings(fUpdateIndent);
			}
		}
		
		fUpdateCompleteConfig = false;
		fUpdateTextPresentation = false;
		fUpdateTabSize = false;
		fUpdateIndent = false;
	}
	
	public RCodeStyleSettings getRCodeStyle() {
		return fRCodeStyleCopy;
	}
	
	public IPreferenceAccess getPrefs() {
		return fSourceCoreAccess.getPrefs();
	}
	
}
