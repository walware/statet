/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.nico.core.NicoVariables;
import de.walware.statet.nico.core.util.TrackingConfiguration;
import de.walware.statet.nico.ui.util.TrackingConfigurationComposite;

import de.walware.statet.r.console.core.RWorkspace;


public class RTrackingConfigurationComposite extends TrackingConfigurationComposite {
	
	
	static final String TRANSCRIPT_TRACKING_DEFAULT_PATH = "${"+NicoVariables.SESSION_STARTUP_WD_VARNAME+"}/${"+NicoVariables.SESSION_STARTUP_DATE_VARNAME+":yyyy-MM-dd HH-mm-ss}.Rtr"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	
	public RTrackingConfigurationComposite(final Composite parent) {
		super(parent);
	}
	
	@Override
	protected void configure() {
		addSaveTemplate(new SaveTemplate(".Rtr file with timestamp", TRANSCRIPT_TRACKING_DEFAULT_PATH));
	}
	
	
	@Override
	protected void create() {
		super.create();
		
		getPathInput().setShowInsertVariable(true, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS,
				RWorkspace.ADDITIONAL_R_VARIABLES);
	}
	
	@Override
	public void setInput(final TrackingConfiguration config) {
		super.setInput(config);
		final String id = config.getId();
		setLabelEnabled(id.startsWith(RConsoleOptionsTab.CUSTOM_TRACKING_ID_PREFIX));
	}
	
}
