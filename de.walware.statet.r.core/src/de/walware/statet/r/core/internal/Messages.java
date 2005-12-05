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

package de.walware.statet.r.core.internal;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = Messages.class.getName();


	public static String Builder_error_OnStartup_message;
	
	public static String Builder_error_MultipleErrors_message;
	
	public static String Builder_error_UnsupportedEncoding_message;
	public static String Builder_error_IOReadingFile_message;


	

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}
