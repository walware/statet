/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.text.ILineInformation;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.base.core.preferences.TaskTagsPreferences.TaskPriority;

import de.walware.statet.r.core.RProject;


public class TaskMarkerHandler {
	
	
	public static final String TASK_MARKER_ID = "de.walware.statet.r.markers.Tasks"; //$NON-NLS-1$
	
	
	private Pattern fTaskTagPattern;
	private Map<String, TaskPriority> fTaskTagMap;
	
	private IResource fResource;
	
	
	public TaskMarkerHandler() {
	}
	
	
	public void init(final RProject project) throws CoreException {
		loadTaskPattern(project);
	}
	
	public void setup(final IResource resource) {
		fResource = resource;
	}
	
	public void addTaskMarker(final String message, final int offset, int lineNumber, final String match) 
		throws CoreException {
		
		final TaskPriority prio = fTaskTagMap.get(match);
		
		final IMarker marker = fResource.createMarker(TASK_MARKER_ID);
		
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.PRIORITY, prio.getMarkerPriority());
		if (lineNumber == -1) {
			lineNumber = 1;
		}
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		if (offset != -1) {
			marker.setAttribute(IMarker.CHAR_START, offset);
			marker.setAttribute(IMarker.CHAR_END, offset+message.length());
		}		
		marker.setAttribute(IMarker.USER_EDITABLE, false);
	}
	
	public void removeTaskMarkers() throws CoreException {
		fResource.deleteMarkers(TASK_MARKER_ID, false, IResource.DEPTH_INFINITE);
	}
	
	private void loadTaskPattern(final RProject project) throws CoreException {
		fTaskTagPattern = null;
		fTaskTagMap = null;
		
		final TaskTagsPreferences taskPrefs = new TaskTagsPreferences(project.getPrefs());
		final String[] tags = taskPrefs.getTags();
		final TaskPriority[] prios = taskPrefs.getPriorities();
		
		if (tags.length == 0)
			return;
		
		fTaskTagMap = new HashMap<String, TaskPriority>(tags.length);
		final String separatorRegex = "[^\\p{L}\\p{N}]"; //$NON-NLS-1$
		final StringBuilder regex = new StringBuilder(separatorRegex);
		regex.append('('); 
		for (int i = 0; i < tags.length; i++) {
			final String tagName = tags[i];
			regex.append(Pattern.quote(tagName));
			regex.append('|'); 
			fTaskTagMap.put(tagName, prios[i]);
		}
		regex.setCharAt(regex.length()-1, ')'); 
		regex.append("(?:\\z|").append(separatorRegex).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		fTaskTagPattern = Pattern.compile(regex.toString());
	}
	
	
	public void checkForTasks(final String content, final int offset, final ILineInformation lines) throws CoreException {
		if (fTaskTagPattern != null) {
			final Matcher matcher = fTaskTagPattern.matcher(content);
			if (matcher.find()) {
				final int start = matcher.start(1);
				final String text = new String(content.substring(start));
				addTaskMarker(text, offset+start, lines.getLineOfOffset(offset)+1, matcher.group(1));
			}
		}
	}
	
}
