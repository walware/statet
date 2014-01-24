/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.cmd.ui.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.RefreshTab;

import de.walware.ecommons.debug.ui.CheckedCommonTab;

import de.walware.statet.r.launching.ui.EnvironmentTabForR;
import de.walware.statet.r.launching.ui.REnvTab;


/**
 * 
 */
public class RCmdToolTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	public RCmdToolTabGroup() {
	}
	
	
	@Override
	public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
		final ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new RCmdMainTab(),
				new REnvTab(true, true),
				new RefreshTab(),
				new EnvironmentTabForR(),
				new CheckedCommonTab()
		};
		setTabs(tabs);
	}
	
}
