/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.walware.statet.nico.ui.util.CommonTabForNico;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;


/**
 *
 */
public class RConsoleTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	public RConsoleTabGroup() {
	}
	
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new RConsoleMainTab(),
				new REnvTab(),
				new EnvironmentTab(),
				new CommonTabForNico()
			};
			setTabs(tabs);
	}
	
}
