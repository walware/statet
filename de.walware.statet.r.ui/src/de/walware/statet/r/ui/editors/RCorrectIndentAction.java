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

package de.walware.statet.r.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IDocumentModelProvider;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.SourceDocumentRunnable;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 *
 */
public class RCorrectIndentAction extends Action implements IUpdate {
	
	
	private REditor fEditor;
	private IEditorAdapter fEditorAdapter;
	private RSourceIndenter fIndenter;
	
	
	public RCorrectIndentAction(final REditor editor) {
		setId("de.walware.statet.r.actions.RCorrectIndent"); //$NON-NLS-1$
		setActionDefinitionId(IStatetUICommandIds.CORRECT_INDENT);
		fEditor = editor;
		fEditorAdapter = (IEditorAdapter) editor.getAdapter(IEditorAdapter.class);
	}
	
	
	public void update() {
		setEnabled(fEditorAdapter.isEditable(false));
	}
	
	@Override
	public void run() {
		if (!fEditorAdapter.isEditable(true)) {
			return;
		}
		try {
			final ITextSelection selection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
			
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						final IDocumentProvider documentProvider = fEditor.getDocumentProvider();
						final ISourceUnit unit = ((IDocumentModelProvider) documentProvider).getWorkingCopy(fEditor.getEditorInput());
						doCorrection(unit, selection, monitor);
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
					finally {
					}
				}
			});
		} catch (final InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	private void doCorrection(final ISourceUnit unit, final ITextSelection selection, final IProgressMonitor monitor)
			throws CoreException {
		monitor.subTask("Updating document structure...");
		final AbstractDocument document = unit.getDocument();
		final AstInfo<RAstNode> ast = (AstInfo<RAstNode>) unit.getAstInfo("r", true, monitor); //$NON-NLS-1$
		
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask("Indenting lines...");
		
		if (fIndenter == null) {
			fIndenter = new RSourceIndenter();
		}
		try {
			final int startLine = selection.getStartLine(); // save before change
	//		if (length > 0 && fDocument.getLineOffset(fLastLine) == start+length) {
	//			fLastLine--;
	//		}
			final MultiTextEdit edits = new MultiTextEdit();
			final List<IRegion> codeRanges = getCodeRanges(document, selection);
			for (final IRegion range : codeRanges) {
				final int rStartLine = document.getLineOfOffset(Math.max(selection.getOffset(), range.getOffset()));
				int rEndLine = document.getLineOfOffset(Math.min(selection.getOffset()+selection.getLength(), range.getOffset()+range.getLength()));
				final int rEndLineOffset = document.getLineOffset(rEndLine);
				if (rEndLineOffset == range.getOffset()+range.getLength()
						|| (rStartLine < rEndLine && rEndLineOffset == selection.getOffset()+selection.getLength())) {
					rEndLine--;
				}
				if (rStartLine <= rEndLine) {
					final TextEdit rEdits = fIndenter.getIndentEdits(document, ast, range.getOffset(), rStartLine, rEndLine, fEditor.getRCoreAccess());
					if (rEdits.getChildrenSize() > 0) {
						edits.addChild(rEdits);
					}
				}
			}
			
			if (edits.getChildrenSize() > 0) {
				unit.syncExec(new SourceDocumentRunnable(document, ast.stamp,
						(edits.getChildrenSize() > 50) ? DocumentRewriteSessionType.SEQUENTIAL : DocumentRewriteSessionType.SEQUENTIAL) {
					@Override
					public void run(final AbstractDocument document) throws InvocationTargetException {
						try {
							edits.apply(document, TextEdit.NONE);
						}
						catch (final MalformedTreeException e) {
							throw new InvocationTargetException(e);
						}
						catch (final BadLocationException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			}
			
			if (selection.getLength() == 0) {
				final int newPos = fIndenter.getNewIndentOffset(startLine);
				if (newPos >= 0) {
					UIAccess.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (UIAccess.isOkToUse(fEditorAdapter.getSourceViewer())) {
								fEditor.selectAndReveal(newPos, 0);
							}
						}
					});
				}
			}
		}
		catch (final BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected List<IRegion> getCodeRanges(final AbstractDocument document, final ITextSelection selection) {
		final List<IRegion> regions = new ArrayList<IRegion>(1);
		regions.add(new Region(0, document.getLength()));
		return regions;
	}
	
}
