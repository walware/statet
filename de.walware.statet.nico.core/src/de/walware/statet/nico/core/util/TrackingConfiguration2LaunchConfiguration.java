/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import de.walware.statet.nico.core.runtime.SubmitType;


public class TrackingConfiguration2LaunchConfiguration {
	
	public static final String LABEL_KEY = "label"; //$NON-NLS-1$
	public static final String TRACK_STREAM_INFO = "TrackInfo.enable"; //$NON-NLS-1$
	public static final String TRACK_STREAM_INPUT = "TrackInput.enable"; //$NON-NLS-1$
	public static final String TRACK_STREAM_INPUT_HISTORYONLY = "TrackInput.HistoryOnly.enable"; //$NON-NLS-1$
	public static final String TRACK_STREAM_OUTPUT = "TrackOutput.enable"; //$NON-NLS-1$
	public static final String TRACK_STREAM_OUTPUT_TRUNCATE = "TrackOutput.Truncate.enable"; //$NON-NLS-1$
	public static final String TRACK_STREAM_OUTPUT_TRUNCATE_LINES = "TrackOutput.Truncate.lines"; //$NON-NLS-1$
	public static final String SUBMIT_TYPES = "SubmitTypes.ids"; //$NON-NLS-1$
	public static final String FILE_PATH = "File.path"; //$NON-NLS-1$
	public static final String FILE_MODE = "File.mode"; //$NON-NLS-1$
	public static final String PREPEND_TIMESTAMP = "Prepend.Timestamp.enable"; //$NON-NLS-1$
	
	public static final String LOAD_HISTORY = "History.LoadAtStartup.enable"; //$NON-NLS-1$
	
	
	public void load(final TrackingConfiguration trackingConfig, final ILaunchConfiguration launchConfig) throws CoreException {
		final String id = trackingConfig.getId();
		
		trackingConfig.setName(launchConfig.getAttribute(id+'.'+LABEL_KEY, "")); //$NON-NLS-1$
		
		trackingConfig.setTrackStreamInfo(launchConfig.getAttribute(id+'.'+TRACK_STREAM_INFO, false));
		trackingConfig.setTrackStreamInput(launchConfig.getAttribute(id+'.'+TRACK_STREAM_INPUT, false));
		trackingConfig.setTrackStreamInputHistoryOnly(launchConfig.getAttribute(id+'.'+TRACK_STREAM_INPUT_HISTORYONLY, false));
		trackingConfig.setTrackStreamOutput(launchConfig.getAttribute(id+'.'+TRACK_STREAM_OUTPUT, false));
		trackingConfig.setTrackStreamOutputTruncate(launchConfig.getAttribute(id+'.'+TRACK_STREAM_OUTPUT_TRUNCATE, false));
		trackingConfig.setTrackStreamOutputTruncateLines(launchConfig.getAttribute(id+'.'+TRACK_STREAM_OUTPUT_TRUNCATE_LINES, 1000000));
		
		final List<String> submitTypeNames = launchConfig.getAttribute(id+'.'+SUBMIT_TYPES, (List<?>) null);
		if (submitTypeNames != null) {
			final EnumSet<SubmitType> submitTypes = EnumSet.noneOf(SubmitType.class);
			for (final String name : submitTypeNames) {
				submitTypes.add(SubmitType.valueOf(name));
			}
			trackingConfig.setSubmitTypes(submitTypes);
		}
		else {
			trackingConfig.setSubmitTypes(SubmitType.getDefaultSet());
		}
		
		trackingConfig.setFilePath(launchConfig.getAttribute(id+'.'+FILE_PATH, "")); //$NON-NLS-1$
		trackingConfig.setFileMode(launchConfig.getAttribute(id+'.'+FILE_MODE, TrackingConfiguration.DEFAULT_FILE_MODE));
		
		trackingConfig.setPrependTimestamp(launchConfig.getAttribute(id+'.'+PREPEND_TIMESTAMP, false));
		
		if (id.equals(HistoryTrackingConfiguration.HISTORY_TRACKING_ID)
				&& launchConfig instanceof HistoryTrackingConfiguration) {
			((HistoryTrackingConfiguration) launchConfig).setLoadHistory(launchConfig.getAttribute(id+'.'+LOAD_HISTORY, false));
		}
	}
	
	public void save(final TrackingConfiguration trackingConfig, final ILaunchConfigurationWorkingCopy launchConfig) {
		final String id = trackingConfig.getId();
		
		launchConfig.setAttribute(id+'.'+LABEL_KEY, trackingConfig.getName());
		
		launchConfig.setAttribute(id+'.'+TRACK_STREAM_INFO, trackingConfig.getTrackStreamInfo());
		launchConfig.setAttribute(id+'.'+TRACK_STREAM_INPUT, trackingConfig.getTrackStreamInput());
		launchConfig.setAttribute(id+'.'+TRACK_STREAM_INPUT_HISTORYONLY, trackingConfig.getTrackStreamInputHistoryOnly());
		launchConfig.setAttribute(id+'.'+TRACK_STREAM_OUTPUT, trackingConfig.getTrackStreamOutput());
		launchConfig.setAttribute(id+'.'+TRACK_STREAM_OUTPUT_TRUNCATE, trackingConfig.getTrackStreamOutputTruncate());
		launchConfig.setAttribute(id+'.'+TRACK_STREAM_OUTPUT_TRUNCATE_LINES, trackingConfig.getTrackStreamOutputTruncateLines());
		
		final EnumSet<SubmitType> submitTypes = trackingConfig.getSubmitTypes();
		final List<String> submitTypeNames = new ArrayList<String>(submitTypes.size());
		for (final SubmitType submitType : submitTypes) {
			submitTypeNames.add(submitType.name());
		}
		launchConfig.setAttribute(id+'.'+SUBMIT_TYPES, submitTypeNames);
		
		launchConfig.setAttribute(id+'.'+FILE_PATH, trackingConfig.getFilePath());
		launchConfig.setAttribute(id+'.'+FILE_MODE, trackingConfig.getFileMode());
		
		launchConfig.setAttribute(id+'.'+PREPEND_TIMESTAMP, trackingConfig.getPrependTimestamp());
		
		if (id.equals(HistoryTrackingConfiguration.HISTORY_TRACKING_ID)
				&& launchConfig instanceof HistoryTrackingConfiguration) {
			launchConfig.setAttribute(id+'.'+LOAD_HISTORY, ((HistoryTrackingConfiguration) launchConfig).getLoadHistory());
		}
	}
	
}
