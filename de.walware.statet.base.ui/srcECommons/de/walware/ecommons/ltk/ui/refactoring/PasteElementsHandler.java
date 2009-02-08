/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui.refactoring;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.ltk.internal.ui.refactoring.RefactoringMessages;
import de.walware.ecommons.ltk.text.ISourceStructElement;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.WorkbenchUIUtil;


/**
 * Handler pasting elements from clipboard at selected position
 */
public class PasteElementsHandler extends AbstractElementsHandler {
	
	
	private ISourceEditor fEditor;
	
	
	public PasteElementsHandler(final ISourceEditor editor, final RefactoringAdapter ltk) {
		super(ltk);
		fEditor = editor;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(final Object evaluationContext) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		if (selection == null) {
			return null;
		}
		final SourceViewer sourceViewer = fEditor.getViewer();
		final AbstractDocument document = (AbstractDocument) sourceViewer.getDocument();
		if (!UIAccess.isOkToUse(sourceViewer) || !fEditor.isEditable(true)) {
			return null;
		}
		
		try {
			int offset = -1;
			if (selection instanceof IStructuredSelection) {
				final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.isEmpty()) {
					offset = document.getLength();
				}
				else if (structuredSelection.size() == 1) {
					final Object object = structuredSelection.getFirstElement();
					if (object instanceof ISourceStructElement) {
						final IRegion range = getRefactoringAdapter().expandElementRange(
								((ISourceStructElement) object).getSourceRange(), document);
						offset = range.getOffset()+range.getLength();
					}
					final int line = document.getLineOfOffset(offset);
					final IRegion lineInformation = document.getLineInformation(line);
					if (offset == lineInformation.getOffset() + lineInformation.getLength()) {
						offset += document.getLineDelimiter(line).length();
					}
				}
			}
			if (offset < 0) {
				return null;
			}
			
			final String code = getCodeFromClipboard(event);
			if (code != null) {
				final Position position = new Position(offset, document.getLength()-offset);
				document.addPosition(position);
				final StyledText textWidget = sourceViewer.getTextWidget();
				textWidget.replaceTextRange(sourceViewer.modelOffset2WidgetOffset(offset), 0, code);
				if (!position.isDeleted()) {
					textWidget.setSelection(sourceViewer.modelOffset2WidgetOffset(position.getOffset()));
				}
			}
		}
		catch (final BadPartitioningException e) {
			StatusManager.getManager().handle(new Status(
					IStatus.ERROR, getRefactoringAdapter().getPluginIdentifier(), -1,
					RefactoringMessages.PastingElements_error_message, e),
					StatusManager.LOG | StatusManager.SHOW);
		}
		catch (final BadLocationException e) {
			StatusManager.getManager().handle(new Status(
					IStatus.ERROR, getRefactoringAdapter().getPluginIdentifier(), -1,
					RefactoringMessages.PastingElements_error_message, e),
					StatusManager.LOG | StatusManager.SHOW);
		}
		return null;
	}
	
}
