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

package de.walware.statet.nico.ui.util;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.debug.ui.CheckedCommonTab;

import de.walware.statet.nico.core.runtime.LogRuntimeProcessFactory;


/**
 * Adapts CommonTab for nico.
 */
public class CommonTabForConsole extends CheckedCommonTab {
	
	
	public CommonTabForConsole() {
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		final Button button = searchButton((Composite) getControl());
		if (button != null) {
			button.setText("Allocate additional Error Log Consoles");
		}
	}
	
	private static Button searchButton(final Composite composite) {
		final Control[] children = composite.getChildren();
		for (final Control control : children) {
			if (control instanceof Button) {
				if (((Button) control).getText().equals(LaunchConfigurationsMessages.CommonTab_5)) {
					return (Button) control;
				}
			}
			else if (control instanceof Composite) {
				final Button button = searchButton((Composite) control);
				if (button != null) {
					return button;
				}
			}
		}
		return null;
	}
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
		config.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, (String) null);
		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, LogRuntimeProcessFactory.FACTORY_ID);
	}
	
	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		config.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, (String) null);
		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, LogRuntimeProcessFactory.FACTORY_ID);
	}
	
}
