/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors.templates;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.text.IndentUtil.IndentEditAction;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.ext.templates.StatextEditorTemplateContext;

import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.REditor;


public class REditorContext extends StatextEditorTemplateContext {
	
	
	public REditorContext(final TemplateContextType type, final IDocument document,	final int offset, final int length,
			final ISourceEditor editor) {
		super(type, document, offset, length, editor);
	}
	
	
	@Override
	public void setVariable(final String name, String value) {
		if ("selection".equals(name) && value != null && value.length() > 0) { //$NON-NLS-1$
			try {
				final IDocument valueDoc = new Document(value);
				
				final RIndentUtil util = new RIndentUtil(valueDoc, REditor.getRCoreAccess(getEditor()).getRCodeStyle());
				final int column = util.getMultilineIndentColumn(0, valueDoc.getNumberOfLines()-1);
				if (column > 0) {
					final IndentEditAction action = new IndentEditAction(column) {
						@Override
						public void doEdit(final int line, final int offset, final int length, final StringBuilder text)
								throws BadLocationException {
							TextEdit edit;
							if (text != null) {
								final int position = util.getIndentedIndex(text, column);
								edit = new ReplaceEdit(offset, length, text.substring(position, text.length()));
							}
							else {
								final int end = util.getIndentedOffsetAt(line, column);
								edit = new DeleteEdit(offset, end-offset);
							}
							edit.apply(valueDoc, 0);
						}
					};
					util.editInIndent(0, valueDoc.getNumberOfLines()-1, action);
					setVariable("indentation", util.createIndentString(column)); //$NON-NLS-1$
					value = valueDoc.get();
				}
			}
			catch (final BadLocationException e) {
				RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while computing indentation variable for R editor templates.", e); //$NON-NLS-1$
			}
		}
		super.setVariable(name, value);
	}
	
}
