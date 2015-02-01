/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorProgressHandler;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.internal.ui.RUIMessages;


/**
 * Command handler to correct indentation of selected code lines.
 * @see RSourceIndenter
 */
public class RCorrectIndentHandler extends SourceEditorProgressHandler {
	
	
	private RSourceIndenter fIndenter;
	
	
	public RCorrectIndentHandler(final ISourceEditor editor) {
		super(editor);
	}
	
	
	@Override
	protected String getTaskLabel() {
		return RUIMessages.CorrectIndent_task_label;
	}
	
	@Override
	protected boolean isEditTask() {
		return true;
	}
	
	@Override
	protected void doExecute(final ISourceEditor editor, final ISourceUnit su,
			final ITextSelection selection, final IProgressMonitor monitor) throws Exception {
		final AbstractDocument document = su.getDocument(monitor);
		final AstInfo ast = su.getAstInfo(null, true, monitor);
		
		if (ast == null || monitor.isCanceled() ) {
			return;
		}
		
		monitor.subTask(getTaskLabel() + "...");
		
		if (fIndenter == null) {
			fIndenter = new RSourceIndenter();
		}
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
				fIndenter.setup((su instanceof IRSourceUnit) ? ((IRSourceUnit) su).getRCoreAccess() : RCore.getWorkbenchAccess());
				final TextEdit rEdits = fIndenter.getIndentEdits(document, ast.root,
						range.getOffset(), rStartLine, rEndLine );
				if (rEdits.getChildrenSize() > 0) {
					edits.addChild(rEdits);
				}
			}
		}
		
		if (edits.getChildrenSize() > 0) {
			su.syncExec(new SourceDocumentRunnable(document, ast.stamp, DocumentRewriteSessionType.SEQUENTIAL) {
				@Override
				public void run() throws InvocationTargetException {
					try {
						edits.apply(getDocument(), TextEdit.NONE);
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
					@Override
					public void run() {
						if (UIAccess.isOkToUse(editor.getViewer())) {
							editor.selectAndReveal(newPos, 0);
						}
					}
				});
			}
		}
	}
	
	protected List<IRegion> getCodeRanges(final AbstractDocument document, final ITextSelection selection) {
		final List<IRegion> regions = new ArrayList<IRegion>(1);
		regions.add(new Region(0, document.getLength()));
		return regions;
	}
	
}
