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

import java.util.EnumSet;

import de.walware.statet.nico.core.runtime.SubmitType;


/**
 * 
 */
public class HistoryTrackingConfiguration extends TrackingConfiguration {
	
	
	public static final String HISTORY_TRACKING_ID = "history"; //$NON-NLS-1$
	
	
	private boolean fLoadHistory;
	
	
	public HistoryTrackingConfiguration(final String id) {
		super(id);
	}
	
	public HistoryTrackingConfiguration(final String id, final HistoryTrackingConfiguration template) {
		super(id, template);
		
		fLoadHistory = template.fLoadHistory;
	}
	
	
	@Override
	public void loadDefaults() {
		super.loadDefaults();
		
		setTrackStreamInfo(false);
		setTrackStreamInput(true);
		setTrackStreamInputHistoryOnly(true);
		setTrackStreamOutput(false);
		setTrackStreamOutputTruncate(false);
		setSubmitTypes(EnumSet.of(SubmitType.CONSOLE));
		
		setLoadHistory(true);
	}
	
	
	public boolean getLoadHistory() {
		return fLoadHistory;
	}
	
	public void setLoadHistory(final boolean enable) {
		final boolean oldValue = fLoadHistory;
		fLoadHistory = enable;
		firePropertyChange("loadHistory", oldValue, enable); //$NON-NLS-1$
	}
	
}
