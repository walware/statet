/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.ecommons.ltk.ui.sourceediting.EcoReconciler2;
import de.walware.ecommons.ltk.ui.sourceediting.EditorInformationProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistProcessor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverDescriptor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverRegistry.EffectiveHovers;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.text.ICharPairMatcher;
import de.walware.ecommons.text.IIndentSettings;
import de.walware.ecommons.ui.ColorManager;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.nico.ui.console.ConsolePageEditor;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.editors.REditor;
import de.walware.statet.r.internal.ui.editors.REditorInformationProvider;
import de.walware.statet.r.internal.ui.editors.REditorTextHover;
import de.walware.statet.r.internal.ui.editors.RQuickOutlineInformationProvider;
import de.walware.statet.r.internal.ui.editors.RReconcilingStrategy;
import de.walware.statet.r.ui.editors.REditorOptions;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;
import de.walware.statet.r.ui.text.r.RCodeScanner2;
import de.walware.statet.r.ui.text.r.RCommentScanner;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;
import de.walware.statet.r.ui.text.r.RInfixOperatorScanner;
import de.walware.statet.r.ui.text.r.RStringScanner;
import de.walware.statet.r.ui.text.r.RoxygenScanner;


/**
 * Default Configuration for SourceViewer of R code.
 */
public class RSourceViewerConfiguration extends SourceEditorViewerConfiguration {
	
	
	private static final String[] NONE_DEFAULT_CONTENT_TYPES = new String[] {
			IRDocumentPartitions.R_INFIX_OPERATOR,
			IRDocumentPartitions.R_STRING,
			IRDocumentPartitions.R_COMMENT,
			IRDocumentPartitions.R_ROXYGEN,
	};
	
	
	private RDoubleClickStrategy fDoubleClickStrategy;
	private RAutoEditStrategy fAutoEditStrategy;
	
	private final REditor fEditor;
	private IRCoreAccess fRCoreAccess;
	
	private boolean fHandleDefaultContentType;
	
	
	public RSourceViewerConfiguration(
			final IPreferenceStore store, final ColorManager colorManager) {
		this(null, null, store, colorManager);
	}
	
	public RSourceViewerConfiguration(final ISourceEditor sourceEditor, final IRCoreAccess access,
			final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		super(sourceEditor);
		setCoreAccess(access);
		fEditor = ((sourceEditor instanceof REditor) ? (REditor) sourceEditor : null);
		fHandleDefaultContentType = true;
		setup((preferenceStore != null) ? preferenceStore : RUIPlugin.getDefault().getEditorPreferenceStore(),
				colorManager,
				IStatetUIPreferenceConstants.EDITING_DECO_PREFERENCES,
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
		initScanners();
	}
	
	protected void setCoreAccess(final IRCoreAccess access) {
		fRCoreAccess = (access != null) ? access : RCore.getWorkbenchAccess();
	}
	
	protected void initScanners() {
		final IPreferenceStore store = getPreferences();
		final ColorManager colorManager = getColorManager();
		
		addScanner(IRDocumentPartitions.R_DEFAULT,
				new RCodeScanner2(colorManager, store) );
		addScanner(IRDocumentPartitions.R_INFIX_OPERATOR,
				new RInfixOperatorScanner(colorManager, store) );
		addScanner(IRDocumentPartitions.R_STRING,
				new RStringScanner(colorManager, store) );
		addScanner(IRDocumentPartitions.R_COMMENT,
				new RCommentScanner(colorManager, store, fRCoreAccess.getPrefs()) );
		addScanner(IRDocumentPartitions.R_ROXYGEN,
				new RoxygenScanner(colorManager, store, fRCoreAccess.getPrefs()) );
	}
	
	
	public void setHandleDefaultContentType(final boolean enable) {
		fHandleDefaultContentType = enable;
	}
	
	
	public IRCoreAccess getRCoreAccess() {
		return fRCoreAccess;
	}
	
	protected REditor getEditor() {
		return fEditor;
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
	public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
		return IRDocumentPartitions.R_PARTITIONING;
	}
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return IRDocumentPartitions.R_PARTITIONS;
	}
	
	@Override
	public void initPresentationReconciler(final PresentationReconciler reconciler) {
		{	final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(
					getScanner(IRDocumentPartitions.R_DEFAULT) );
			if (fHandleDefaultContentType) {
				reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT);
				reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT);
			}
			reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT_EXPL);
			reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT_EXPL);
		}
		for (final String contentType : NONE_DEFAULT_CONTENT_TYPES) {
			final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getScanner(contentType));
			reconciler.setDamager(dr, contentType);
			reconciler.setRepairer(dr, contentType);
		}
	}
	
	
	@Override
	public ICharPairMatcher createPairMatcher() {
		return new RBracketPairMatcher();
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		if (fDoubleClickStrategy == null) {
			fDoubleClickStrategy = new RDoubleClickStrategy();
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
		reconciler.addReconcilingStrategy(new RReconcilingStrategy());
		
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
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		if (getSourceEditor() != null) {
			final ContentAssist assistant = new ContentAssist();
			assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
			assistant.setRestoreCompletionProposalSize(DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "RContentAssist.Proposal.size")); //$NON-NLS-1$
			
			initDefaultContentAssist(assistant);
			return assistant;
		}
		return null;
	}
	
	public void initDefaultContentAssist(final ContentAssist assistant) {
		final ContentAssistComputerRegistry registry = RUIPlugin.getDefault().getREditorContentAssistRegistry();
		
		final ContentAssistProcessor codeProcessor = new RContentAssistProcessor(assistant,
				IRDocumentPartitions.R_DEFAULT_EXPL, registry, getSourceEditor());
		codeProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '$' });
		assistant.setContentAssistProcessor(codeProcessor, IRDocumentPartitions.R_DEFAULT_EXPL);
		if (fHandleDefaultContentType) {
			assistant.setContentAssistProcessor(codeProcessor, IRDocumentPartitions.R_DEFAULT);
		}
		
		final ContentAssistProcessor symbolProcessor = new RContentAssistProcessor(assistant,
				IRDocumentPartitions.R_QUOTED_SYMBOL, registry, getSourceEditor());
		assistant.setContentAssistProcessor(symbolProcessor, IRDocumentPartitions.R_QUOTED_SYMBOL);
		
		final ContentAssistProcessor stringProcessor = new RContentAssistProcessor(assistant,
				IRDocumentPartitions.R_STRING, registry, getSourceEditor());
		assistant.setContentAssistProcessor(stringProcessor, IRDocumentPartitions.R_STRING);
		
		final ContentAssistProcessor commentProcessor = new RContentAssistProcessor(assistant,
				IRDocumentPartitions.R_COMMENT, registry, getSourceEditor());
		assistant.setContentAssistProcessor(commentProcessor, IRDocumentPartitions.R_COMMENT);
		
		final ContentAssistProcessor roxygenProcessor = new RContentAssistProcessor(assistant,
				IRDocumentPartitions.R_ROXYGEN, registry, getSourceEditor());
		roxygenProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '@', '\\' });
		assistant.setContentAssistProcessor(roxygenProcessor, IRDocumentPartitions.R_ROXYGEN);
	}
	
	@Override
	protected IQuickAssistAssistant createQuickAssistant(final ISourceViewer sourceViewer) {
		final QuickAssistAssistant assistant = new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new RQuickAssistProcessor(fEditor));
		assistant.enableColoredLabels(true);
		return assistant;
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
	protected Map getHyperlinkDetectorTargets(final ISourceViewer sourceViewer) {
		final Map<String, Object> targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("de.walware.statet.r.editorHyperlinks.REditorTarget", getSourceEditor()); //$NON-NLS-1$
		return targets;
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
