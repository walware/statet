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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITypedRegion;
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

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.ui.sourceediting.EcoReconciler2;
import de.walware.ecommons.ltk.ui.sourceediting.EditorInformationProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistCategory;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistProcessor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IContentAssistComputer;
import de.walware.ecommons.text.ICharPairMatcher;
import de.walware.ecommons.ui.ColorManager;

import de.walware.docmlet.tex.core.ITexCoreAccess;
import de.walware.docmlet.tex.core.TexCore;
import de.walware.docmlet.tex.core.text.ITexDocumentConstants;
import de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner;
import de.walware.docmlet.tex.ui.sourceediting.LtxViewerConfiguration;
import de.walware.docmlet.tex.ui.text.LtxDoubleClickStrategy;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.text.CommentScanner;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.sweave.editors.LtxRweaveInformationProvider;
import de.walware.statet.r.internal.sweave.editors.LtxRweaveQuickAssistProcessor;
import de.walware.statet.r.internal.sweave.editors.RChunkTemplatesCompletionComputer;
import de.walware.statet.r.internal.sweave.editors.SweaveEditorOptions;
import de.walware.statet.r.sweave.ITexRweaveCoreAccess;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;
import de.walware.statet.r.sweave.text.LtxRweaveBracketPairMatcher;
import de.walware.statet.r.sweave.text.LtxRweaveSwitch;
import de.walware.statet.r.sweave.text.RChunkControlCodeScanner;
import de.walware.statet.r.sweave.text.Rweave;
import de.walware.statet.r.sweave.text.RweaveChunkHeuristicScanner;
import de.walware.statet.r.ui.sourceediting.RAutoEditStrategy;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.text.r.RDoubleClickStrategy;


/**
 * Default Configuration for SourceViewer of Sweave (LaTeX/R) code.
 */
public class LtxRweaveViewerConfiguration extends SourceEditorViewerConfiguration {
	
	
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
			final ITypedRegion cat= Rweave.R_TEX_CAT_UTIL.getCat(getDocument(), offset);
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
			super(sourceEditor, coreAccess, preferenceStore, colorManager);
		}
		
		@Override
		protected void setCoreAccess(final IRCoreAccess access) {
			super.setCoreAccess(access);
		}
		
		@Override
		public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
			return Rweave.LTX_R_PARTITIONING;
		}
		
		@Override
		public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
			return Rweave.R_PARTITION_TYPES;
		}
		
		public CommentScanner getCommentScanner() {
			return (CommentScanner) getScanner(IRDocumentPartitions.R_COMMENT);
		}
		
		@Override
		protected RAutoEditStrategy createRAutoEditStrategy() {
			return new RChunkAutoEditStrategy(getRCoreAccess(), getSourceEditor());
		}
		
		@Override
		protected EditorInformationProvider getInformationProvider() {
			return super.getInformationProvider();
		}
		
	}
	
	private static class TexChunkViewerConfiguration extends LtxViewerConfiguration {
		
		public TexChunkViewerConfiguration(final ISourceEditor editor,
				final ITexCoreAccess texCoreAccess, final IPreferenceStore preferenceStore, final ColorManager colorManager) {
			super(editor, texCoreAccess, preferenceStore, colorManager);
		}
		
		@Override
		protected void setCoreAccess(final ITexCoreAccess access) {
			super.setCoreAccess(access);
		}
		
		@Override
		public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
			return Rweave.LTX_R_PARTITIONING;
		}
		
	}
	
	
	private final TexChunkViewerConfiguration fTexConfig;
	private final RChunkViewerConfiguration fRConfig;
	
	private ITexRweaveCoreAccess fCoreAccess;
	
	private ITextDoubleClickStrategy fTexDoubleClickStrategy;
	private ITextDoubleClickStrategy fRDoubleClickStrategy;
	
	
	public LtxRweaveViewerConfiguration(
			final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		this(null, null, preferenceStore, colorManager);
	}
	
	public LtxRweaveViewerConfiguration(final ISourceEditor sourceEditor,
			final ITexRweaveCoreAccess coreAccess,
			final IPreferenceStore preferenceStore, final ColorManager colorManager) {
		super(sourceEditor);
		this.fCoreAccess= (coreAccess != null) ? coreAccess : new TexRweaveCoreAccess(
				TexCore.getWorkbenchAccess(), RCore.getWorkbenchAccess() );
		this.fRConfig= new RChunkViewerConfiguration(sourceEditor, this.fCoreAccess, preferenceStore, colorManager);
		this.fRConfig.setHandleDefaultContentType(false);
		this.fTexConfig= new TexChunkViewerConfiguration(sourceEditor, this.fCoreAccess, preferenceStore, colorManager);
		
		setup((preferenceStore != null) ? preferenceStore : SweavePlugin.getDefault().getEditorTexRPreferenceStore(),
				colorManager,
				IStatetUIPreferenceConstants.EDITING_DECO_PREFERENCES,
				IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES );
		initScanners();
	}
	
	protected void initScanners() {
		final IPreferenceStore store= getPreferences();
		final ColorManager colorManager= getColorManager();
		
		addScanner(Rweave.CHUNK_CONTROL_CONTENT_TYPE,
				new RChunkControlCodeScanner(colorManager, store) );
	}
	
	protected void setCoreAccess(final ITexRweaveCoreAccess coreAccess) {
		this.fCoreAccess= (coreAccess != null) ? coreAccess : new TexRweaveCoreAccess(
				TexCore.getWorkbenchAccess(), RCore.getWorkbenchAccess() );
		this.fRConfig.setCoreAccess(this.fCoreAccess);
		this.fTexConfig.setCoreAccess(this.fCoreAccess);
	}
	
	
	@Override
	public List<ISourceEditorAddon> getAddOns() {
		final List<ISourceEditorAddon> addOns= super.getAddOns();
		addOns.addAll(this.fTexConfig.getAddOns());
		addOns.addAll(this.fRConfig.getAddOns());
		return addOns;
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		this.fRConfig.handleSettingsChanged(groupIds, options);
		this.fTexConfig.handleSettingsChanged(groupIds, options);
		super.handleSettingsChanged(groupIds, options);
	}
	
	
	@Override
	public String getConfiguredDocumentPartitioning(final ISourceViewer sourceViewer) {
		return Rweave.LTX_R_PARTITIONING;
	}
	
	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		return Rweave.ALL_PARTITION_TYPES;
	}
	
	@Override
	protected void initPresentationReconciler(final PresentationReconciler reconciler) {
		{	final DefaultDamagerRepairer dr= new DefaultDamagerRepairer(
					getScanner(Rweave.CHUNK_CONTROL_CONTENT_TYPE) );
			reconciler.setDamager(dr, Rweave.CHUNK_CONTROL_CONTENT_TYPE);
			reconciler.setRepairer(dr, Rweave.CHUNK_CONTROL_CONTENT_TYPE);
		}
		{	final DefaultDamagerRepairer dr= new DefaultDamagerRepairer(this.fRConfig.getCommentScanner());
			reconciler.setDamager(dr, Rweave.CHUNK_COMMENT_CONTENT_TYPE);
			reconciler.setRepairer(dr, Rweave.CHUNK_COMMENT_CONTENT_TYPE);
		}
		this.fRConfig.initPresentationReconciler(reconciler);
		this.fTexConfig.initPresentationReconciler(reconciler);
	}
	
	
	@Override
	public ICharPairMatcher createPairMatcher() {
		return new LtxRweaveBracketPairMatcher();
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
		switch (LtxRweaveSwitch.get(contentType)) {
		case LTX:
			if (this.fTexDoubleClickStrategy == null) {
				this.fTexDoubleClickStrategy= new LtxDoubleClickStrategy(
						new LtxHeuristicTokenScanner(Rweave.LTX_PARTITIONING_CONFIG) );
			}
			return this.fTexDoubleClickStrategy;
		case R:
		case CHUNK_CONTROL:
			if (this.fRDoubleClickStrategy == null) {
				final RweaveChunkHeuristicScanner scanner= new RweaveChunkHeuristicScanner();
				this.fRDoubleClickStrategy= new RDoubleClickStrategy(scanner,
						LtxRweaveBracketPairMatcher.createRChunkPairMatcher(scanner) );
			}
			return this.fRDoubleClickStrategy;
		default:
			return null;
		}
	}
	
	
	@Override
	public int getTabWidth(final ISourceViewer sourceViewer) {
		return this.fTexConfig.getTabWidth(sourceViewer);
	}
	
	@Override
	public String[] getDefaultPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		if (Rweave.R_PARTITION_CONSTRAINT.matches(contentType)) {
			return this.fRConfig.getDefaultPrefixes(sourceViewer, contentType);
		}
		return this.fTexConfig.getDefaultPrefixes(sourceViewer, contentType);
	}
	
	@Override
	public String[] getIndentPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		switch (LtxRweaveSwitch.get(contentType)) {
		case LTX:
			return this.fTexConfig.getIndentPrefixes(sourceViewer, contentType);
		case R:
			return this.fRConfig.getIndentPrefixes(sourceViewer, contentType);
		default:
			return new String[0];
		}
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		switch (LtxRweaveSwitch.get(contentType)) {
		case LTX:
			return this.fTexConfig.getAutoEditStrategies(sourceViewer, contentType);
		case R:
			return this.fRConfig.getAutoEditStrategies(sourceViewer, contentType);
		default:
			return new IAutoEditStrategy[0];
		}
	}
	
	
	protected IReconcilingStrategy getSpellingStrategy(final ISourceViewer sourceViewer) {
		if (!(this.fRConfig.getRCoreAccess().getPrefs().getPreferenceValue(SweaveEditorOptions.PREF_SPELLCHECKING_ENABLED)
				&& this.fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED)) ) {
			return null;
		}
		final SpellingService spellingService= EditorsUI.getSpellingService();
		if (spellingService.getActiveSpellingEngineDescriptor(this.fPreferenceStore) == null) {
			return null;
		}
		return new SpellingReconcileStrategy(sourceViewer, spellingService);
	}
	
	
	@Override
	protected ContentAssistant createContentAssistant(final ISourceViewer sourceViewer) {
		if (getSourceEditor() == null) {
			return null;
		}
		final RChunkTemplatesCompletionComputer chunkComputer= new RChunkTemplatesCompletionComputer();
		
		final ContentAssist assistant= (ContentAssist) this.fTexConfig.getContentAssistant(sourceViewer);
		
		final ContentAssistProcessor texProcessor= (ContentAssistProcessor) assistant.getContentAssistProcessor(ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE);
		texProcessor.addCategory(new ContentAssistCategory(ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE,
				new ConstArrayList<IContentAssistComputer>(chunkComputer)));
		texProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '\\', '<' });
		
		final ContentAssistProcessor mathProcessor= (ContentAssistProcessor) assistant.getContentAssistProcessor(ITexDocumentConstants.LTX_MATH_CONTENT_TYPE);
		mathProcessor.addCategory(new ContentAssistCategory(ITexDocumentConstants.LTX_MATH_CONTENT_TYPE,
				new ConstArrayList<IContentAssistComputer>(chunkComputer)));
		mathProcessor.setCompletionProposalAutoActivationCharacters(new char[] { '\\', '<' });
		
		this.fRConfig.initDefaultContentAssist(assistant);
		
		final ContentAssistProcessor controlProcessor= new ContentAssistProcessor(assistant,
				Rweave.CHUNK_CONTROL_CONTENT_TYPE, SweavePlugin.getDefault().getTexEditorContentAssistRegistry(), getSourceEditor());
		controlProcessor.addCategory(new ContentAssistCategory(Rweave.CHUNK_CONTROL_CONTENT_TYPE,
				new ConstArrayList<IContentAssistComputer>(chunkComputer)));
		assistant.setContentAssistProcessor(controlProcessor, Rweave.CHUNK_CONTROL_CONTENT_TYPE);
		
		return assistant;
	}
	
	@Override
	protected IQuickAssistAssistant createQuickAssistant(final ISourceViewer sourceViewer) {
		if (getSourceEditor() == null) {
			return null;
		}
		final QuickAssistAssistant assistant= new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new LtxRweaveQuickAssistProcessor(getSourceEditor()));
		assistant.enableColoredLabels(true);
		return assistant;
	}
	
	
	@Override
	public int[] getConfiguredTextHoverStateMasks(final ISourceViewer sourceViewer, final String contentType) {
		switch (LtxRweaveSwitch.get(contentType)) {
		case LTX:
			return this.fTexConfig.getConfiguredTextHoverStateMasks(sourceViewer, contentType);
		case R:
			return this.fRConfig.getConfiguredTextHoverStateMasks(sourceViewer, contentType);
		default:
			return null;
		}
	}
	
	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType, final int stateMask) {
		switch (LtxRweaveSwitch.get(contentType)) {
		case LTX:
			return this.fTexConfig.getTextHover(sourceViewer, contentType, stateMask);
		case R:
			return this.fRConfig.getTextHover(sourceViewer, contentType, stateMask);
		default:
			return null;
		}
	}
	
	@Override
	protected IInformationProvider getInformationProvider() {
		return new LtxRweaveInformationProvider(this.fRConfig.getInformationProvider());
	}
	
	
	@Override
	public IReconciler getReconciler(final ISourceViewer sourceViewer) {
		final ISourceEditor editor= getSourceEditor();
		if (!(editor instanceof SourceEditor1)) {
			return null;
		}
		final EcoReconciler2 reconciler= (EcoReconciler2) this.fTexConfig.getReconciler(sourceViewer);
		if (reconciler != null) {
			final IReconcilingStrategy spellingStrategy= getSpellingStrategy(sourceViewer);
			if (spellingStrategy != null) {
				reconciler.addReconcilingStrategy(spellingStrategy);
			}
		}
		return reconciler;
	}
	
	@Override
	protected Map getHyperlinkDetectorTargets(final ISourceViewer sourceViewer) {
		final Map<String, Object> targets= super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("de.walware.docmlet.tex.editorHyperlinks.TexEditorTarget", getSourceEditor()); //$NON-NLS-1$
		targets.put("de.walware.statet.r.editorHyperlinks.REditorTarget", getSourceEditor()); //$NON-NLS-1$
		return targets;
	}
	
	@Override
	public boolean isSmartInsertSupported() {
		return true;
	}
	
	@Override
	public boolean isSmartInsertByDefault() {
		return this.fTexConfig.isSmartInsertByDefault()
				&& this.fRConfig.isSmartInsertByDefault();
	}
	
	
	@Override
	protected IInformationProvider getQuickInformationProvider(final ISourceViewer sourceViewer,
			final int operation) {
		final ISourceEditor editor= getSourceEditor();
		if (editor == null) {
			return null;
		}
		switch (operation) {
		case SourceEditorViewer.SHOW_SOURCE_OUTLINE:
			return new LtxRQuickOutlineInformationProvider(editor, operation);
		default:
			return null;
		}
	}
	
}
