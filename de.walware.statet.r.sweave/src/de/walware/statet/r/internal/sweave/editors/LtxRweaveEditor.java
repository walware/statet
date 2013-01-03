/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ui.ElementInfoController;
import de.walware.ecommons.ltk.ui.sourceediting.AbstractMarkOccurrencesProvider;
import de.walware.ecommons.ltk.ui.sourceediting.FoldingEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.actions.SpecificContentAssistHandler;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.docmlet.tex.core.ast.TexAstNode;
import de.walware.docmlet.tex.core.model.TexModel;
import de.walware.docmlet.tex.ui.editors.LtxDefaultFoldingProvider;
import de.walware.docmlet.tex.ui.editors.TexEditorOptions;
import de.walware.docmlet.tex.ui.editors.TexMarkOccurrencesLocator;

import de.walware.statet.base.ui.IStatetUICommandIds;

import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.sweave.ILtxRweaveEditor;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.Sweave;
import de.walware.statet.r.sweave.text.Rweave;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.DefaultRFoldingProvider;
import de.walware.statet.r.ui.editors.RCorrectIndentAction;
import de.walware.statet.r.ui.editors.RMarkOccurrencesLocator;
import de.walware.statet.r.ui.sourceediting.InsertAssignmentHandler;


/**
 * Editor for Sweave (LaTeX/R) code.
 */
public class LtxRweaveEditor extends SourceEditor1 implements ILtxRweaveEditor {
	
	
	private static class ThisMarkOccurrencesProvider extends AbstractMarkOccurrencesProvider {
		
		
		private final RMarkOccurrencesLocator fRLocator = new RMarkOccurrencesLocator();
		private final TexMarkOccurrencesLocator fTexLocator = new TexMarkOccurrencesLocator();
		
		
		public ThisMarkOccurrencesProvider(final SourceEditor1 editor) {
			super(editor, Rweave.R_PARTITIONING_CONFIG.getDefaultPartitionConstraint() );
		}
		
		@Override
		protected void doUpdate(final RunData run, final ISourceUnitModelInfo info,
				final AstSelection astSelection, final ITextSelection orgSelection)
				throws BadLocationException, BadPartitioningException, UnsupportedOperationException {
			if (astSelection.getCovering() instanceof RAstNode) {
				fRLocator.run(run, info, astSelection, orgSelection);
			}
			else if (astSelection.getCovering() instanceof TexAstNode) {
				fTexLocator.run(run, info, astSelection, orgSelection);
			}
		}
		
	}
	
	
	private class MultiCatCommentHandler extends ToggleCommentHandler {
		
		MultiCatCommentHandler() {
			super();
		}
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final ISelection selection = getSelectionProvider().getSelection();
			if (!(selection instanceof ITextSelection)) {
				return null;
			}
			try {
				final ITextSelection textSelection = (ITextSelection) selection;
				final IDocument document = getSourceViewer().getDocument();
				final IRegion block = getTextBlockFromSelection(textSelection, document);
				final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(document,
						block.getOffset(), block.getLength());
				if (cats != null && cats.length > 1) {
					commentTex(document, block);
					return null;
				}
				return super.execute(event);
			}
			catch (final BadLocationException e) {
				SweavePlugin.logError(-1, "Error when commenting multi cat.", e);
				return null;
			}
		}
		
		private void commentTex(final IDocument document, final IRegion region) throws BadLocationException {
			final int startLine = document.getLineOfOffset(region.getOffset());
			final int stopLine = (region.getLength() > 0) ?
					document.getLineOfOffset(region.getOffset()+region.getLength()-1) :
					startLine;
			final MultiTextEdit multi = new MultiTextEdit(region.getOffset(), region.getLength());
			for (int line = startLine; line <= stopLine; line++) {
				multi.addChild(new ReplaceEdit(document.getLineOffset(line), 0, "%")); //$NON-NLS-1$
			}
			if (document instanceof IDocumentExtension4) {
				final IDocumentExtension4 document4 = (IDocumentExtension4) document;
				final DocumentRewriteSession rewriteSession = document4.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
				try {
					multi.apply(document, TextEdit.NONE);
				}
				finally {
					document4.stopRewriteSession(rewriteSession);
				}
			}
			else {
				multi.apply(document, TextEdit.NONE);
			}
		}
		
	}
	
	private class CatIndentAction extends RCorrectIndentAction {
		
		public CatIndentAction(final SourceEditor1 editor) {
			super(editor);
		}
		
		@Override
		protected List<IRegion> getCodeRanges(final AbstractDocument document, final ITextSelection selection) {
			final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(document, selection.getOffset(), selection.getLength());
			final List<IRegion> regions = new ArrayList<IRegion>(3+cats.length/4);
			for (int i = 0; i < cats.length; i++) {
				if (cats[i].getType() == Rweave.R_CAT) {
					regions.add(cats[i]);
				}
			}
			return regions;
		}
	}
	
	
	private ElementInfoController fModelProvider;
	
	private LtxRweaveViewerConfigurator fCombinedConfig;
	
	
	public LtxRweaveEditor() {
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		setEditorContextMenuId("de.walware.statet.r.sweave.menus.LtxRweaveEditorContextMenu"); //$NON-NLS-1$
		setRulerContextMenuId("de.walware.statet.r.sweave.menus.LtxRweaveEditorRulerMenu"); //$NON-NLS-1$
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		setDocumentProvider(SweavePlugin.getDefault().getRTexDocumentProvider());
		
		fModelProvider = new ElementInfoController(TexModel.getModelManager(), LTK.EDITOR_CONTEXT);
		enableStructuralFeatures(fModelProvider,
				TexEditorOptions.FOLDING_ENABLED_PREF,
				SweaveEditorOptions.MARKOCCURRENCES_ENABLED_PREF );
		
		fCombinedConfig = new LtxRweaveViewerConfigurator(null,
				new LtxRweaveViewerConfiguration(this, null, null, SharedUIResources.getColors()) );
		return fCombinedConfig;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] {
				"de.walware.docmlet.tex.contexts.TexEditor", //$NON-NLS-1$
				"de.walware.statet.r.contexts.RweaveEditorScope", //$NON-NLS-1$
		});
	}
	
	@Override
	protected ISourceEditorAddon createCodeFoldingProvider() {
		return new FoldingEditorAddon(new LtxDefaultFoldingProvider(
				new DefaultRFoldingProvider() ));
	}
	
	@Override
	protected ISourceEditorAddon createMarkOccurrencesProvider() {
		return new ThisMarkOccurrencesProvider(this);
	}
	
	
	@Override
	public String getModelTypeId() {
		return Sweave.LTX_R_MODEL_TYPE_ID;
	}
	
	@Override
	public ILtxRweaveSourceUnit getSourceUnit() {
		return (ILtxRweaveSourceUnit) super.getSourceUnit();
	}
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		super.setupConfiguration(newInput);
		final ILtxRweaveSourceUnit su = getSourceUnit();
		fModelProvider.setInput(su);
		fCombinedConfig.setSource(su.getTexCoreAccess());
	}
	
	
	@Override
	protected void handlePreferenceStoreChanged(final org.eclipse.jface.util.PropertyChangeEvent event) {
		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(event.getProperty())
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS.equals(event.getProperty())) {
			return;
		}
		super.handlePreferenceStoreChanged(event);
	}
	
	
	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		return false;
	}
	
	public void updateSettings(final boolean indentChanged) {
		if (indentChanged) {
			updateIndentPrefixes();
		}
	}
	
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		super.collectContextMenuPreferencePages(pageIds);
		pageIds.add("de.walware.statet.r.preferencePages.SweaveEditor"); //$NON-NLS-1$
		pageIds.add("de.walware.docmlet.tex.preferencePages.TexEditor"); //$NON-NLS-1$
		pageIds.add("de.walware.docmlet.tex.preferencePages.LtxTextStyles"); //$NON-NLS-1$
		pageIds.add("de.walware.docmlet.tex.preferencePages.LtxEditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.docmlet.tex.preferencePages.TexCodeStyle"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RTextStyles"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RCodeStyle"); //$NON-NLS-1$
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		final IHandlerService handlerService = (IHandlerService) getServiceLocator().getService(IHandlerService.class);
		
		{	final IHandler2 handler = new InsertAssignmentHandler(this);
			handlerService.activateHandler(IStatetUICommandIds.INSERT_ASSIGNMENT, handler);
			markAsStateDependentHandler(handler, true);
		}
		{	final IHandler2 handler = new LtxRweaveForwardHandler(this, null,
					new SpecificContentAssistHandler(this,
							RUIPlugin.getDefault().getREditorContentAssistRegistry() ));
			handlerService.activateHandler(ISourceEditorCommandIds.SPECIFIC_CONTENT_ASSIST_COMMAND_ID, handler);
		}
	}
	
	@Override
	protected IHandler createToggleCommentHandler() {
		final MultiCatCommentHandler commentHandler = new MultiCatCommentHandler();
		markAsStateDependentHandler(commentHandler, true);
		return commentHandler;
	}
	
	@Override
	protected IAction createCorrectIndentAction() {
		return new CatIndentAction(this);
	}
	
	
	@Override
	protected SourceEditor1OutlinePage createOutlinePage() {
		return new LtxRweaveOutlinePage(this);
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new LtxRweaveEditorTemplatesPage(this, getSourceViewer());
	}
	
	@Override
	public String[] getShowInTargetIds() {
		return new String[] { IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.ID_OUTLINE, RUI.R_HELP_VIEW_ID };
	}
	
}
