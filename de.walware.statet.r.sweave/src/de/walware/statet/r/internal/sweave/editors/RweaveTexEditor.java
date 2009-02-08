/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.ElementInfoController;
import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfigurator;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.base.ui.sourceeditors.StatextOutlinePage1;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RCorrectIndentAction;
import de.walware.statet.r.ui.editors.REditor;


/**
 * Editor for Sweave (LaTeX/R) code.
 */
public class RweaveTexEditor extends REditor {
	
	
	private class MultiCatCommentAction extends ToggleCommentAction {
		
		MultiCatCommentAction() {
			super();
		}
		
		@Override
		public void run() {
			final ISelection selection = getSelectionProvider().getSelection();
			if (!(selection instanceof ITextSelection)) {
				return;
			}
			try {
				final ITextSelection textSelection = (ITextSelection) selection;
				final IDocument document = getSourceViewer().getDocument();
				final IRegion block = getTextBlockFromSelection(textSelection, document);
				final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(document,
						block.getOffset(), block.getLength());
				if (cats != null && cats.length > 1) {
					commentTex(document, block);
					return;
				}
				super.run();
			} catch (final BadLocationException e) {
				SweavePlugin.logError(-1, "Error when commenting multi cat.", e);
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
		
		public CatIndentAction(final REditor editor) {
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
	
	
	
	private RweaveTexSourceViewerConfigurator fCombinedConfig;
	private ISourceUnit fDocUnit;
	
	
	public RweaveTexEditor() {
		super();
	}
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		fModelProvider = new ElementInfoController(RCore.getRModelManager(), ECommonsLTK.EDITOR_CONTEXT);
		enableStructuralFeatures(fModelProvider, null, null);
		
		configureStatetProjectNatureId(RProject.NATURE_ID);
		setDocumentProvider(SweavePlugin.getDefault().getRTexDocumentProvider());
		
		final IRCoreAccess basicContext = RCore.getWorkbenchAccess();
		fOptions = RUIPlugin.getDefault().getREditorSettings(basicContext.getPrefs());
		
		final IPreferenceStore store = SweavePlugin.getDefault().getEditorRTexPreferenceStore();
		fCombinedConfig = new RweaveTexSourceViewerConfigurator(basicContext, store);
		fRConfig = fCombinedConfig;
		fCombinedConfig.setConfiguration(new RweaveTexSourceViewerConfiguration(this,
				fCombinedConfig, store, StatetUIServices.getSharedColorManager()));
		return fCombinedConfig;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "de.walware.statet.r.contexts.RweaveEditorScope" }); //$NON-NLS-1$
	}
	
	@Override
	protected void setupConfiguration(final RProject prevProject, final RProject newProject, final IEditorInput newInput) {
		fDocUnit = ((RweaveTexDocumentProvider) getDocumentProvider()).getWorkingCopy(newInput);
		fRConfig.setSource((fDocUnit != null) ? (IRCoreAccess) fDocUnit.getAdapter(IRCoreAccess.class) : null);
//		fModelProvider.setInput(fRResourceUnit);
	}
	
	@Override
	protected void setupConfiguration(final RProject prevProject, final RProject newProject, final IEditorInput newInput,
			final ISourceViewer sourceViewer) {
		if (fOptions.isSmartModeByDefaultEnabled()) {
			setInsertMode(SMART_INSERT);
		}
		else {
			setInsertMode(INSERT);
		}
	}
	
	@Override
	protected IAction createToggleCommentAction() {
		return new MultiCatCommentAction();
	}
	
	@Override
	protected IAction createCorrectIndentAction() {
		return new CatIndentAction(this);
	}
	
	@Override
	public ISourceUnit getSourceUnit() {
		return fDocUnit;
	}
	
	
	@Override
	protected StatextOutlinePage1 createOutlinePage() {
		return null;
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new RweaveTexEditorTemplatesPage(this, getSourceViewer());
	}
	
}
