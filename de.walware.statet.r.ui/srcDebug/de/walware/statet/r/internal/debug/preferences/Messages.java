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

package de.walware.statet.r.internal.debug.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = Messages.class.getName();


	public static String RInteraction_description;
	public static String RInteraction_RConnector;
	public static String RInteraction_RConnector_Description_label;


	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}
