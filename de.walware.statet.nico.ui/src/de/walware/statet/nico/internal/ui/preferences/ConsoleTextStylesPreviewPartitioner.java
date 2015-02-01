/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import static de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.OUTPUT_INFO_ROOT_KEY;
import static de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.OUTPUT_INPUT_ROOT_KEY;
import static de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.OUTPUT_STANDARD_ERROR_ROOT_KEY;
import static de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.OUTPUT_STANDARD_OUTPUT_ROOT_KEY;
import static de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.OUTPUT_SYSTEM_OUTPUT_ROOT_KEY;

import de.walware.ecommons.text.FixDocumentPartitioner;


public class ConsoleTextStylesPreviewPartitioner extends FixDocumentPartitioner {
	
	
	public static final String[] PARTITIONS= new String[] {
			OUTPUT_INPUT_ROOT_KEY,
			OUTPUT_INFO_ROOT_KEY,
			OUTPUT_STANDARD_OUTPUT_ROOT_KEY,
			OUTPUT_STANDARD_ERROR_ROOT_KEY,
			OUTPUT_SYSTEM_OUTPUT_ROOT_KEY,
	};
	
	
	public ConsoleTextStylesPreviewPartitioner() {
		super(PARTITIONS);
		append(OUTPUT_STANDARD_OUTPUT_ROOT_KEY, 21 + 44 + 22 + 1);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INPUT_ROOT_KEY, 17-2);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INPUT_ROOT_KEY, 4-2);
		append(OUTPUT_STANDARD_OUTPUT_ROOT_KEY, 65 + 65);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INPUT_ROOT_KEY, 13-2);
		append(OUTPUT_STANDARD_OUTPUT_ROOT_KEY, 49 + 48);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INPUT_ROOT_KEY, 5-2);
		append(OUTPUT_STANDARD_ERROR_ROOT_KEY, 29);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INPUT_ROOT_KEY, 3-2);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INFO_ROOT_KEY, 15-2);
		append(OUTPUT_SYSTEM_OUTPUT_ROOT_KEY, 41 + 41 + 11);
		append(OUTPUT_INFO_ROOT_KEY, 2);
		append(OUTPUT_INPUT_ROOT_KEY, 6-2);
		append(OUTPUT_INFO_ROOT_KEY, 31);
		append(OUTPUT_INPUT_ROOT_KEY, 33-31);
	}
	
}
