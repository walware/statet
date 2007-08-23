/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.eclipsecommons.ui.text.EcoReconciler;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.ext.ui.editors.ContentAssistPreference;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.ext.ui.text.SingleTokenScanner;
import de.walware.statet.ext.ui.text.StatextTextScanner;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.ui.editors.RReconcilingStrategy;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesCompletionProcessor;
import de.walware.statet.r.ui.text.r.RCodeScanner;
import de.walware.statet.r.ui.text.r.RCommentScanner;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;
import de.walware.statet.r.ui.text.r.RInfixOperatorScanner;
import de.walware.statet.r.ui.text.r.RStringScanner;


/**
 * Default Configuration for SourceViewer of R code.
 */
public class RSourceViewerConfiguration extends StatextSourceViewerConfiguration {

	
	private RCodeScanner fCodeScanner;
	private RInfixOperatorScanner fInfixScanner;
	private CommentScanner fCommentScanner;
	private SingleTokenScanner fStringScanner;
	
	private RDoubleClickStrategy fDoubleClickStrategy;
	private RAutoEditStrategy fRAutoEditStrategy;
	protected ContentAssistant fContentAssistant;
	
	private REditor fEditor;
	private IEditorAdapter fEditorAdapter;
	private IRCoreAccess fRCoreAccess;

	
	public RSourceViewerConfiguration(
			IRCoreAccess rCoreAccess, IPreferenceStore store, ColorManager colorManager) {
		this(null, null, rCoreAccess, store, colorManager);
	}
	
	public RSourceViewerConfiguration(IEditorAdapter editor,
			IRCoreAccess rCoreAccess, IPreferenceStore store, ColorManager colorManager) {
		this(null, editor, rCoreAccess, store, colorManager);
	}
	
	public RSourceViewerConfiguration(REditor editor,
			IRCoreAccess rCoreAccess, IPreferenceStore preferenceStore, ColorManager colorManager) {
		this(editor, (IEditorAdapter) editor.getAdapter(IEditorAdapter.class),
				rCoreAccess, preferenceStore, colorManager);
	}
	
	protected RSourceViewerConfiguration(REditor editor, IEditorAdapter adapter,
			IRCoreAccess rCoreAccess, IPreferenceStore preferenceStore, ColorManager colorManager) {
		super();
		fRCoreAccess = rCoreAccess;
		if (fRCoreAccess == null) {
			fRCoreAccess = RCore.getWorkbenchAccess();
		}
		fEditor = editor;
		fEditorAdapter = adapter;
		setup(preferenceStore, colorManager);
		setScanners(initializeScanners());
	}
	
	
	protected IRCoreAccess getRCoreAccess() {
		return fRCoreAccess;
	}
	
	/**
	 * Initializes the scanners.
	 */
	protected StatextTextScanner[] initializeScanners() {
		IPreferenceStore store = getPreferences();
		ColorManager colorManager = getColorManager();
		fCodeScanner = new RCodeScanner(colorManager, store, fRCoreAccess.getPrefs());
		fInfixScanner = new RInfixOperatorScanner(colorManager, store);
		fCommentScanner = new RCommentScanner(colorManager, store, fRCoreAccess.getPrefs());
		fStringScanner = new RStringScanner(colorManager, store);
		return new StatextTextScanner[] { fCodeScanner, fInfixScanner, fCommentScanner, fStringScanner };
	}

	
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return IRDocumentPartitions.R_PARTITIONS;
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return IRDocumentPartitions.R_DOCUMENT_PARTITIONING;
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (fDoubleClickStrategy == null) {
			fDoubleClickStrategy = new RDoubleClickStrategy();
		}
		return fDoubleClickStrategy;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fCodeScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_DEFAULT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_DEFAULT);

		dr = new DefaultDamagerRepairer(fInfixScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_INFIX_OPERATOR);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_INFIX_OPERATOR);
		
		dr = new DefaultDamagerRepairer(fStringScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_STRING);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_STRING);

		dr = new DefaultDamagerRepairer(fCommentScanner);
		reconciler.setDamager(dr, IRDocumentPartitions.R_COMMENT);
		reconciler.setRepairer(dr, IRDocumentPartitions.R_COMMENT);

		return reconciler;
	}

	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return fRCoreAccess.getRCodeStyle().getTabSize();
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "#", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		String[] prefixes = getIndentPrefixesForTab(getTabWidth(sourceViewer));
		RCodeStyleSettings codeStyle = fRCoreAccess.getRCodeStyle();
		if (codeStyle.getIndentDefaultType() == IndentationType.SPACES) {
			for (int i = prefixes.length-2; i > 0; i--) {
				prefixes[i] = prefixes[i-1];
			}
			prefixes[0] = new String(RIndentUtil.repeat(' ', codeStyle.getIndentSpacesCount()));
		}
		return prefixes;
	}
	
	@Override
	public boolean handleSettingsChanged(Set<String> contexts, Object options) {
		if (contexts.contains(ContentAssistPreference.CONTEXT_ID)) {
			ContentAssistPreference.configure(fContentAssistant);
		}
		return super.handleSettingsChanged(contexts, fRCoreAccess.getPrefs());
	}
	
	
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor == null) { // at moment only for editors
			return null;
		}
		EcoReconciler reconciler = new EcoReconciler(fEditor);
		reconciler.setDelay(333);
		reconciler.addReconcilingStrategy(new RReconcilingStrategy());
		
		IReconcilingStrategy spellingStrategy = getSpellingStrategy(sourceViewer);
		if (spellingStrategy != null) {
			reconciler.addReconcilingStrategy(spellingStrategy);
		}
		
		return reconciler;
	}
	
	protected IReconcilingStrategy getSpellingStrategy(ISourceViewer sourceViewer) {
		if (!(fRCoreAccess.getPrefs().getPreferenceValue(REditorOptions.PREF_SPELLCHECKING_ENABLED)
				&& fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) ) {
			return null;
		}
		SpellingService spellingService = EditorsUI.getSpellingService();
		if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) == null) {
			return null;
		}
		return new SpellingReconcileStrategy(sourceViewer, spellingService);
	}
	
	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		if (fRCoreAccess.getPrefs().getPreferenceValue(REditorOptions.PREF_SPELLCHECKING_ENABLED)) {
			return super.getQuickAssistAssistant(sourceViewer);
		}
		return null;
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if (fEditorAdapter == null) {
			return super.getAutoEditStrategies(sourceViewer, contentType);
		}
		if (fRAutoEditStrategy == null) {
			fRAutoEditStrategy = new RAutoEditStrategy(fRCoreAccess, fEditorAdapter, fEditor);
			fInstallableModules.add(fRAutoEditStrategy);
		}
		return new IAutoEditStrategy[] { fRAutoEditStrategy };
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (fContentAssistant == null) {
			fContentAssistant = createContentAssistant(sourceViewer);
			if (fContentAssistant != null) {
				ContentAssistPreference.configure(fContentAssistant);
				fContentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
				fContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
				fContentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
			}
		}
		return fContentAssistant;
	}
	
	protected ContentAssistant createContentAssistant(ISourceViewer sourceViewer) {
		if (fEditor != null) {
			ContentAssistant contentAssistant = new ContentAssistant();
			REditorTemplatesCompletionProcessor processor = new REditorTemplatesCompletionProcessor(fEditor);
			
			contentAssistant.setDocumentPartitioning(IRDocumentPartitions.R_DOCUMENT_PARTITIONING);
			for (String contentType : getConfiguredContentTypes(sourceViewer)) {
				contentAssistant.setContentAssistProcessor(processor, contentType);
			}
			return contentAssistant;
		}
		return null;
	}
	
}
