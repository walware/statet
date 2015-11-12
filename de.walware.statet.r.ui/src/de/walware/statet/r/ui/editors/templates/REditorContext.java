/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.editors.templates;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.templates.SourceEditorTemplateContext;
import de.walware.ecommons.text.IndentUtil;
import de.walware.ecommons.text.IndentUtil.IndentEditAction;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.IRSourceEditor;


public class REditorContext extends SourceEditorTemplateContext {
	
	
	public REditorContext(final TemplateContextType type, final IDocument document,	final int offset, final int length,
			final ISourceEditor editor) {
		super(type, document, offset, length, editor);
	}
	
	
	protected IRCoreAccess getRCoreAccess() {
		final ISourceEditor editor= getEditor();
		return (editor instanceof IRSourceEditor) ?
				((IRSourceEditor) editor).getRCoreAccess() :
				RCore.WORKBENCH_ACCESS;
	}
	
	@Override
	public void setVariable(final String name, String value) {
		if ("selection".equals(name) && value != null && value.length() > 0) { //$NON-NLS-1$
			try {
				final IDocument valueDoc = new Document(value);
				
				final IndentUtil util = new IndentUtil(valueDoc, getRCoreAccess().getRCodeStyle());
				final int column = util.getMultilineIndentColumn(0, valueDoc.getNumberOfLines()-1);
				if (column > 0) {
					final IndentEditAction action = new IndentEditAction(column) {
						@Override
						public void doEdit(final int line, final int offset, final int length, final StringBuilder text)
								throws BadLocationException {
							TextEdit edit;
							if (text != null) {
								final int position = util.getIndentedOffsetAt(text, column);
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
