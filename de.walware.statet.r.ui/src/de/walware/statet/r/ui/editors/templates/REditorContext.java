/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.text.edits.TextEdit;

import de.walware.statet.base.StatetProject;
import de.walware.statet.ext.templates.IStatetContext;
import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.r.core.RResourceUnit;


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
		String selection = getVariable("selection");
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

		String indentation = getVariable("indentation");

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
		
		if ("selection".equals(name) && value != null && value.length() > 0) {
			IDocument valueDoc = new Document(value);
			String ind = TemplatesUtil.searchMultilineIndentation(valueDoc);

			if (ind.length() > 0) {
				try {
					for (int line = 0; line < valueDoc.getNumberOfLines(); line++) {
						IRegion lineRegion = valueDoc.getLineInformation(line);
						int length = Math.min(lineRegion.getLength(), ind
								.length());
						if (length > 0) {
							TextEdit edit = new DeleteEdit(lineRegion
									.getOffset(), length);
							edit.apply(valueDoc, 0);
						}
					}
					setVariable("indentation", ind);
					value = valueDoc.get();
				} 
				catch (BadLocationException e) {
				}
			}
		}
		super.setVariable(name, value);
	}
	
}
