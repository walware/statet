/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;

import de.walware.statet.nico.ui.util.CommonTabForConsole;

import de.walware.statet.r.launching.ui.EnvironmentTabForR;
import de.walware.statet.r.launching.ui.REnvTab;


public class RConsoleTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	public static class ExtJavaClasspathTab extends JavaClasspathTab {
		
		@Override
		public String getName() {
			return "Java "+super.getName(); //$NON-NLS-1$
		}
		
	}
	
	
	public RConsoleTabGroup() {
	}
	
	
	@Override
	public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
		final RConsoleMainTab mainTab = new RConsoleMainTab();
		final REnvTab renvTab = new REnvTab(true);
		final boolean jdt = true;
		
		final ILaunchConfigurationTab[] tabs = jdt ? new ILaunchConfigurationTab[] {
				mainTab,
				renvTab,
				new RConsoleOptionsTab(),
				new EnvironmentTabForR(),
				
				new ExtJavaJRETab(mainTab, renvTab),
				new ExtJavaClasspathTab(),
				new SourceLookupTab(),
				
				new CommonTabForConsole()
		} : new ILaunchConfigurationTab[] {
				mainTab,
				renvTab,
				new EnvironmentTabForR(),
				
				new CommonTabForConsole()
		};
		setTabs(tabs);
	}
	
}
