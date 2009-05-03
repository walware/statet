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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;


/**
 * Allows string substitution of special variables with manual replacement strings
 * 
 * Resource Variables: resource_loc, container_loc, project_loc
 */
public class FileLocationVariableText {
	
	
	private static final String FILE_LOC = "resource_loc"; //$NON-NLS-1$
	private static final String CONTAINER_LOC = "container_loc"; //$NON-NLS-1$
	private static final String PROJECT_LOC = "project_loc"; //$NON-NLS-1$
	
	private static final Pattern FILE_LOC_PATTERN = Pattern.compile("\\Q${"+FILE_LOC+"}\\E"); //$NON-NLS-1$
	private static final Pattern CONTAINER_LOC_PATTERN = Pattern.compile("\\Q${"+CONTAINER_LOC+"}\\E"); //$NON-NLS-1$
	private static final Pattern PROJECT_LOC_PATTERN = Pattern.compile("\\Q${"+PROJECT_LOC+"}\\E"); //$NON-NLS-1$
	
	private static final String FILE_TEMP = "XXX{"+FILE_LOC+"}XXX"; //$NON-NLS-1$
	private static final String CONTAINER_TEMP = "XXX{"+CONTAINER_LOC+"}XXX"; //$NON-NLS-1$
	private static final String PROJECT_TEMP = "XXX{"+PROJECT_LOC+"}XXX"; //$NON-NLS-1$
	
	private static final Pattern FILE_TEMP_PATTERN = Pattern.compile("\\Q"+FILE_TEMP+"\\E"); //$NON-NLS-1$
	private static final Pattern CONTAINER_TEMP_PATTERN = Pattern.compile("\\Q"+CONTAINER_TEMP+"\\E"); //$NON-NLS-1$
	private static final Pattern PROJECT_TEMP_PATTERN = Pattern.compile("\\Q"+PROJECT_TEMP+"\\E"); //$NON-NLS-1$
	
	
	private String fText;
	private boolean fRequireFile;
	private boolean fRequireContainer;
	private boolean fRequireProject;
	
	
	public FileLocationVariableText(String text) {
		Matcher matcher;
		matcher = FILE_LOC_PATTERN.matcher(text);
		if (matcher.find()) {
			fRequireFile = true;
			text = matcher.replaceAll(FILE_TEMP);
		}
		matcher = CONTAINER_LOC_PATTERN.matcher(text);
		if (matcher.find()) {
			fRequireContainer = true;
			text = matcher.replaceAll(CONTAINER_TEMP);
		}
		matcher = PROJECT_LOC_PATTERN.matcher(text);
		if (matcher.find()) {
			fRequireProject = true;
			text = matcher.replaceAll(PROJECT_TEMP);
		}
		fText = text;
	}
	
	
	public String getText() {
		return fText;
	}
	
	public void performPlatformStringSubstitution(final boolean reportUndefinedVariables) throws CoreException {
		fText = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(fText, reportUndefinedVariables);
	}
	
	public boolean requireFile() {
		return (fRequireFile || fRequireContainer);
	}
	
	public boolean requireProject() {
		return fRequireProject;
	}
	
	public void performResourceStringSubstitution(final String resourcePath, final String projectPath) {
		String text = fText;
		if (fRequireFile) {
			final Matcher matcher = FILE_TEMP_PATTERN.matcher(text);
			text = matcher.replaceAll(Matcher.quoteReplacement(resourcePath));
		}
		if (fRequireContainer) {
			final int idx = Math.max(resourcePath.lastIndexOf('/'), resourcePath.lastIndexOf('\\'));
			final String containerPath = (idx >= 0) ? resourcePath.substring(0, idx+1) : null;
			final Matcher matcher = CONTAINER_TEMP_PATTERN.matcher(text);
			text = matcher.replaceAll(Matcher.quoteReplacement(containerPath));
		}
		if (fRequireFile) {
			final Matcher matcher = PROJECT_TEMP_PATTERN.matcher(text);
			text = matcher.replaceAll(Matcher.quoteReplacement(projectPath));
		}
		fText = text;
	}
	
}
