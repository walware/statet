/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.eclipsecommons.ui.text.EcoReconciler;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.base.ui.sourceeditors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.ext.ui.text.SingleTokenScanner;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.ui.editors.RReconcilingStrategy;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesCompletionProcessor;
import de.walware.statet.r.ui.text.r.RCodeScanner2;
import de.walware.statet.r.ui.text.r.RCommentScanner;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;
import de.walware.statet.r.ui.text.r.RInfixOperatorScanner;
import de.walware.statet.r.ui.text.r.RStringScanner;


/**
 * Default Configuration for SourceViewer of R code.
 */
public class RSourceViewerConfiguration extends StatextSourceViewerConfiguration {
	
	
	protected RCodeScanner2 fCodeScanner;
	protected RInfixOperatorScanner fInfixScanner;
	protected CommentScanner fCommentScanner;
	protected SingleTokenScanner fStringScanner;
	
	private RDoubleClickStrategy fDoubleClickStrategy;
	private RAutoEditStrategy fRAutoEditStrategy;
	
	private REditor fEditor;
	private IRCoreAccess fRCoreAccess;
	
	
	public RSourceViewerConfiguration(
			final IRCoreAccess rCoreAccess, final IPreferenceStore store, final ColorManager colorManager) {
		this(null, null, null, rCoreAccess, store, colorManager);
	}
	
	public RSourceViewerConfiguration(final IEditorAdapter editor,
			final IRCoreAccess rCoreAccess, final IPreferenceStore store, final ColorManager colorManager) {
		this(null, null, editor, rCoreAccess, store, colorManager);
	}
	
	public RSourceViewerConfiguration(final REditor editor,
			final IRCoreAccess rCoreAccess, final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		this(null, editor, (IEditorAdapter) editor.getAdapter(IEditorAdapter.class),
				rCoreAccess, preferenceStore, colorManager);
	}
	
	protected RSourceViewerConfiguration(final StatextSourceViewerConfiguration parent,
			final REditor editor, final IEditorAdapter adapter,
			final IRCoreAccess rCoreAccess, final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		super(adapter);
		fRCoreAccess = rCoreAccess;
		if (fRCoreAccess == null) {
			fRCoreAccess = RCore.getWorkbenchAccess();
		}
		fEditor = editor;
		setup(preferenceStore, colorManager);
		setScanners(initializeScanners());
	}
	
	
	public IRCoreAccess getRCoreAccess() {
		return fRCoreAccess;
	}
	
	protected IEditorAdapter getEditorAdapter() {
		return fEditorAdapter;
	}
	
	protected REditor getEditor() {
		return fEditor;
	}
	
	/**
	 * Initializes the scanners.
	 */
	protected ITokenScanner[] initializeScanners() {
		final IPreferenceStore store = getPreferences();
		final ColorManager colorManager = getColorManager();
		fCodeScanner = new RCodeScanner2(colorManager, store);
		fInfixScanner = new RInfixOperatorScanner(colorManager, store);
		fCommentScanner = new RCommentScanner(colorManager, store, fRCoreAccess.getPrefs());
		fStringScanner = new RStringScanner(colorManager, store);
		return new ITokenScanner[] { fCodeScanner, fInfixScanner, fCommentScanner, fStringScanner };
	}
	
	
	@Override
	public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
		return IRDocumentPartitions.R_DOCUMENT_PARTITIONING;
	}
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return IRDocumentPartitions.R_PARTITIONS;
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		if (fDoubleClickStrategy == null) {
			fDoubleClickStrategy = new RDoubleClickStrategy();
		}
		return fDoubleClickStrategy;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
		final PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		initDefaultPresentationReconciler(reconciler, true);
		
		return reconciler;
	}
	
	public void initDefaultPresentationReconciler(final PresentationReconciler reconciler,
			final boolean handleDefaultContentType) {
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fCodeScanner);
		if (handleDefaultContentType) {
			reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT);
			reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT);
		}
		reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT_EXPL);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT_EXPL);
		
		dr = new DefaultDamagerRepairer(fInfixScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_INFIX_OPERATOR);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_INFIX_OPERATOR);
		
		dr = new DefaultDamagerRepairer(fStringScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_STRING);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_STRING);
		
		dr = new DefaultDamagerRepairer(fCommentScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_COMMENT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_COMMENT);
	}
	
	@Override
	public int getTabWidth(final ISourceViewer sourceViewer) {
		return fRCoreAccess.getRCodeStyle().getTabSize();
	}
	
	@Override
	public String[] getDefaultPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		return new String[] { "#", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String[] getIndentPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		final String[] prefixes = getIndentPrefixesForTab(getTabWidth(sourceViewer));
		final RCodeStyleSettings codeStyle = fRCoreAccess.getRCodeStyle();
		if (codeStyle.getIndentDefaultType() == IndentationType.SPACES) {
			for (int i = prefixes.length-2; i > 0; i--) {
				prefixes[i] = prefixes[i-1];
			}
			prefixes[0] = new String(RIndentUtil.repeat(' ', codeStyle.getIndentSpacesCount()));
		}
		return prefixes;
	}
	
	@Override
	public boolean handleSettingsChanged(final Set<String> contexts, final Object options) {
		return super.handleSettingsChanged(contexts, fRCoreAccess.getPrefs());
	}
	
	
	@Override
	public IReconciler getReconciler(final ISourceViewer sourceViewer) {
		if (fEditor == null) { // at moment only for editors
			return null;
		}
		final EcoReconciler reconciler = new EcoReconciler(fEditor);
		reconciler.setDelay(500);
		reconciler.addReconcilingStrategy(new RReconcilingStrategy());
		
		final IReconcilingStrategy spellingStrategy = getSpellingStrategy(sourceViewer);
		if (spellingStrategy != null) {
			reconciler.addReconcilingStrategy(spellingStrategy);
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
	public IQuickAssistAssistant getQuickAssistAssistant(final ISourceViewer sourceViewer) {
		if (fRCoreAccess.getPrefs().getPreferenceValue(REditorOptions.PREF_SPELLCHECKING_ENABLED)) {
			return super.getQuickAssistAssistant(sourceViewer);
		}
		return null;
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		if (fEditorAdapter == null) {
			return super.getAutoEditStrategies(sourceViewer, contentType);
		}
		if (fRAutoEditStrategy == null) {
			fRAutoEditStrategy = createRAutoEditStrategy();
			fEditorAdapter.install(fRAutoEditStrategy);
		}
		return new IAutoEditStrategy[] { fRAutoEditStrategy };
	}
	
	protected RAutoEditStrategy createRAutoEditStrategy() {
		return new RAutoEditStrategy(fRCoreAccess, fEditorAdapter, fEditor);
	}
	
	@Override
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		if (fEditor != null) {
			final ContentAssistant assistant = new ContentAssistant();
			final REditorTemplatesCompletionProcessor processor = new REditorTemplatesCompletionProcessor(fEditor);
			
			assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
			for (final String contentType : getConfiguredContentTypes(sourceViewer)) {
				assistant.setContentAssistProcessor(processor, contentType);
			}
			return assistant;
		}
		return null;
	}
	
}
