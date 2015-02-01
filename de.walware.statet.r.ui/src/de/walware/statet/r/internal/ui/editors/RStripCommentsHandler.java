/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorProgressHandler;
import de.walware.ecommons.text.TextUtil;

import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.internal.ui.RUIMessages;


public class RStripCommentsHandler extends SourceEditorProgressHandler {
	
	
	public RStripCommentsHandler(final ISourceEditor editor) {
		super(editor);
	}
	
	
	@Override
	protected String getTaskLabel() {
		return RUIMessages.StripComments_task_label;
	}
	
	@Override
	protected boolean isEditTask() {
		return true;
	}
	
	@Override
	protected void doExecute(final ISourceEditor editor, final ISourceUnit su,
			final ITextSelection selection, final IProgressMonitor monitor) throws Exception {
		final AbstractDocument document = su.getDocument(monitor);
		final ISourceUnitModelInfo model = su.getModelInfo(null, IModelManager.MODEL_FILE, monitor);
		final RHeuristicTokenScanner scanner = (RHeuristicTokenScanner) LTK.getModelAdapter(su.getModelTypeId(), RHeuristicTokenScanner.class);
		
		if (model == null || scanner == null || monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask(getTaskLabel() + "..."); //$NON-NLS-1$
		
		final List<? extends IAstNode> comments = ((SourceComponent) model.getAst().root).getComments();
		final IRegion region = TextUtil.getBlock(document,
				selection.getOffset(), selection.getOffset()+selection.getLength() );
		
		scanner.configure(document);
		final MultiTextEdit edits = new MultiTextEdit();
		final int beginOffset = region.getOffset();
		final int endOffset = region.getOffset() + region.getLength();
		for (final IAstNode comment : comments) {
			if (comment.getOffset() >= beginOffset) {
				if (comment.getOffset() >= endOffset) {
					break;
				}
				final int line = document.getLineOfOffset(comment.getOffset());
				final int lineOffset = document.getLineOffset(line);
				
				int offset = scanner.findNonBlankBackward(comment.getOffset(), lineOffset, false);
				if (offset >= 0) {
					offset++;
				}
				else {
					offset = lineOffset;
				}
				if (offset == lineOffset) {
					edits.addChild(new DeleteEdit(lineOffset, document.getLineLength(line)));
				}
				else {
					edits.addChild(new DeleteEdit(
							offset, comment.getOffset() + comment.getLength() - offset ));
				}
			}
		}
		
		if (edits.getChildrenSize() > 0) {
			su.syncExec(new SourceDocumentRunnable(document, model.getStamp(), DocumentRewriteSessionType.SEQUENTIAL) {
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
	}
	
}
