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

package de.walware.statet.ext.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.core.preferences.TaskTagsPreferences;


public class TemplatesUtil {
	
	
	public static class EvaluatedTemplate {
		
		private String fContent;
		
		private IRegion fSelect;
		
		private final String fLineDelimiter;
		private AbstractDocument fPostEditDocument;
		private Position fPostEditSelectPosition;
		
		
		public EvaluatedTemplate(final TemplateBuffer buffer, final String lineDelimiter) {
			setContent(buffer.getString());
			final TemplateVariable selectStartVariable = findVariable(buffer, StatextCodeTemplatesContextType.SELECT_START_VARIABLE);
			final TemplateVariable selectEndVariable = findVariable(buffer, StatextCodeTemplatesContextType.SELECT_END_VARIABLE);
			if (selectStartVariable != null && selectStartVariable.getOffsets().length == 1) {
				fSelect = new Region(selectStartVariable.getOffsets()[0],
						(selectEndVariable != null && selectEndVariable.getOffsets().length == 1) ?
						Math.max(selectEndVariable.getOffsets()[0] - selectStartVariable.getOffsets()[0], 0) : 0);
			}
			fLineDelimiter = lineDelimiter;
		}
		
		
		/**
		 * Sets the evaluated template text
		 * @param content the text
		 */
		public void setContent(final String content) {
			fPostEditDocument = null;
			fContent = content;
		}
		
		/**
		 * Returns the evaluated template text
		 * @return the text
		 * */
		public String getContent() {
			return fContent;
		}
		
		/**
		 * Returns the region to select, if specified
		 */
		public IRegion getRegionToSelect() {
			return fSelect;
		}
		
		/**
		 * Returns a document which can be used for further edits in the text.
		 * After edits are done, {@link #finishPostEdit()} must be called.
		 * 
		 * @return a document with the template content
		 * @throws BadLocationException
		 */
		public AbstractDocument startPostEdit() throws BadLocationException {
			if (fPostEditDocument == null) {
				fPostEditDocument = new Document(getContent()) {
					@Override
					public String getDefaultLineDelimiter() {
						return fLineDelimiter;
					}
				};
				if (fSelect != null) {
					fPostEditSelectPosition = new Position(fSelect.getOffset(), fSelect.getLength());
					fPostEditDocument.addPosition(fPostEditSelectPosition);
				}
			}
			return fPostEditDocument;
		}
		
		/**
		 * See {@link #startPostEdit()}.
		 */
		public void finishPostEdit() {
			setContent(fPostEditDocument.get());
			if (fPostEditSelectPosition != null) {
				fSelect = (fPostEditSelectPosition.isDeleted) ? null :
						new Region(fPostEditSelectPosition.getOffset(), fPostEditSelectPosition.getLength());
			}
		}
		
	}
	
	public static String searchIndentation(final IDocument document, final int offset) {
		try {
			final IRegion region = document.getLineInformationOfOffset(offset);
			final String lineContent = document.get(region.getOffset(), region.getLength());
			return searchIndentation(lineContent);
		} 
		catch (final BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}	
	
	private static String searchIndentation(final String text) throws BadLocationException {
		int i = 0;
		for (; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (!(c == ' ' || c == '\t'))
				break;
		}
		return text.substring(0, i);
	}
	
	public static void positionsToVariables(final List<TextEdit> positions, final TemplateVariable[] variables) {
		final Iterator<TextEdit> iterator = positions.iterator();
		for (final TemplateVariable variable : variables) {
			final int[] offsets = new int[variable.getOffsets().length];
			for (int j = 0; j < offsets.length; j++) {
				offsets[j] = iterator.next().getOffset();
			}
			variable.setOffsets(offsets);
		}
	}
	
	public static List<TextEdit> variablesToPositions(final TemplateVariable[] variables) {
		final List<TextEdit> positions = new ArrayList<TextEdit>(5);
		for (final TemplateVariable variable : variables) {
			final int[] offsets = variable.getOffsets();
			
			// trim positions off whitespace
			final String value = variable.getDefaultValue();
			int wsStart = 0;
			while (wsStart < value.length() && Character.isWhitespace(value.charAt(wsStart)) && !isLineDelimiterChar(value.charAt(wsStart))) {
				wsStart++;
			}
			
			variable.getValues()[0] = value.substring(wsStart);
			
			for (int j = 0; j != offsets.length; j++) {
				offsets[j] += wsStart;
				positions.add(new RangeMarker(offsets[j], 0));
			}
		}
		return positions;
	}
	
	public static TemplateVariable findVariable(final TemplateBuffer buffer, final String variableType) {
		final TemplateVariable[] variables = buffer.getVariables();
		for (final TemplateVariable cand : variables) {
			if (variableType.equals(cand.getType())) {
				return cand;
			}
		}
		return null;
	}
	
	public static String getTodoTaskTag(final StatetProject project) {
		final TaskTagsPreferences taskPrefs = (project != null) ?
				new TaskTagsPreferences(project) :
				new TaskTagsPreferences(PreferencesUtil.getInstancePrefs());
		
		final String[] markers = taskPrefs.getTags();
		
		if (markers == null || markers.length == 0)
			return null;
		return markers[0];
	}
	
	
	private static boolean isLineDelimiterChar(final char c) {
		return (c == '\r' || c == '\n');
	}
	
	/**
	 * Indents each line of the template (document) using the specified indentation (string).
	 * An empty last line is note indented.
	 * 
	 * @param doc document with the template
	 * @param lineIndent string to use as line indentation
	 * @throws BadLocationException
	 */
	public static void indentTemplateDocument(final AbstractDocument doc, final String lineIndent) 
			throws BadLocationException {
		final int lastLine = doc.getNumberOfLines()-1;
		for (int templateLine = 0; templateLine < lastLine; templateLine++) {
			doc.replace(doc.getLineOffset(templateLine), 0, lineIndent);
		}
		final int lineOffset = doc.getLineOffset(lastLine);
		if (lineOffset != doc.getLength()) {
			doc.replace(lineOffset, 0, lineIndent);
			doc.replace(doc.getLength(), 0, doc.getDefaultLineDelimiter());
		}
	}
	
}
