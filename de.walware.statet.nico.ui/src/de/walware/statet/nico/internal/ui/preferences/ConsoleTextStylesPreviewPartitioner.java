/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import de.walware.ecommons.text.FixDocumentPartitioner;

import de.walware.statet.nico.ui.NicoUIPreferences;


class ConsoleTextStylesPreviewPartitioner extends FixDocumentPartitioner {
	
	
	public static final String PARTITIONING= "ConsolePreview_walware";
	
	
	public static final String[] PARTITIONS= new String[] {
			NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY,
			NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY,
			NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY,
			NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY,
			NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY,
	};
	
	
	public ConsoleTextStylesPreviewPartitioner() {
		super(PARTITIONS);
		append(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY, 21 + 44 + 22 + 1);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 17-2);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 4-2);
		append(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY, 65 + 65);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 13-2);
		append(NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY, 49 + 48);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 5-2);
		append(NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY, 29);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 3-2);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 15-2);
		append(NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY, 41 + 41 + 11);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 2);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 6-2);
		append(NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY, 31);
		append(NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY, 33-31);
	}
	
}
