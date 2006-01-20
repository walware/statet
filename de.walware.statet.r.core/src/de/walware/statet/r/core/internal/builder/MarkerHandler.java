/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.internal.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.core.preferences.TaskTagsPreferences;
import de.walware.statet.base.core.preferences.TaskTagsPreferences.TaskPriority;
import de.walware.statet.r.core.RProject;


public class MarkerHandler {

	
	public static final String TASK_MARKER_ID = "de.walware.statet.r.core.TaskMarker";

	private RProject fProject;
	
	private Pattern fTaskTagPattern;
	private Map<String, TaskPriority> fTaskTagMap;

	private IResource fResource;
	
	
	public MarkerHandler(RProject project) throws CoreException {
		
		fProject = project;
		loadTaskPattern();
	}
	
	public void setup(IResource resource) {
		
		fResource = resource;
	}
	
	public void clean() throws CoreException {
		
		removeTaskMarkers();
	}
	
	public void addTaskMarker(String message, int offset, int lineNumber, String match) 
	
		throws CoreException {

		TaskPriority prio = fTaskTagMap.get(match);
		
		IMarker marker = fResource.createMarker(TASK_MARKER_ID);
		
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

	private void loadTaskPattern() throws CoreException {
		
		fTaskTagPattern = null;
		fTaskTagMap = null;
		
		TaskTagsPreferences taskPrefs = TaskTagsPreferences.load(fProject);
		String[] tags = taskPrefs.getTags();
		TaskPriority[] prios = taskPrefs.getPriorities();
		
		if (tags.length == 0)
			return;

		fTaskTagMap = new HashMap<String, TaskPriority>(tags.length);
		StringBuilder regex = new StringBuilder("[^\\p{L}](");
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].length() > 0) {
				String tagName = tags[i];
				regex.append(tagName);
				regex.append('|');
				fTaskTagMap.put(tagName, prios[i]);
			}
		}
		if (fTaskTagMap.size() > 0) {
			regex.setCharAt(regex.length()-1, ')');
			fTaskTagPattern = Pattern.compile(regex.toString());
		}
	}
	
	
	public void checkForTasks(String content, int offset, ILineResolver lines) throws CoreException {

		Matcher matcher = fTaskTagPattern.matcher(content);
		if (matcher.find()) {
			int start = matcher.start(1);
			String text = content.substring(start);
			addTaskMarker(text, offset+start, lines.getLineOfOffset(offset)+1, matcher.group(1));
		}
		
	}
}
