/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui;

import java.util.EnumSet;

import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.preferences.core.Preference.IntPref;
import de.walware.ecommons.preferences.ui.RGBPref;

import de.walware.statet.nico.core.runtime.SubmitType;


public class NicoUIPreferences {
	
	
	public static final String QUALIFIER= NicoUI.PLUGIN_ID + "/console"; //$NON-NLS-1$
	
	public static final String OUTPUT_QUALIFIER= QUALIFIER + "/output"; //$NON-NLS-1$
	
	
	public static final Preference<Integer> OUTPUT_CHARLIMIT_PREF= new IntPref(
			OUTPUT_QUALIFIER, "CharLimit.num"); //$NON-NLS-1$
	
	
	public static final String OUTPUT_INFO_STREAM_ROOT_KEY= "InfoStream"; //$NON-NLS-1$
	public static final String OUTPUT_STD_INPUT_STREAM_ROOT_KEY= "StdInputStream"; //$NON-NLS-1$
	public static final String OUTPUT_STD_OUTPUT_ROOT_KEY= "StdOutputStream"; //$NON-NLS-1$
	public static final String OUTPUT_STD_ERROR_STREAM_ROOT_KEY= "StdErrorStream"; //$NON-NLS-1$
	public static final String OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY= "SystemOutputStream"; //$NON-NLS-1$
	
	public static final String OUTPUT_FILTER_SUBMITTYPES_INCLUDE_KEY= "Filter.SubmitTypes.include"; //$NON-NLS-1$
	public static final Preference<EnumSet<SubmitType>> OUTPUT_FILTER_SUBMITTYPES_INCLUDE_PREF= new Preference.EnumSetPref<>(
			OUTPUT_QUALIFIER, OUTPUT_FILTER_SUBMITTYPES_INCLUDE_KEY, SubmitType.class );
	
	public static final String OUTPUT_OTHER_TASKS_BACKGROUND_ROOT_KEY= "OtherTasks.Background"; //$NON-NLS-1$
	public static final String OUTPUT_OTHER_TASKS_BACKGROUND_COLOR_KEY= OUTPUT_OTHER_TASKS_BACKGROUND_ROOT_KEY + ".color"; //$NON-NLS-1$
	public static final Preference<RGB> OUTPUT_OTHER_TASKS_BACKGROUND_COLOR_PREF= new RGBPref(
			OUTPUT_QUALIFIER, OUTPUT_OTHER_TASKS_BACKGROUND_COLOR_KEY );
	
	
}
