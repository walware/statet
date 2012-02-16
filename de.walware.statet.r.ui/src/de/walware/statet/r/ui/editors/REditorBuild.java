/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import de.walware.ecommons.preferences.Preference.BooleanPref;

import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;


public class REditorBuild {
	
	
	public static final String GROUP_ID = "r/r.editor/build.options"; //$NON-NLS-1$
	
	
	public static final BooleanPref PROBLEMCHECKING_ENABLED_PREF = new BooleanPref(
			RUIPreferenceInitializer.REDITOR_NODE, "ProblemChecking.enabled"); //$NON-NLS-1$
	
	
	public static final String ERROR_ANNOTATION_TYPE = "de.walware.statet.r.ui.error"; //$NON-NLS-1$
	public static final String WARNING_ANNOTATION_TYPE = "de.walware.statet.r.ui.warning"; //$NON-NLS-1$
	public static final String INFO_ANNOTATION_TYPE = "de.walware.statet.r.ui.info"; //$NON-NLS-1$
	
}
