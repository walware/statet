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

package de.walware.statet.nico.core.internal;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	

	public static String LoadHistory_AllocatingTask_label;
	public static String LoadHistory_error_message;
	public static String SaveHistory_error_message;

	public static String InternalError_UnexpectedException_message;
	
	public static String Runtime_error_CriticalError_message;
	public static String Runtime_error_UnexpectedTermination_message;

	
	private static final String BUNDLE_NAME = Messages.class.getName();
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}
