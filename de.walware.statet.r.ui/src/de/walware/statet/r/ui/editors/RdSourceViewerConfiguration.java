/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import static de.walware.statet.r.ui.text.rd.IRdTextTokens.COMMENT;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.PLATFORM_SPECIF;
import static de.walware.statet.r.ui.text.rd.IRdTextTokens.TASK_TAG;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

import de.walware.ecommons.ltk.ui.LTKUIPreferences;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.text.ICharPairMatcher;
import de.walware.ecommons.text.PairMatcher;
import de.walware.ecommons.text.ui.presentation.SingleTokenScanner;
import de.walware.ecommons.text.ui.settings.TextStyleManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.text.CommentScanner;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RdDocumentContentInfo;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.text.rd.RdCodeScanner;
import de.walware.statet.r.ui.text.rd.RdDoubleClickStrategy;


/**
 * Default Configuration for SourceViewer of R documentations.
 */
public class RdSourceViewerConfiguration extends SourceEditorViewerConfiguration
		implements ISettingsChangedHandler {
	
	
	private static final String[] CONTENT_TYPES= IRDocumentConstants.RDOC_CONTENT_TYPES.toArray(
			new String[IRDocumentConstants.RDOC_CONTENT_TYPES.size()] );
	
	private static final char[][] BRACKETS = { { '{', '}' } };
	
	
	private RdDoubleClickStrategy fDoubleClickStrategy;
	
	private IRCoreAccess fRCoreAccess;
	
	
	public RdSourceViewerConfiguration() {
		this(null, null, null, null);
	}
	
	public RdSourceViewerConfiguration(final ISourceEditor sourceEditor,
			final IRCoreAccess access,
			final IPreferenceStore preferenceStore, final TextStyleManager textStyles) {
		super(RdDocumentContentInfo.INSTANCE, sourceEditor);
		setCoreAccess(access);
		
		setup((preferenceStore != null) ? preferenceStore : RUIPlugin.getDefault().getEditorPreferenceStore(),
				LTKUIPreferences.getEditorDecorationPreferences(),
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
		setTextStyles(textStyles);
		fDoubleClickStrategy = new RdDoubleClickStrategy();
	}
	
	protected void setCoreAccess(final IRCoreAccess access) {
		fRCoreAccess = (access != null) ? access : RCore.getWorkbenchAccess();
	}
	
	
	@Override
	protected void initTextStyles() {
		setTextStyles(RUIPlugin.getDefault().getRdTextStyles());
	}
	
	@Override
	protected void initScanners() {
		final TextStyleManager textStyles= getTextStyles();
		
		addScanner(IRDocumentConstants.RDOC_DEFAULT_CONTENT_TYPE,
				new RdCodeScanner(textStyles) );
		addScanner(IRDocumentConstants.RDOC_COMMENT_CONTENT_TYPE,
				new CommentScanner(textStyles, COMMENT, TASK_TAG, fRCoreAccess.getPrefs()) );
		addScanner(IRDocumentConstants.RDOC_COMMENT_CONTENT_TYPE,
				new SingleTokenScanner(textStyles, PLATFORM_SPECIF ) );
	}
	
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return CONTENT_TYPES;
	}
	
	
	@Override
	public ICharPairMatcher getPairMatcher() {
		return new PairMatcher(BRACKETS, getDocumentContentInfo(),
					new String[] { IRDocumentConstants.RDOC_DEFAULT_CONTENT_TYPE }, '\\');
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
	
	
	@Override
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		return null;
	}
	
}
