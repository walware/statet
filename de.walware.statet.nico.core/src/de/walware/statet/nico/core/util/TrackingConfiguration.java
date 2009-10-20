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

package de.walware.statet.nico.core.util;

import java.util.EnumSet;

import org.eclipse.core.filesystem.EFS;

import de.walware.ecommons.AbstractSettingsModelObject;

import de.walware.statet.nico.core.runtime.SubmitType;


public class TrackingConfiguration extends AbstractSettingsModelObject {
	
	
	static int DEFAULT_FILE_MODE = EFS.APPEND;
	
	
	private String fId;
	
	private String fName;
	
	private boolean fTrackStreamInfo;
	private boolean fTrackStreamInput;
	private boolean fTrackStreamInputHistoryOnly;
	private boolean fTrackStreamOutput;
	private boolean fTrackStreamOutputTruncate;
	private int fTrackStreamOutputTruncateLines;
	
	private EnumSet<SubmitType> fSubmitTypes;
	
	private String fFilePath;
	private int fFileMode;
	
	private boolean fPrependTimestamp;
	
	
	public TrackingConfiguration(final String id) {
		fId = id;
		
		fName = ""; //$NON-NLS-1$
		
		loadDefaults();
	}
	
	public TrackingConfiguration(final String id, final TrackingConfiguration template) {
		fId = id;
		
		fName = template.fName;
		fTrackStreamInfo = template.fTrackStreamInfo;
		fTrackStreamInput = template.fTrackStreamInput;
		fTrackStreamInputHistoryOnly = template.fTrackStreamInputHistoryOnly;
		fTrackStreamOutput = template.fTrackStreamOutput;
		fTrackStreamOutputTruncate = template.fTrackStreamOutputTruncate;
		fTrackStreamOutputTruncateLines = template.fTrackStreamOutputTruncateLines;
		fSubmitTypes = template.fSubmitTypes;
		fFilePath = template.fFilePath;
		fFileMode = template.fFileMode;
		fPrependTimestamp = template.fPrependTimestamp;
	}
	
	
	public void loadDefaults() {
		setTrackStreamInfo(true);
		setTrackStreamInput(true);
		setTrackStreamInputHistoryOnly(false);
		setTrackStreamOutput(true);
		setTrackStreamOutputTruncate(false);
		setTrackStreamOutputTruncateLines(50);
		setSubmitTypes(SubmitType.getDefaultSet());
		setFilePath(""); //$NON-NLS-1$
		setFileMode(DEFAULT_FILE_MODE);
		setPrependTimestamp(true);
	}
	
	
	public String getId() {
		return fId;
	}
	
	public String getName() {
		return fName;
	}
	
	public void setName(final String label) {
		final String oldValue = fName;
		fName = label;
		firePropertyChange("name", oldValue, label);
	}
	
	public boolean getTrackStreamInfo() {
		return fTrackStreamInfo;
	}
	
	public void setTrackStreamInfo(final boolean enable) {
		final boolean oldValue = fTrackStreamInfo;
		fTrackStreamInfo = enable;
		firePropertyChange("trackStreamInfo", oldValue, enable);
	}
	
	public boolean getTrackStreamInput() {
		return fTrackStreamInput;
	}
	
	public void setTrackStreamInput(final boolean enable) {
		final boolean oldValue = fTrackStreamInput;
		fTrackStreamInput = enable;
		firePropertyChange("trackStreamInput", oldValue, enable);
	}
	
	public boolean getTrackStreamInputHistoryOnly() {
		return fTrackStreamInputHistoryOnly;
	}
	
	public void setTrackStreamInputHistoryOnly(final boolean enable) {
		final boolean oldValue = fTrackStreamInputHistoryOnly;
		fTrackStreamInputHistoryOnly = enable;
		firePropertyChange("trackStreamInputHistoryOnly", oldValue, enable);
	}
	
	public boolean getTrackStreamOutput() {
		return fTrackStreamOutput;
	}
	
	public void setTrackStreamOutput(final boolean enable) {
		final boolean oldValue = fTrackStreamOutput;
		fTrackStreamOutput = enable;
		firePropertyChange("trackStreamOutput", oldValue, enable);
	}
	
	public boolean getTrackStreamOutputTruncate() {
		return fTrackStreamOutputTruncate;
	}
	
	public void setTrackStreamOutputTruncate(final boolean enable) {
		final boolean oldValue = fTrackStreamOutputTruncate;
		fTrackStreamOutputTruncate = enable;
		firePropertyChange("trackStreamOutputTruncate", oldValue, enable);
	}
	
	public int getTrackStreamOutputTruncateLines() {
		return fTrackStreamOutputTruncateLines;
	}
	
	public void setTrackStreamOutputTruncateLines(final int lines) {
		final int oldValue = fTrackStreamOutputTruncateLines;
		fTrackStreamOutputTruncateLines = lines;
		firePropertyChange("trackStreamOutputTruncateLines", oldValue, lines);
	}
	
	public EnumSet<SubmitType> getSubmitTypes() {
		return fSubmitTypes;
	}
	
	public void setSubmitTypes(final EnumSet<SubmitType> typesToInclude) {
		final EnumSet<SubmitType> oldValue = fSubmitTypes;
		fSubmitTypes = typesToInclude;
		firePropertyChange("submitTypes", oldValue, typesToInclude);
	}
	
	public String getFilePath() {
		return fFilePath;
	}
	
	public void setFilePath(final String path) {
		final String oldValue = fFilePath;
		fFilePath = path;
		firePropertyChange("filePath", oldValue, path);
	}
	
	public int getFileMode() {
		return fFileMode;
	}
	
	public void setFileMode(final int mode) {
		final int oldValue = fFileMode;
		fFileMode = mode;
		firePropertyChange("fileMode", oldValue, mode);
	}
	
	public String getFileEncoding() {
		return "UTF-8"; //$NON-NLS-1$
	}
	
	
	public boolean getPrependTimestamp() {
		return fPrependTimestamp;
	}
	
	public void setPrependTimestamp(final boolean enable) {
		final boolean oldValue = fPrependTimestamp;
		fPrependTimestamp = enable;
		firePropertyChange("prependTimestamp", oldValue, enable);
	}
	
	
	@Override
	public int hashCode() {
		return fId.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (this == obj ||
				(obj instanceof TrackingConfiguration && fId.equals(((TrackingConfiguration) obj).fId)) );
	}
	
}
