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

package de.walware.statet.base;


public interface IStatetStatusConstants {

	
	int INTERNAL_ERROR = 10001;
	
	/** Code for errors in (incremental) builders */
	int BUILD_ERROR = 10100;
	
	
	int LAUNCHING_ERROR = 20100;
	
	/** Code for errors when handle LaunchConfigurations */
	int LAUNCHCONFIG_ERROR = 20110;
	
	/** Code for errors when handle Threads/Runnables */
	int RUNTIME_ERROR = 20200;
}
