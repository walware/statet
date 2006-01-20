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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;

import de.walware.eclipsecommon.preferences.PreferencesUtil;
import de.walware.statet.base.core.StatetProject;
import de.walware.statet.base.core.preferences.TaskTagsPreferences;


public class TemplatesUtil {


	public static String searchIndentation(IDocument document, int offset) {
		
		try {
			IRegion region = document.getLineInformationOfOffset(offset);
			String lineContent = document.get(region.getOffset(), region.getLength());
			return searchIndentation(lineContent);
		} 
		catch (BadLocationException e) {
			return "";
		}
	}	

	public static String searchMultilineIndentation(IDocument document) {
		
		try {
			String ind = null;

			int n = document.getNumberOfLines();
			for (int line = 0; line < n; line++) {
				IRegion lineRegion = document.getLineInformation(line);
				if (lineRegion.getLength() > 0) {
					String lineInd = searchIndentation(document.get(lineRegion.getOffset(), lineRegion.getLength()));
					if (lineRegion.getLength() != lineInd.length())
						ind = (ind == null) ? lineInd : getEqualStart(ind, lineInd);
				}
			}
			if (ind != null) 
				return ind;
		} 
		catch (BadLocationException e) {
		}
		return "";
	}
	
	private static String searchIndentation(String text) throws BadLocationException {
		
		int i = 0;
		for (; i < text.length(); i++) {
			char c = text.charAt(i);
			if (!(c == ' ' || c == '\t'))
				break;
		}
		return text.substring(0, i);
	}
	
	/**
	 * Vergleicht zwei Strings und gibt den gemeinsamen Beginn zurück
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static String getEqualStart(String s1, String s2) {
		
		int n = Math.min(s1.length(), s2.length());
		for (int i = 0; i < n; i++) {
			if (s1.charAt(i) != s2.charAt(i))
				return s1.substring(0, i);
		}
		return s1.substring(0, n);
	}
	
	
	
	public static void positionsToVariables(List<TextEdit> positions, TemplateVariable[] variables) {

		Iterator iterator = positions.iterator();
		
		for (int i= 0; i != variables.length; i++) {
		    TemplateVariable variable = variables[i];
		    
			int[] offsets= new int[variable.getOffsets().length];
			for (int j= 0; j != offsets.length; j++)
				offsets[j]= ((TextEdit) iterator.next()).getOffset();
			
		 	variable.setOffsets(offsets);   
		}
	}	

	public static List<TextEdit> variablesToPositions(TemplateVariable[] variables) {
		
   		List<TextEdit> positions = new ArrayList<TextEdit>(5);
		for (int i= 0; i != variables.length; i++) {
		    int[] offsets= variables[i].getOffsets();
		    
		    // trim positions off whitespace
		    String value = variables[i].getDefaultValue();
		    int wsStart = 0;
		    while (wsStart < value.length() && Character.isWhitespace(value.charAt(wsStart)) && !isLineDelimiterChar(value.charAt(wsStart)))
		    	wsStart++;
		    
		    variables[i].getValues()[0]= value.substring(wsStart);
		    
		    for (int j= 0; j != offsets.length; j++) {
		    	offsets[j] += wsStart;
				positions.add(new RangeMarker(offsets[j], 0));
		    }
		}
		return positions;	    
	}

	
	public static String getLineSeparator(IProject project) {
		
		IScopeContext[] scopeContext;
		if (project != null) {
			scopeContext = new IScopeContext[] { new ProjectScope(project.getProject()), new InstanceScope() };
			String lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
			if (lineSeparator != null)
				return lineSeparator;
		}
		
		return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String evaluateTemplate(StatextCodeTemplatesContext context, Template template) throws CoreException {

		TemplateBuffer buffer;
		try {
			buffer = context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (buffer == null)
			return null;
		String str = buffer.getString();
//		if (Strings.containsOnlyWhitespaces(str)) {
//			return null;
//		}
		return str;
	}

	public static String getTodoTaskTag(StatetProject project) {
		
		TaskTagsPreferences taskPrefs = (project != null) ?
			TaskTagsPreferences.load(project) :
			TaskTagsPreferences.load(PreferencesUtil.getInstancePrefs());
		
		String[] markers = taskPrefs.getTags();
		
		if (markers == null || markers.length == 0)
			return null;
		return markers[0];
	}

	
	private static boolean isLineDelimiterChar(char c) {
		
		return (c == '\r' || c == '\n');
	}
	
}
