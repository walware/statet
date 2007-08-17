/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors.templates;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.eclipsecommons.ltk.text.IndentUtil.IndentEditAction;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.templates.IStatetContext;
import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.REditor;


public class REditorContext extends DocumentTemplateContext implements IStatetContext {

	
	private REditor fEditor;
	
	
	public REditorContext(TemplateContextType type, IDocument document,	int offset, int length,
			REditor editor) {
		super(type, document, offset, length);
		fEditor = editor;
	}

//	public REditorContext(TemplateContextType type, IDocument document,	Position position) {
//		super(type, document, position);
//	}
	
	public StatetProject getStatetProject() {
		return fEditor.getRResourceUnit().getStatetProject();
	}
	
	public String getInfo(Template template) throws BadLocationException, TemplateException {
		TemplateBuffer buffer = super.evaluate(template);
		if (buffer != null)
			return buffer.getString();
		return null;
	}
	
	@Override
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		TemplateBuffer buffer = super.evaluate(template);
		indent(buffer);
		String selection = getVariable("selection"); //$NON-NLS-1$
		if (selection != null && TextUtilities.indexOf(getDocument().getLegalLineDelimiters(), selection, 0)[0] != -1) {
			String ln = TextUtilities.getDefaultLineDelimiter(getDocument());
			buffer.setContent(buffer.getString()+ln, buffer.getVariables());
		}
		
		return buffer;
	}
	
	private void indent(TemplateBuffer buffer) throws BadLocationException {
		TemplateVariable[] variables = buffer.getVariables();
		List<TextEdit> positions = TemplatesUtil.variablesToPositions(variables);
		IDocument baseDoc = getDocument();

		IDocument templateDoc = new Document(buffer.getString());
		MultiTextEdit root = new MultiTextEdit(0, templateDoc.getLength());
		root.addChildren(positions.toArray(new TextEdit[positions.size()]));

		String indentation = getVariable("indentation"); //$NON-NLS-1$

		// first line
		int offset = templateDoc.getLineOffset(0);
		if (indentation != null) {
			TextEdit edit = new InsertEdit(offset, indentation);
			root.addChild(edit);
			root.apply(templateDoc, TextEdit.UPDATE_REGIONS);
			root.removeChild(edit);
		}
		else {
			indentation = TemplatesUtil.searchIndentation(baseDoc, getStart());
		}
		
		// following lines
	    for (int line = 1; line < templateDoc.getNumberOfLines(); line++) {
			IRegion region = templateDoc.getLineInformation(line);
			offset = region.getOffset();
	    		
			TextEdit edit = new InsertEdit(offset, indentation);
			root.addChild(edit);
			root.apply(templateDoc, TextEdit.UPDATE_REGIONS);
			root.removeChild(edit);
	    }
	    
		TemplatesUtil.positionsToVariables(positions, variables);
		buffer.setContent(templateDoc.get(), variables);
	}
	
	@Override
	public void setVariable(String name, String value) {
		if ("selection".equals(name) && value != null && value.length() > 0) { //$NON-NLS-1$
			try {
				final IDocument valueDoc = new Document(value);
				final RIndentUtil util = new RIndentUtil(valueDoc, fEditor.getRCoreAccess().getRCodeStyle());
				final int column = util.getMultilineIndentColumn(0, valueDoc.getNumberOfLines()-1);
				if (column > 0) {
					IndentEditAction action = new IndentEditAction(column) {
						@Override
						public void doEdit(int line, int offset, int length, StringBuilder text)
								throws BadLocationException {
							TextEdit edit;
							if (text != null) {
								int position = util.getIndentedIndex(text, column);
								edit = new ReplaceEdit(offset, length, text.substring(position, text.length()));
							}
							else {
								int end = util.getIndentedOffsetAt(line, column);
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
			catch (BadLocationException e) {
				RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "An error occurred while computing indentation variable for R editor templates.", e); //$NON-NLS-1$
			}
		}
		super.setVariable(name, value);
	}
	
}
