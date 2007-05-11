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

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.ext.templates.IStatetContext;
import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.text.r.RIndentation;
import de.walware.statet.r.ui.text.r.RIndentation.IndentEditAction;


public class REditorContext extends DocumentTemplateContext implements IStatetContext {

	private RResourceUnit fUnit;
	
	public REditorContext(TemplateContextType type, IDocument document,	int offset, int length,
			RResourceUnit unit) {

		super(type, document, offset, length);
		fUnit = unit;
	}

//	public REditorContext(TemplateContextType type, IDocument document,	Position position) {
//		super(type, document, position);
//	}
	
	public StatetProject getStatetProject() {
		
		return fUnit.getStatetProject();
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
				IDocument valueDoc = new Document(value);
				final RIndentation indent = new RIndentation(valueDoc, fUnit.getRCodeStyle());
				int depth = indent.getMultilineIndentationDepth(0, valueDoc.getNumberOfLines()-1);
				if (depth > 0) {
					IndentEditAction action = indent.new IndentEditAction(depth) {
						@Override
						public TextEdit createEdit(int offset, int length, StringBuilder text) throws BadLocationException {
							int position = indent.getIndentedIndex(text, getDepth());
							return new ReplaceEdit(offset, length, text.substring(position, text.length()));
						}
						@Override
						public TextEdit createEdit(int offset) throws BadLocationException {
							int end = indent.getIndentedOffset(getDocument().getLineOfOffset(offset), getDepth());
							return new DeleteEdit(offset, end-offset);
						}
					};
					for (int line = 0; line < valueDoc.getNumberOfLines(); line++) {
						TextEdit edit = indent.edit(line, action);
						if (edit != null) {
							edit.apply(valueDoc, 0);
						}
					}
					setVariable("indentation", indent.createIndentationString(depth)); //$NON-NLS-1$
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
