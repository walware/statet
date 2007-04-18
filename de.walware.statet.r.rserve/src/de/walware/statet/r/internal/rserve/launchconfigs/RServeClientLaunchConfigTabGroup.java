/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.rserve.launchconfigs;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;


public class RServeClientLaunchConfigTabGroup extends AbstractLaunchConfigurationTabGroup {

	
	public RServeClientLaunchConfigTabGroup() {
	}

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {

		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new RServeClientMainTab(),

				new CommonTab() {
					@Override
					public void setDefaults(ILaunchConfigurationWorkingCopy config) {
						super.setDefaults(config);
						config.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
						config.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
					}
				},
				
		};
		setTabs(tabs);
	}

}
