/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.walware.statet.nico.ui.util.CommonTabForConsole;

import de.walware.statet.r.launching.ui.REnvTab;


public class RRemoteConsoleTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	public RRemoteConsoleTabGroup() {
	}
	
	
	@Override
	public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
		final ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new RRemoteConsoleMainTab(),
				new REnvTab(false, false),
				new RConsoleOptionsTab(),
				
				new CommonTabForConsole()
		};
		setTabs(tabs);
	}
	
}
