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

package de.walware.statet.nico.ui.util;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Adapts CommonTab for nico.
 */
public class CommonTabForNico extends CommonTab {
	
	@Override
	protected void createLaunchInBackgroundComponent(Composite parent) {
		super.createLaunchInBackgroundComponent(parent);
		Control[] children = parent.getChildren();
		children[children.length-1].setEnabled(false);
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
		config.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
	}

}
