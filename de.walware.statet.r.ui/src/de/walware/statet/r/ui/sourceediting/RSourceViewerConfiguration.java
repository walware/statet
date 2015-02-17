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

package de.walware.statet.r.ui.sourceediting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.ecommons.ltk.ui.LTKUIPreferences;
import de.walware.ecommons.ltk.ui.sourceediting.EcoReconciler2;
import de.walware.ecommons.ltk.ui.sourceediting.EditorInformationProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.sourceediting.SourceUnitReconcilingStrategy;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistProcessor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverDescriptor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverRegistry.EffectiveHovers;
import de.walware.ecommons.ltk.ui.sourceediting.assist.QuickAssistProcessor;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.text.ICharPairMatcher;
import de.walware.ecommons.text.IIndentSettings;
import de.walware.ecommons.text.core.sections.IDocContentSections;
import de.walware.ecommons.text.ui.presentation.SingleTokenScanner;
import de.walware.ecommons.text.ui.settings.TextStyleManager;
import de.walware.ecommons.ui.ColorManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.nico.ui.console.ConsolePageEditor;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RDocumentContentInfo;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.editors.REditor;
import de.walware.statet.r.internal.ui.editors.REditorInformationProvider;
import de.walware.statet.r.internal.ui.editors.REditorTextHover;
import de.walware.statet.r.internal.ui.editors.RQuickOutlineInformationProvider;
import de.walware.statet.r.ui.editors.REditorOptions;
import de.walware.statet.r.ui.text.r.IRTextTokens;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;
import de.walware.statet.r.ui.text.r.RCodeScanner2;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;
import de.walware.statet.r.ui.text.r.RInfixOperatorScanner;
import de.walware.statet.r.ui.text.r.RoxygenScanner;


/**
 * Default Configuration for SourceViewer of R code.
 */
public class RSourceViewerConfiguration extends SourceEditorViewerConfiguration {
	
	
	private static final String[] CONTENT_TYPES= IRDocumentConstants.R_CONTENT_TYPES.toArray(
			new String[IRDocumentConstants.R_CONTENT_TYPES.size()] );
	
	
	private RDoubleClickStrategy fDoubleClickStrategy;
	private RAutoEditStrategy fAutoEditStrategy;
	
	private IRCoreAccess fRCoreAccess;
	
	
	public RSourceViewerConfiguration(final IPreferenceStore store, final ColorManager colorManager) {
		this(RDocumentContentInfo.INSTANCE, null, null, store, null);
	}
	
	public RSourceViewerConfiguration(final IDocContentSections documentContentInfo,
			final ISourceEditor sourceEditor,
			final IRCoreAccess access,
			final IPreferenceStore preferenceStore, final TextStyleManager textStyles) {
		super(documentContentInfo, sourceEditor);
		setCoreAccess(access);
		setup((preferenceStore != null) ? preferenceStore : RUIPlugin.getDefault().getEditorPreferenceStore(),
				LTKUIPreferences.getEditorDecorationPreferences(),
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
		setTextStyles(textStyles);
	}
	
	protected void setCoreAccess(final IRCoreAccess access) {
		fRCoreAccess = (access != null) ? access : RCore.getWorkbenchAccess();
	}
	
	
	@Override
	protected void initTextStyles() {
		setTextStyles(RUIPlugin.getDefault().getRTextStyles());
	}
	
	@Override
	protected void initScanners() {
		final TextStyleManager textStyles= getTextStyles();
		
		addScanner(IRDocumentConstants.R_DEFAULT_CONTENT_TYPE,
				new RCodeScanner2(textStyles) );
		addScanner(IRDocumentConstants.R_INFIX_OPERATOR_CONTENT_TYPE,
				new RInfixOperatorScanner(textStyles) );
		addScanner(IRDocumentConstants.R_STRING_CONTENT_TYPE,
				new SingleTokenScanner(textStyles, IRTextTokens.STRING_KEY) );
		addScanner(IRDocumentConstants.R_COMMENT_CONTENT_TYPE,
				new CommentScanner(textStyles, IRTextTokens.COMMENT_KEY, IRTextTokens.TASK_TAG_KEY,
						fRCoreAccess.getPrefs()) );
		addScanner(IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE,
				new RoxygenScanner(textStyles, fRCoreAccess.getPrefs()) );
	}
	
	@Override
	protected ITokenScanner getScanner(String contentType) {
		if (contentType == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE) {
			contentType= IRDocumentConstants.R_STRING_CONTENT_TYPE;
		}
		return super.getScanner(contentType);
	}
	
	
	public IRCoreAccess getRCoreAccess() {
		return fRCoreAccess;
	}
	
	@Override
	public List<ISourceEditorAddon> getAddOns() {
		final List<ISourceEditorAddon> addons = super.getAddOns();
		if (fAutoEditStrategy != null) {
			addons.add(fAutoEditStrategy);
		}
		return addons;
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		options.put(ISettingsChangedHandler.PREFERENCEACCESS_KEY, fRCoreAccess.getPrefs());
		super.handleSettingsChanged(groupIds, options);
	}
	
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return CONTENT_TYPES;
	}
	
	
	@Override
	public ICharPairMatcher createPairMatcher() {
		return new RBracketPairMatcher(
				RHeuristicTokenScanner.create(getDocumentContentInfo()) );
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		if (fDoubleClickStrategy == null) {
			fDoubleClickStrategy= new RDoubleClickStrategy(
					RHeuristicTokenScanner.create(getDocumentContentInfo()) );
		}
		return fDoubleClickStrategy;
	}
	
	@Override
	public String[] getDefaultPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		return new String[] { "#", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	protected IIndentSettings getIndentSettings() {
		return fRCoreAccess.getRCodeStyle();
	}
	
	
	@Override
	public boolean isSmartInsertSupported() {
		return true;
	}
	
	@Override
	public boolean isSmartInsertByDefault() {
		return PreferencesUtil.getInstancePrefs().getPreferenceValue(
				REditorOptions.SMARTINSERT_BYDEFAULT_ENABLED_PREF );
	}
	
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		if (getSourceEditor() == null) {
			return super.getAutoEditStrategies(sourceViewer, contentType);
		}
		if (fAutoEditStrategy == null) {
			fAutoEditStrategy = createRAutoEditStrategy();
		}
		return new IAutoEditStrategy[] { fAutoEditStrategy };
	}
	
	protected RAutoEditStrategy createRAutoEditStrategy() {
		return new RAutoEditStrategy(fRCoreAccess, getSourceEditor());
	}
	
	
	@Override
	public IReconciler getReconciler(final ISourceViewer sourceViewer) {
		final ISourceEditor editor = getSourceEditor();
		if (!(editor instanceof SourceEditor1 || editor instanceof ConsolePageEditor)) {
			return null;
		}
		final EcoReconciler2 reconciler = new EcoReconciler2(editor);
		reconciler.setDelay(500);
		reconciler.addReconcilingStrategy(new SourceUnitReconcilingStrategy());
		
		if (editor instanceof REditor) {
			final IReconcilingStrategy spellingStrategy = getSpellingStrategy(sourceViewer);
			if (spellingStrategy != null) {
				reconciler.addReconcilingStrategy(spellingStrategy);
			}
		}
		
		return reconciler;
	}
	
	protected IReconcilingStrategy getSpellingStrategy(final ISourceViewer sourceViewer) {
		if (!(fRCoreAccess.getPrefs().getPreferenceValue(REditorOptions.PREF_SPELLCHECKING_ENABLED)
				&& fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) ) {
			return null;
		}
		final SpellingService spellingService = EditorsUI.getSpellingService();
		if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) == null) {
			return null;
		}
		return new SpellingReconcileStrategy(sourceViewer, spellingService);
	}
	
	
	@Override
	public void initContentAssist(final ContentAssist assistant) {
		final ContentAssistComputerRegistry registry = RUIPlugin.getDefault().getREditorContentAssistRegistry();
		
		final ContentAssistProcessor codeProcessor = new RContentAssistProcessor(assistant,
				IRDocumentConstants.R_DEFAULT_CONTENT_TYPE, registry, getSourceEditor());
		codeProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '$' });
		assistant.setContentAssistProcessor(codeProcessor, IRDocumentConstants.R_DEFAULT_CONTENT_TYPE);
		
		final ContentAssistProcessor symbolProcessor = new RContentAssistProcessor(assistant,
				IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE, registry, getSourceEditor());
		assistant.setContentAssistProcessor(symbolProcessor, IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE);
		
		final ContentAssistProcessor stringProcessor = new RContentAssistProcessor(assistant,
				IRDocumentConstants.R_STRING_CONTENT_TYPE, registry, getSourceEditor());
		assistant.setContentAssistProcessor(stringProcessor, IRDocumentConstants.R_STRING_CONTENT_TYPE);
		
		final ContentAssistProcessor commentProcessor = new RContentAssistProcessor(assistant,
				IRDocumentConstants.R_COMMENT_CONTENT_TYPE, registry, getSourceEditor());
		assistant.setContentAssistProcessor(commentProcessor, IRDocumentConstants.R_COMMENT_CONTENT_TYPE);
		
		final ContentAssistProcessor roxygenProcessor = new RContentAssistProcessor(assistant,
				IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE, registry, getSourceEditor());
		roxygenProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '@', '\\' });
		assistant.setContentAssistProcessor(roxygenProcessor, IRDocumentConstants.R_ROXYGEN_CONTENT_TYPE);
	}
	
	@Override
	protected QuickAssistProcessor createQuickAssistProcessor() {
		return new RQuickAssistProcessor(getSourceEditor());
	}
	
	@Override
	protected InfoHoverRegistry getInfoHoverRegistry() {
		return RUIPlugin.getDefault().getREditorInfoHoverRegistry();
	}
	
	@Override
	protected EditorInformationProvider getInformationProvider() {
		final ISourceEditor editor = getSourceEditor();
		if (editor != null) {
			return new REditorInformationProvider(editor);
		}
		return null;
	}
	
	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType, final int stateMask) {
		final EffectiveHovers effectiveHovers = getEffectiveHovers();
		if (effectiveHovers != null) {
			final InfoHoverDescriptor descriptor = effectiveHovers.getDescriptor(stateMask);
			if (descriptor != null) {
				return new REditorTextHover(descriptor, this);
			}
		}
		return null;
	}
	
	
	@Override
	protected void collectHyperlinkDetectorTargets(final Map<String, IAdaptable> targets,
			final ISourceViewer sourceViewer) {
		targets.put("de.walware.statet.r.editorHyperlinks.REditorTarget", getSourceEditor()); //$NON-NLS-1$
	}
	
	
	@Override
	protected IInformationProvider getQuickInformationProvider(final ISourceViewer sourceViewer,
			final int operation) {
		final ISourceEditor editor = getSourceEditor();
		if (editor == null) {
			return null;
		}
		switch (operation) {
		case SourceEditorViewer.SHOW_SOURCE_OUTLINE:
			return new RQuickOutlineInformationProvider(editor, operation);
		default:
			return null;
		}
	}
	
}
