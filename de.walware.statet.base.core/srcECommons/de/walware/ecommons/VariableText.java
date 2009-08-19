/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;


/**
 * Allows string substitution of special variables with manual replacement strings
 */
public class VariableText {
	
	
	public static interface LocationProcessor {
		String process(String path) throws CoreException;
	}
	
	private static boolean isEscaped(final String text, final int offset) {
		int count = 1;
		while (offset >= 0) {
			final char c = text.charAt(offset-count);
			if (c == '$') {
				count++;
			}
			else {
				break;
			}
		}
		return (count % 2) == 0;
	}
	
	private static void searchSurrounding(final String text, final int[] region) {
		while (true) {
			if (region[0] == 0) {
				return;
			}
			final char c1 = text.charAt(region[0]-1);
			if (c1 != ':') {
				return;
			}
			final int start = text.lastIndexOf("${", region[0]-1); //$NON-NLS-1$
			if (start < 0 || text.lastIndexOf("}", region[0]-1) > start || isEscaped(text, start)) { //$NON-NLS-1$
				return;
			}
			
			region[0] = start;
			final int end = text.indexOf('}', region[1]);
			if (end >= 0) {
				region[1] = end + 1;
			}
		}
	}
	
	private static final String[] LOCATION_VARIABLES = new String[] {
		"resource_loc", "selected_resource_loc", "container_loc", "project_loc", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	};
	
	
	private String fText;
	
	private int fState;
	
	private final String[] fSpecialVariables;
	private final Set<String> fUnresolvedSpecial = new HashSet<String>();
	private final Map<String, String> fLocationVariables = new HashMap<String, String>();
	
	
	public VariableText(final String text, final String[] specialVariables) {
		fSpecialVariables = specialVariables;
		fText = text;
		fState = 1;
	}
	
	
	public String getText() {
		return fText;
	}
	
	public void performInitialStringSubstitution(final boolean reportUndefinedVariables) throws CoreException {
		if (fState != 1) {
			throw new IllegalStateException();
		}
		String text = fText;
		
		final Map<String, String> specialVariables = new HashMap<String, String>();
		for (final String variableName : fSpecialVariables) {
			final String pattern = "${"+variableName+"}"; //$NON-NLS-1$ //$NON-NLS-2$
			int offset = -1;
			while ((offset = text.indexOf(pattern, offset+1)) >= 0) {
				if (!isEscaped(text, offset)) {
					fUnresolvedSpecial.add(variableName);
					final int[] region = new int[] { offset, offset + pattern.length() };
					searchSurrounding(text, region);
					final String key = "XX-SPECIALVAR-"+fLocationVariables.size()+"-XX"; //$NON-NLS-1$ //$NON-NLS-2$
					specialVariables.put(key, new String(text.substring(region[0], region[1])));
					text = text.substring(0, region[0]) + key + text.substring(region[1], text.length());
				}
			}
		}
		
		text = searchResourceVar(text, reportUndefinedVariables);
		
		text = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(text, reportUndefinedVariables);
		
		for (final Entry<String, String> entry : specialVariables.entrySet()) {
			text = text.replace(entry.getKey(), entry.getValue());
		}
		
		fText = text;
		fState = 2;
	}
	
	private String searchResourceVar(String text, final boolean reportUndefinedVariables) throws CoreException {
		final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
		
		for (final String variableName : LOCATION_VARIABLES) {
			final String pattern = "${"+variableName; //$NON-NLS-1$ 
			int offset = -1;
			while ((offset = text.indexOf(pattern, offset+1)) >= 0) {
				if (!isEscaped(text, offset)) {
					int end = text.indexOf('}', offset);
					int next = offset+1;
					while ((next = text.indexOf('{', next+1)) >= 0 && next < end) {
						end = text.indexOf('}', end+1);
					}
					final String key = "XX-RESOURCEVAR-" + fLocationVariables.size() + "-XX"; //$NON-NLS-1$ //$NON-NLS-2$
					fLocationVariables.put(key, new String(variableManager.performStringSubstitution(text.substring(offset, end+1), reportUndefinedVariables)));
					text = text.substring(0, offset) + key + text.substring(end+1, text.length());
				}
			}
		}
		return text;
	}
	
	public boolean require(final String variableName) {
		return fUnresolvedSpecial.contains(variableName);
	}
	
	public void set(final String variableName, final String value) {
		if (fUnresolvedSpecial.remove(variableName)) {
			final String pattern = "${"+variableName+"}"; //$NON-NLS-1$ //$NON-NLS-2$
			String text = fText;
			
			int offset = -1;
			while ((offset = text.indexOf(pattern, offset+1)) >= 0) {
				if (!isEscaped(text, offset)) {
					text = text.substring(0, offset) + value + text.substring(offset+pattern.length(), text.length());
				}
			}
			
			fText = text;
		}
	}
	
	public void performFinalStringSubstitution(final LocationProcessor locationProcessor) throws CoreException {
		if (fState == 1) {
			performInitialStringSubstitution(true);
		}
		if (fState != 2) {
			throw new IllegalStateException();
		}
		String text = fText;
		
		text = searchResourceVar(text, true);
		text = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(text, true);
		for (final Entry<String, String> entry : fLocationVariables.entrySet()) {
			final String value = (locationProcessor != null) ? locationProcessor.process(entry.getValue()) : entry.getValue();
			text = text.replace(entry.getKey(), value);
		}
		
		fText = text;
		fState = 3;
	}
	
}
