/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.snippets;

import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.variables.core.DynamicVariable;

import de.walware.statet.r.launching.RCodeLaunching;


public class EchoEnabledVariable extends DynamicVariable {
	
	
	public EchoEnabledVariable() {
		super(RSnippets.ECHO_ENABLED_VARIABLE);
	}
	
	
	@Override
	public String getValue(final String argument) throws CoreException {
		final Boolean echo = PreferencesUtil.getInstancePrefs().getPreferenceValue(
				RCodeLaunching.ECHO_ENABLED_PREF );
		return (echo != null && echo.booleanValue()) ?
				"TRUE" : "FALSE"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
