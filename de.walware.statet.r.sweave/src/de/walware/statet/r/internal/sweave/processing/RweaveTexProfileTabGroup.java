/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.processing;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.walware.ecommons.debug.ui.CheckedCommonTab;



/**
 * Tab group for Sweave (LaTeX/R) output creation toolchain.
 */
public class RweaveTexProfileTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	
	public RweaveTexProfileTabGroup() {
	}
	
	public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
		final ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new RweaveTab(),
				new TexTab(),
				new PreviewTab(),
				new CheckedCommonTab()
			};
			setTabs(tabs);
	}
	
}
