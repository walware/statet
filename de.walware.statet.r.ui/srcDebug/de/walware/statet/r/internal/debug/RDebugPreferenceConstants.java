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

import org.eclipse.core.runtime.preferences.IScopeContext;

import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class RDebugPreferenceConstants {


	public static final String ROOT_QUALIFIER = "de.walware.statet.r.debug";

	public static final String CAT_RCONNECTOR_QUALIFIER = ROOT_QUALIFIER + "/RConnector";

	
	/**
	 * Initializes the default values.
	 */
	public static void initializeDefaultValues(IScopeContext context) {

		RCodeLaunchRegistry.initializeDefaultValues(context);
	}
}
