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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
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
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ui.ElementInfoController;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.sweave.text.RweaveChunkHeuristicScanner;
import de.walware.statet.r.ui.editors.RCorrectIndentAction;
import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Editor for Sweave (LaTeX/R) code.
 */
public class RweaveTexEditor extends SourceEditor1 {
	
	
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
	
	
	private RweaveTexViewerConfigurator fCombinedConfig;
	protected REditorOptions fROptions;
	
	protected ElementInfoController fModelProvider;
	
	
	public RweaveTexEditor() {
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		setEditorContextMenuId("#REditorContext"); //$NON-NLS-1$
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		fModelProvider = new ElementInfoController(RCore.getRModelManager(), LTK.EDITOR_CONTEXT);
		enableStructuralFeatures(fModelProvider, null, null);
		
		setDocumentProvider(SweavePlugin.getDefault().getRTexDocumentProvider());
		
		final IRCoreAccess basicContext = RCore.getWorkbenchAccess();
		fROptions = RUIPlugin.getDefault().getREditorSettings(basicContext.getPrefs());
		
		final IPreferenceStore store = SweavePlugin.getDefault().getEditorRTexPreferenceStore();
		fCombinedConfig = new RweaveTexViewerConfigurator(basicContext);
		fCombinedConfig.setConfiguration(new RweaveTexViewerConfiguration(this,
				fCombinedConfig, store, SharedUIResources.getColors() ));
		return fCombinedConfig;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] {
				"de.walware.statet.r.contexts.RweaveEditorScope", //$NON-NLS-1$
		});
	}
	
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		super.setupConfiguration(newInput);
		final ISourceUnit su = getSourceUnit();
		fCombinedConfig.setSource((su != null) ? (IRCoreAccess) su.getAdapter(IRCoreAccess.class) : null);
		fModelProvider.setInput(su);
	}
	
	@Override
	protected void doSetInput(final IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fROptions.isSmartModeByDefaultEnabled()) {
			setInsertMode(SMART_INSERT);
		}
		else {
			setInsertMode(INSERT);
		}
	}
	
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		super.collectContextMenuPreferencePages(pageIds);
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RTextStylesPage"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RCodeStyle"); //$NON-NLS-1$
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
		return null;
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new RweaveTexEditorTemplatesPage(this, getSourceViewer());
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(RHeuristicTokenScanner.class)) {
			return new RweaveChunkHeuristicScanner();
		}
		return super.getAdapter(required);
	}
	
}
