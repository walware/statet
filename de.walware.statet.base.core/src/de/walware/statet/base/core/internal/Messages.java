/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core.internal;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String CoreUtility_Build_Job_title;
	public static String CoreUtility_Build_AllTask_name;
	public static String CoreUtility_Build_ProjectTask_name;
	

	private static final String BUNDLE_NAME = Messages.class.getName();
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() { }
}
