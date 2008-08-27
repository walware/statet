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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.core.preferences.TaskTagsPreferences;


public class TemplatesUtil {
	
	
	public static class EvaluatedTemplate {
		
		/**
		 * Content string of template
		 */
		public String content;
		
		/**
		 * Offset of selection to set in template or <code>-1</code>, if not specified
		 */
		public int selectOffset;
		
		/**
		 * Length of selection to set
		 */
		public int selectLength;
		
		
		public EvaluatedTemplate(final TemplateBuffer buffer) {
			content = buffer.getString();
			final TemplateVariable selectStartVariable = findVariable(buffer, StatextCodeTemplatesContextType.SELECT_START_VARIABLE);
			final TemplateVariable selectEndVariable = findVariable(buffer, StatextCodeTemplatesContextType.SELECT_END_VARIABLE);
			selectOffset = (selectStartVariable != null && selectStartVariable.getOffsets().length == 1) ?
					selectStartVariable.getOffsets()[0] : -1;
			selectLength = (selectEndVariable != null && selectEndVariable.getOffsets().length == 1
							&& selectOffset >= 0) ?
					Math.max(selectEndVariable.getOffsets()[0] - selectOffset, 0) : 0;
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
	
}
