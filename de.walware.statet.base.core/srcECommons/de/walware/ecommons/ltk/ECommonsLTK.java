/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import org.eclipse.core.runtime.Plugin;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.internal.core.BaseCorePlugin;


public class ECommonsLTK {
	
	
	public static final String PLUGIN_ID = StatetCore.PLUGIN_ID;
	
	public static Plugin getPlugin() {
		return BaseCorePlugin.getDefault();
	}
	
	
	public static final WorkingContext PERSISTENCE_CONTEXT = new WorkingContext("persistence.default"); //$NON-NLS-1$
	
	public static final WorkingContext EDITOR_CONTEXT = new WorkingContext("editor.default"); //$NON-NLS-1$
	
	
	public static IExtContentTypeManager getExtContentTypeManager() {
		return BaseCorePlugin.getDefault().getContentTypeServices();
	}
	
}
