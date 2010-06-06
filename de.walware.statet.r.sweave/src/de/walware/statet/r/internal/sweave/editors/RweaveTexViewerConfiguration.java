/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import de.walware.ecommons.ltk.ui.sourceediting.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.text.PairMatcher;
import de.walware.ecommons.text.ui.EcoReconciler;
import de.walware.ecommons.ui.ColorManager;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.text.CommentScanner;

import net.sourceforge.texlipse.TexlipsePlugin;
import net.sourceforge.texlipse.editor.ITexDocumentConstants;
import net.sourceforge.texlipse.editor.TexDoubleClickStrategy;
import net.sourceforge.texlipse.editor.scanner.TexMathScanner;
import net.sourceforge.texlipse.editor.scanner.TexScanner;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.internal.sweave.RweaveTexReconcilingStrategy;
import de.walware.statet.r.sweave.text.RChunkControlCodeScanner;
import de.walware.statet.r.sweave.text.RweaveChunkHeuristicScanner;
import de.walware.statet.r.sweave.text.RweaveTexBracketPairMatcher;
import de.walware.statet.r.ui.editors.RAutoEditStrategy;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;


/**
 * Default Configuration for SourceViewer of Sweave (LaTeX/R) code.
 */
public class RweaveTexViewerConfiguration extends SourceEditorViewerConfiguration {
	
	
	private static class RChunkAutoEditStrategy extends RAutoEditStrategy {
		
		public RChunkAutoEditStrategy(final IRCoreAccess coreAccess, final ISourceEditor sourceEditor) {
			super(coreAccess, sourceEditor);
		}
		
		@Override
		protected RHeuristicTokenScanner createScanner() {
			return new RweaveChunkHeuristicScanner();
		}
		
		@Override
		protected IRegion getValidRange(final int offset, final int c) {
			final ITypedRegion cat = Rweave.R_TEX_CAT_UTIL.getCat(getDocument(), offset);
			if (cat.getType() == Rweave.R_CAT) {
				return cat;
			}
			if (cat.getType() == Rweave.CONTROL_CAT) {
				switch (c) {
				case '(':
				case '[':
				case '{':
				case '%':
				case '\"':
				case '\'':
					return cat;
				}
			}
			return null;
		}
		
	}
	
	private static class RChunkViewerConfiguration extends RSourceViewerConfiguration {
		
		public RChunkViewerConfiguration(
				final ISourceEditor sourceEditor,
				final IRCoreAccess coreAccess, final IPreferenceStore preferenceStore, final ColorManager colorManager) {
			super(sourceEditor, null, coreAccess, preferenceStore, colorManager);
		}
		
		@Override
		public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
			return Rweave.R_TEX_PARTITIONING;
		}
		
		public CommentScanner getCommentScanner() {
			return fCommentScanner;
		}
		
		@Override
		protected RAutoEditStrategy createRAutoEditStrategy() {
			return new RChunkAutoEditStrategy(getRCoreAccess(), getSourceEditor());
		}
		
	}
	
	
	private RChunkViewerConfiguration fRConfig;
	
	private RChunkControlCodeScanner fChunkControlScanner;
	
	private net.sourceforge.texlipse.editor.ColorManager fTexColorManager;
	private TexScanner fTexDefaultScanner;
	private TexMathScanner fTexMathScanner;
	private ITokenScanner fTexVerbatimScanner;
	
	private PairMatcher fPairMatcher;
	private ITextDoubleClickStrategy fRDoubleClickStrategy;
	private ITextDoubleClickStrategy fTexDoubleClickStrategy;
	
	
	public RweaveTexViewerConfiguration(final ISourceEditor sourceEditor,
			final IRCoreAccess rCoreAccess, final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		super(sourceEditor);
		fRConfig = new RChunkViewerConfiguration(sourceEditor, rCoreAccess, preferenceStore, colorManager);
		fRConfig.setHandleDefaultContentType(false);
		
		setup(preferenceStore, colorManager,
				IStatetUIPreferenceConstants.EDITING_DECO_PREFERENCES,
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
		setScanners(createScanners());
	}
	
	protected ITokenScanner[] createScanners() {
		final IPreferenceStore store = getPreferences();
		final ColorManager colorManager = getColorManager();
		
		fChunkControlScanner = new RChunkControlCodeScanner(colorManager, store);
		
		fTexColorManager = TexlipsePlugin.getDefault().getColorManager();
		fTexMathScanner = new TexMathScanner(fTexColorManager);
		fTexDefaultScanner = new TexScanner(fTexColorManager);
		final RuleBasedScanner verbatimScanner = new RuleBasedScanner ();
		verbatimScanner.setDefaultReturnToken(new Token(new TextAttribute(
				fTexColorManager.getColor(net.sourceforge.texlipse.editor.ColorManager.VERBATIM),
				null,
				fTexColorManager.getStyle(net.sourceforge.texlipse.editor.ColorManager.VERBATIM_STYLE))));
		fTexVerbatimScanner = verbatimScanner;
		
		return new ITokenScanner[] {
				fChunkControlScanner,
		};
	}
	
	
	@Override
	public List<ISourceEditorAddon> getAddOns() {
		final List<ISourceEditorAddon> addOns = super.getAddOns();
		addOns.addAll(fRConfig.getAddOns());
		return addOns;
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		fRConfig.handleSettingsChanged(groupIds, options);
		super.handleSettingsChanged(groupIds, options);
	}
	
	
	@Override
	public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
		return Rweave.R_TEX_PARTITIONING;
	}
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return Rweave.ALL_PARTITION_TYPES;
	}
	
	
	@Override
	public PairMatcher getPairMatcher() {
		if (fPairMatcher == null) {
			fPairMatcher = new RweaveTexBracketPairMatcher();
		}
		return fPairMatcher;
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		if (Rweave.R_PARTITION_CONSTRAINT.matches(contentType) 
				|| Rweave.CHUNK_CONTROL_PARTITION_CONSTRAINT.matches(contentType)) {
			if (fRDoubleClickStrategy == null) {
				fRDoubleClickStrategy = new RDoubleClickStrategy(Rweave.R_TEX_PARTITIONING);
			}
			return fRDoubleClickStrategy;
		}
		else {
			if (fTexDoubleClickStrategy == null) {
				fTexDoubleClickStrategy = new TexDoubleClickStrategy(Rweave.R_TEX_PARTITIONING);
			}
			return fTexDoubleClickStrategy;
		}
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
		final PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fChunkControlScanner);
		reconciler.setDamager(dr, Rweave.CHUNK_CONTROL_CONTENT_TYPE);
		reconciler.setRepairer(dr, Rweave.CHUNK_CONTROL_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(fRConfig.getCommentScanner());
		reconciler.setDamager(dr, Rweave.CHUNK_COMMENT_CONTENT_TYPE);
		reconciler.setRepairer(dr, Rweave.CHUNK_COMMENT_CONTENT_TYPE);
		
		fRConfig.initDefaultPresentationReconciler(reconciler);
		
		dr = new DefaultDamagerRepairer(fTexDefaultScanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(fTexMathScanner);
		reconciler.setDamager(dr, ITexDocumentConstants.TEX_MATH_CONTENT_TYPE);
		reconciler.setRepairer(dr, ITexDocumentConstants.TEX_MATH_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(fTexVerbatimScanner);
		reconciler.setDamager(dr, ITexDocumentConstants.TEX_VERBATIM_CONTENT_TYPE);
		reconciler.setRepairer(dr, ITexDocumentConstants.TEX_VERBATIM_CONTENT_TYPE);
		
		dr = new DefaultDamagerRepairer(fRConfig.getCommentScanner());
		reconciler.setDamager(dr, ITexDocumentConstants.TEX_COMMENT_CONTENT_TYPE);
		reconciler.setRepairer(dr, ITexDocumentConstants.TEX_COMMENT_CONTENT_TYPE);
		
		return reconciler;
	}
	
	@Override
	public int getTabWidth(final ISourceViewer sourceViewer) {
		return fRConfig.getTabWidth(sourceViewer);
	}
	
	@Override
	public String[] getDefaultPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		if (Rweave.R_PARTITION_CONSTRAINT.matches(contentType)) {
			return fRConfig.getDefaultPrefixes(sourceViewer, contentType);
		}
		return new String[] { "%", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String[] getIndentPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		if (Rweave.CHUNK_CONTROL_PARTITION_CONSTRAINT.matches(contentType)) {
			return new String[0];
		}
		if (Rweave.R_PARTITION_CONSTRAINT.matches(contentType)) {
			return fRConfig.getIndentPrefixes(sourceViewer, contentType);
		}
		return super.getIndentPrefixes(sourceViewer, contentType);
	}
	
	
	@Override
	public IReconciler getReconciler(final ISourceViewer sourceViewer) {
		if (!(getSourceEditor() instanceof ITextEditor)) {
			return null;
		}
		final EcoReconciler reconciler = new EcoReconciler((ITextEditor) getSourceEditor());
		reconciler.setDelay(500);
		reconciler.addReconcilingStrategy(new RweaveTexReconcilingStrategy());
		
		final IReconcilingStrategy spellingStrategy = getSpellingStrategy(sourceViewer);
		if (spellingStrategy != null) {
			reconciler.addReconcilingStrategy(spellingStrategy);
		}
		
		return reconciler;
	}
	
	protected IReconcilingStrategy getSpellingStrategy(final ISourceViewer sourceViewer) {
		if (!(fRConfig.getRCoreAccess().getPrefs().getPreferenceValue(SweaveEditorOptions.PREF_SPELLCHECKING_ENABLED)
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
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		if (Rweave.R_PARTITION_CONSTRAINT.matches(contentType)) {
			return fRConfig.getAutoEditStrategies(sourceViewer, contentType);
		}
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}
	
	@Override
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		if (!(getSourceEditor() instanceof ITextEditor)) {
			return null;
		}
		final ContentAssist assistant = new ContentAssist();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		final RChunkTemplatesCompletionProcessor controlProcessor = new RChunkTemplatesCompletionProcessor(getSourceEditor());
		assistant.setContentAssistProcessor(controlProcessor, Rweave.TEX_DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(controlProcessor, Rweave.CHUNK_CONTROL_CONTENT_TYPE);
		
		fRConfig.initDefaultContentAssist(assistant);
		
		return assistant;
	}
	
	@Override
	public boolean isSmartInsertSupported() {
		return true;
	}
	
}
