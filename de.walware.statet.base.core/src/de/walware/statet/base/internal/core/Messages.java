/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.core;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String CoreUtility_Build_Job_title;
	public static String CoreUtility_Build_AllTask_name;
	public static String CoreUtility_Build_ProjectTask_name;
	
	public static String SettingsChangeNotifier_Job_title;
	public static String SettingsChangeNotifier_Task_name;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
