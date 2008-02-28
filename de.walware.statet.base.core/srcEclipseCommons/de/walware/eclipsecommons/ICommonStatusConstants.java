/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons;


public interface ICommonStatusConstants {
	
	int INTERNAL_ERROR = 100;
	int INTERNAL_PREF = 200;
	int INTERNAL_PREF_PERSISTENCE = 1001;
	int INTERNAL_TEMPLATE = 2000;
	
	int LAUNCHING = 20100;
	
	/** Code for errors when handle LaunchConfigurations */
	int LAUNCHCONFIG_ERROR = 20110;
	
	int IO_ERROR = 30100;
	
	/** Status Code for common errors in (incremental) builders */
	int BUILD_ERROR = 40100;
	
}
