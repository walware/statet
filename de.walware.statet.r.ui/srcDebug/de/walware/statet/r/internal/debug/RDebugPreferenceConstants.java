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

package de.walware.statet.r.internal.debug;

import org.eclipse.core.runtime.Preferences;

import de.walware.statet.r.internal.debug.connector.RConsoleConnector;


public class RDebugPreferenceConstants {


	public static final String ROOT = "de.walware.statet.r.debug";

	
	public static final String R_CONNECTOR = ROOT + ".rconnector";
	
	
	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store the preference store to be initialized
	 */
	public static void initializeDefaultValues(Preferences prefs) {

		prefs.setDefault(R_CONNECTOR, RConsoleConnector.ID);
	}
}
