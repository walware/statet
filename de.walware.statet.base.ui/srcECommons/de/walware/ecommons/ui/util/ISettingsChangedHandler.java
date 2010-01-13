/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

import java.util.Map;
import java.util.Set;


/**
 * 
 */
public interface ISettingsChangedHandler {
	
	
	public static final String PREFERENCEACCESS_KEY = "context.PrefAccess"; //$NON-NLS-1$
	
	public static final String VIEWER_KEY = "context.Viewer"; //$NON-NLS-1$
	
	
	public void handleSettingsChanged(Set<String> groupIds, Map<String, Object> options);
	
}
