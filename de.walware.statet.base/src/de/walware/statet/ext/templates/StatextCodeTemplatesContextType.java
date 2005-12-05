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

package de.walware.statet.ext.templates;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.statet.base.StatetProject;


public class StatextCodeTemplatesContextType extends TemplateContextType {


	
	/**
	 * Resolver that resolves to the variable defined in the context.
	 */
	protected static class CodeTemplatesVariableResolver extends TemplateVariableResolver {
		
		public CodeTemplatesVariableResolver(String type, String description) {
			
			super(type, description);
		}
		
		protected String resolve(TemplateContext context) {
			return context.getVariable(getType());
		}
	}

	/**
	 * Resolver for ToDo-tags.
	 */
	protected static class Todo extends SimpleTemplateVariableResolver {

		public Todo() {
			super("todo", TemplatesMessages.Templates_Variable_ToDo_description);  //$NON-NLS-1$
		}
		
		protected String resolve(TemplateContext context) {
			if (context instanceof IStatetContext) {
				StatetProject proj = ((IStatetContext) context).getStatetProject();
				String todoTaskTag = TemplatesUtil.getTodoTaskTag(proj);
				if (todoTaskTag != null)
					return todoTaskTag;
			}
			return "TODO"; //$NON-NLS-1$
		}
	}
	
	/**
	 * Resolver for Project-name.
	 */
	protected static class Project extends SimpleTemplateVariableResolver {

		public Project() {
			super("enclosing_project", TemplatesMessages.Templates_Variable_EnclosingProject_description);  //$NON-NLS-1$
		}
		
		protected String resolve(TemplateContext context) {
			if (context instanceof IStatetContext) {
				StatetProject proj = ((IStatetContext) context).getStatetProject();
				if (proj != null)
					return proj.getProject().getName();
			}
			return "";
		}
	}
	

	public static final String FILENAME = "file_name"; //$NON-NLS-1$
	
	
	public StatextCodeTemplatesContextType(String id) {
		super(id);

		// Global
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		addResolver(new Todo());
		addResolver(new Project());
	}

	
	public void resolve(TemplateBuffer buffer, TemplateContext context) throws MalformedTreeException, BadLocationException {
		Assert.isNotNull(context);
		TemplateVariable[] variables= buffer.getVariables();

		IDocument document= new Document(buffer.getString());
		List<TextEdit> positions = TemplatesUtil.variablesToPositions(variables);
		List<TextEdit> edits= new ArrayList<TextEdit>(5);


        // iterate over all variables and try to resolve them
        for (int i= 0; i != variables.length; i++) {
            TemplateVariable variable= variables[i];

			if (variable.isUnambiguous())
				continue;
			
			// remember old values
			int[] oldOffsets= variable.getOffsets();
			int oldLength= variable.getLength();
			String oldValue= variable.getDefaultValue();

			String type= variable.getType();
			TemplateVariableResolver resolver = getResolver(type);
			if (resolver == null) {
				resolver = new TemplateVariableResolver();
				resolver.setType(type);
			}
			
			resolver.resolve(variable, context);

			String value= variable.getDefaultValue();
			String[] ln = document.getLegalLineDelimiters();
			boolean multiLine = (TextUtilities.indexOf(ln, value, 0)[0] != -1);

			if (!oldValue.equals(value))
				// update buffer to reflect new value
				for (int k= 0; k != oldOffsets.length; k++) {
					String thisValue = value;
					if (multiLine) {
						String indent = TemplatesUtil.searchIndentation(document, oldOffsets[k]);
						if (indent.length() > 0) {
							StringBuilder temp = new StringBuilder(thisValue);
							int offset = 0;
							while (true) {
								int[] search = TextUtilities.indexOf(ln, temp.toString(), offset);
								if (search[0] == -1)
									break;
								offset = search[0]+ln[search[1]].length();
								temp.insert(offset, indent);
								offset += indent.length();
							}
							thisValue = temp.toString();
						}
					}
					edits.add(new ReplaceEdit(oldOffsets[k], oldLength, thisValue));
				}
        }

        MultiTextEdit edit= new MultiTextEdit(0, document.getLength());
        edit.addChildren(positions.toArray(new TextEdit[positions.size()]));
        edit.addChildren(edits.toArray(new TextEdit[edits.size()]));
        edit.apply(document, TextEdit.UPDATE_REGIONS);

		TemplatesUtil.positionsToVariables(positions, variables);

        buffer.setContent(document.get(), variables);
    }
	
	
	
	
}
