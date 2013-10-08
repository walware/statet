/*******************************************************************************
 * Copyright (c) 2005-2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import static de.walware.statet.r.core.rsource.IRDocumentPartitions.RDOC_COMMENT;
import static de.walware.statet.r.core.rsource.IRDocumentPartitions.RDOC_DEFAULT;
import static de.walware.statet.r.core.rsource.IRDocumentPartitions.RDOC_DOCUMENT_PARTITIONING;
import static de.walware.statet.r.core.rsource.IRDocumentPartitions.RDOC_PARTITIONS;
import static de.walware.statet.r.core.rsource.IRDocumentPartitions.RDOC_PLATFORM_SPECIF;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.COMMENT;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.PLATFORM_SPECIF;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.TASK_TAG;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.text.ICharPairMatcher;
import de.walware.ecommons.text.PairMatcher;
import de.walware.ecommons.text.ui.presentation.SingleTokenScanner;
import de.walware.ecommons.ui.ColorManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.text.CommentScanner;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.text.rd.RdCodeScanner;
import de.walware.statet.r.ui.text.rd.RdDoubleClickStrategy;


/**
 * Default Configuration for SourceViewer of R documentations.
 */
public class RdSourceViewerConfiguration extends SourceEditorViewerConfiguration
		implements ISettingsChangedHandler {
	
	
	private static final char[][] BRACKETS = { { '{', '}' } };
	
	
	private RdDoubleClickStrategy fDoubleClickStrategy;
	
	private IRCoreAccess fRCoreAccess;
	
	
	public RdSourceViewerConfiguration(final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		this(null, null, preferenceStore, colorManager);
	}
	
	public RdSourceViewerConfiguration(final ISourceEditor sourceEditor, final IRCoreAccess access,
			final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		super(sourceEditor);
		setCoreAccess(access);
		
		setup(preferenceStore, colorManager,
				IStatetUIPreferenceConstants.EDITING_DECO_PREFERENCES,
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
		initScanners();
		fDoubleClickStrategy = new RdDoubleClickStrategy();
	}
	
	protected void setCoreAccess(final IRCoreAccess access) {
		fRCoreAccess = (access != null) ? access : RCore.getWorkbenchAccess();
	}
	
	protected void initScanners() {
		final IPreferenceStore store = getPreferences();
		final ColorManager colorManager = getColorManager();
		
		addScanner(RDOC_DEFAULT,
				new RdCodeScanner(colorManager, store) );
		addScanner(RDOC_COMMENT,
				new CommentScanner(colorManager, store, fRCoreAccess.getPrefs(),
						RUIPreferenceConstants.Rd.TS_GROUP_ID, COMMENT, TASK_TAG ) );
		addScanner(RDOC_PLATFORM_SPECIF,
				new SingleTokenScanner(colorManager, store,
						RUIPreferenceConstants.Rd.TS_GROUP_ID, PLATFORM_SPECIF ) );
	}
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return RDOC_PARTITIONS;
	}
	
	@Override
	public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
		return RDOC_DOCUMENT_PARTITIONING;
	}
	
	
	@Override
	public ICharPairMatcher getPairMatcher() {
		return new PairMatcher(BRACKETS,
					IRDocumentPartitions.RDOC_PARTITIONING_CONFIG,
					new String[] { IRDocumentPartitions.RDOC_DEFAULT }, '\\');
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		return fDoubleClickStrategy;
	}
	
	
	@Override
	public String[] getDefaultPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		return new String[] { "%", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		options.put(ISettingsChangedHandler.PREFERENCEACCESS_KEY, fRCoreAccess.getPrefs());
		super.handleSettingsChanged(groupIds, options);
	}
	
}
