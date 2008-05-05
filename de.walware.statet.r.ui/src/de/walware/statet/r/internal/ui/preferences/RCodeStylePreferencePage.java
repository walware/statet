/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.ext.ui.preferences.PropertyAndPreferencePage;


/**
 * A Property- and PreferencePage for RCodeStyle settings.
 */
public class RCodeStylePreferencePage extends PropertyAndPreferencePage<RCodeStylePreferenceBlock> {
	
	public static final String PREF_ID = "de.walware.statet.r.preferencePages.RCodeStyle"; //$NON-NLS-1$
	
	
	public RCodeStylePreferencePage() {
	}
	
	
	@Override
	protected String getPreferencePageID() {
		return PREF_ID;
	}
	
	@Override
	protected String getPropertyPageID() {
		return null;
	}
	
	@Override
	protected RCodeStylePreferenceBlock createConfigurationBlock() throws CoreException {
		return new RCodeStylePreferenceBlock(getProject(), createStatusChangedListener());
	}
	
	@Override
	protected boolean hasProjectSpecificSettings(final IProject project) {
		return fBlock.hasProjectSpecificOptions(project);
	}
	
}
