/*******************************************************************************
 * Copyright (c) 2000-2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - adaptions for StatET, ISourceEditor, Handler
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.ui.util.DNDUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * An action to delete/cut a whole line, the fraction of the line that is left from the cursor
 * or the fraction that is right from the cursor.
 */
public class DeleteLineHandler extends AbstractHandler {
	
	/**
	 * Delete the whole line.
	 */
	public static final int WHOLE = org.eclipse.ui.texteditor.DeleteLineAction.WHOLE;
	
	/**
	 * Delete to the beginning of line.
	 */
	public static final int TO_BEGINNING = org.eclipse.ui.texteditor.DeleteLineAction.TO_BEGINNING;
	
	/**
	 * Delete to the end of line.
	 */
	public static final int TO_END = org.eclipse.ui.texteditor.DeleteLineAction.TO_END;
	
	
	/**
	 * The type of deletion.
	 */
	private final int fType;
	/**
	 * Should the deleted line be copied to the clipboard.
	 * @since 2.1
	 */
	private final boolean fCopyToClipboard;
	/** The deletion target.
	 * @since 2.1
	 */
	
	private ISourceEditor fSourceEditor;
	
	
	/**
	 * Creates a line deletion/cut action.
	 * 
	 * @param editor the editor
	 * @param prefix the prefix for the property keys into <code>bundle</code>
	 * @param type the line deletion type, must be one of
	 *     <code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 * @param copyToClipboard if <code>true</code>, the contents of the deleted line are copied to the clipboard
	 */
	public DeleteLineHandler(final ISourceEditor editor, final int type, final boolean copyToClipboard) {
		fSourceEditor = editor;
		fType = type;
		fCopyToClipboard = copyToClipboard;
		
		update();
	}
	
	/**
	 * Creates a line deletion/cut action.
	 * 
	 * @param editor the editor
	 * @param prefix the prefix for the property keys into <code>bundle</code>
	 * @param commandId
	 */
	public DeleteLineHandler(final ISourceEditor editor, final String commandId) {
		fSourceEditor = editor;
		if (commandId.equals(ITextEditorActionDefinitionIds.CUT_LINE)) {
			fType = WHOLE;
			fCopyToClipboard = true;
		}
		else if (commandId.equals(ITextEditorActionDefinitionIds.CUT_LINE_TO_BEGINNING)) {
			fType = TO_BEGINNING;
			fCopyToClipboard = true;
		}
		else if (commandId.equals(ITextEditorActionDefinitionIds.CUT_LINE_TO_END)) {
			fType = TO_END;
			fCopyToClipboard = true;
		}
		else if (commandId.equals(ITextEditorActionDefinitionIds.DELETE_LINE)) {
			fType = WHOLE;
			fCopyToClipboard = false;
		}
		else if (commandId.equals(ITextEditorActionDefinitionIds.DELETE_LINE_TO_BEGINNING)) {
			fType = TO_BEGINNING;
			fCopyToClipboard = false;
		}
		else if (commandId.equals(ITextEditorActionDefinitionIds.DELETE_LINE_TO_END)) {
			fType = TO_END;
			fCopyToClipboard = false;
		}
		else {
			throw new IllegalArgumentException("Unsupported command"); //$NON-NLS-1$
		}
		
		update();
	}
	
	
	public void update() {
		setBaseEnabled(fSourceEditor.isEditable(false));
	}
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (fSourceEditor.isEditable(true)) {
			try {
				deleteLine();
			}
			catch (final BadLocationException e) {
				StatetUIPlugin.logUnexpectedError(e);
			}
		}
		return null;
	}
	
	private void deleteLine() throws BadLocationException {
		final ISourceViewer sourceViewer = fSourceEditor.getViewer();
		final IDocument document = sourceViewer.getDocument();
		final Point selection = sourceViewer.getSelectedRange();
		
		if (document == null || selection == null) {
			return;
		}
		
		final IRegion deleteRegion = getDeleteRegion(document, selection.x, selection.y, fType);
		if (fCopyToClipboard) {
			final String text = document.get(deleteRegion.getOffset(), deleteRegion.getLength());
			final Clipboard clipboard = new Clipboard(sourceViewer.getTextWidget().getDisplay());
			try {
				DNDUtil.setContent(clipboard, 
						new String[] { text }, 
						new Transfer[] { TextTransfer.getInstance() } );
			}
			finally {
				clipboard.dispose();
			}
		}
		document.replace(deleteRegion.getOffset(), deleteRegion.getLength(), ""); //$NON-NLS-1$
	}
	
	
	private static IRegion getDeleteRegion(final IDocument document, final int offset, final int length, final int type) throws BadLocationException {
		final int line = document.getLineOfOffset(offset);
		int temp;
		
		switch  (type) {
		case DeleteLineHandler.WHOLE:
			return TextUtil.getBlock(document, offset, offset+length);
		
		case DeleteLineHandler.TO_BEGINNING:
			temp = document.getLineOffset(line);
			return new Region(temp, offset-temp);
		
		case DeleteLineHandler.TO_END:
			temp = document.getLineOffset(line)+document.getLineLength(line);
			return new Region(offset, temp-offset);
		
		default:
			throw new IllegalArgumentException();
		}
	}
	
}
