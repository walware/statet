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

package de.walware.statet.r.core.internal.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.StatetCore;
import de.walware.statet.base.StatetPreferenceConstants;
import de.walware.statet.base.StatetProject;
import de.walware.statet.r.core.RProject;


public class MarkerHandler {

	
	public static final String TASK_MARKER_ID = "de.walware.statet.r.core.TaskMarker";

	private RProject fProject;
	
	private Pattern fTaskTagPattern;
	private Map<String, StatetCore.TaskPriority> fTaskTagMap;

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
	
	public void addTaskMarker(String message, int lineNumber, String match) 
		throws CoreException {

		StatetCore.TaskPriority prio = fTaskTagMap.get(match);
		
		IMarker marker = fResource.createMarker(TASK_MARKER_ID);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.PRIORITY, prio.getMarkerPriority());
		if (lineNumber == -1)
			lineNumber = 1;
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		marker.setAttribute(IMarker.USER_EDITABLE, false);
	}

	public void removeTaskMarkers() throws CoreException {
		//
		fResource.deleteMarkers(TASK_MARKER_ID, false, IResource.DEPTH_INFINITE);
	}

	private void loadTaskPattern() throws CoreException {
		
		fTaskTagPattern = null;
		fTaskTagMap = new HashMap<String, StatetCore.TaskPriority>();
		
		String tagOption;
		String priosOption;
		if (fProject == null) {
			tagOption = StatetCore.getOption(StatetPreferenceConstants.TASK_TAGS);
			priosOption = StatetCore.getOption(StatetPreferenceConstants.TASK_TAGS_PRIORITIES);
		} else {
			StatetProject sp = fProject.getStatetProject();
			tagOption = sp.getOption(StatetPreferenceConstants.TASK_TAGS, true);
			priosOption = sp.getOption(StatetPreferenceConstants.TASK_TAGS_PRIORITIES, true);
		}

		String[] tags = tagOption.split(",");
		String[] prios = priosOption.split(",");
		StringBuilder regex = new StringBuilder("[^\\p{L}](");
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].length() > 0) {
				String tagName = tags[i];
				fTaskTagMap.put(tagName, StatetCore.TaskPriority.valueOf(prios[i]));
				regex.append(tagName);
				regex.append('|');
			}
		}
		
		if (fTaskTagMap.size() > 0) {
			regex.setCharAt(regex.length()-1, ')');
			fTaskTagPattern = Pattern.compile(regex.toString());
		}
	}
	
	public Pattern getTaskPattern() {
		
		return fTaskTagPattern;
	}
	
}
