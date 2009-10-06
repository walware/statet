/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons;

import org.eclipse.core.runtime.IStatus;


public final class ECommons {
	
	
	public static interface IAppEnvironment {
		
		void log(IStatus status);
		
		void addStoppingListener(IDisposable listener);
		void removeStoppingListener(IDisposable listener);
		
	}
	
	
	public static String PLUGIN_ID;
	
	
	private static IAppEnvironment appEnv;
	
	
	public static void init(final String pluginId, final IAppEnvironment env) {
		PLUGIN_ID = pluginId;
		appEnv = env;
	}
	
	public static IAppEnvironment getEnv() {
		return appEnv;
	}
	
	
}
