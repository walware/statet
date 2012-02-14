/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.lauching;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.launching.core.ILaunchDelegateAddon;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.AbstractRDbgController;


public class DebugLaunchDelegateAddon implements ILaunchDelegateAddon {
	
	
	public DebugLaunchDelegateAddon() {
	}
	
	
	@Override
	public void init(final ILaunchConfiguration configuration, final String mode,
			final AbstractRController controller, final IProgressMonitor monitor) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)
				&& controller instanceof AbstractRDbgController) {
			RDebugModel.createRDebugTarget((AbstractRDbgController) controller);
		}
	}
	
}
