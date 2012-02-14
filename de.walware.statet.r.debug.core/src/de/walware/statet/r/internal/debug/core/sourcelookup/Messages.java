/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core.sourcelookup;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String REnvLibraryPathSourceContainer_name;
	public static String REnvLibraryPathSourceContainer_error_InvalidConfiguration_message;
	public static String REnvLibraryPathSourceContainer_error_REnvNotAvailable_message;
	public static String RLibrarySourceContainer_error_InvalidConfiguration_message;
	public static String RLibrarySourceContainer_name_UnresolvablePath_message;
	public static String AllRProjectsSourceContainer_name;
	public static String AllRProjectsSourceContainer_error_InvalidConfiguration_message;
	public static String RProjectSourceContainer_error_InvalidConfiguration_message;
	
	public static String RSourcePathComputer_error_REnvNotFound_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
