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

package de.walware.eclipsecommons.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;


/**
 * Interface to access preferences using <code>Preference</code>.
 * <p>
 * In most cases, you can take the Objects from <code>PreferencesUtil</code>.
 */
public interface IPreferenceAccess {
	
	/**
	 * Returns the preference value of the specified <code>Preference</code>
	 * @param <T> type for which the <code>Preference</code> is designed.
	 * @param key
	 * @return value
	 */
	<T> T getPreferenceValue(Preference<T> key);
	
	/**
	 * Returns the preference nodes of all scopes used by this agent.
	 * 
	 * @param nodeQualifier the qualifier of the node.
	 * @return array of preference nodes.
	 */
	IEclipsePreferences[] getPreferenceNodes(String nodeQualifier);
	
	/**
	 * Returns the scopes used by this agent.
	 * @return
	 */
	IScopeContext[] getPreferenceContexts();
	
}
