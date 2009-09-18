/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Adapts CommonTab for nico.
 */
public class CommonTabForNico extends CommonTab {
	
	
	public CommonTabForNico() {
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		final Button button = searchButton((Composite) getControl());
		if (button != null) {
			button.setText("Allocate additional Debug Consoles");
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
		config.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
	}
	
}
