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

package de.walware.ecommons.debug.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.CommonTab;


public class CheckedCommonTab extends CommonTab {
	
	
	private boolean fLoading;
	
	
	public CheckedCommonTab() {
	}
	
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		fLoading = true;
		try {
			super.initializeFrom(configuration);
		}
		finally {
			fLoading = false;
			scheduleUpdateJob(); // E-3.7 still necessary?
		}
	}
	
	@Override
	protected long getUpdateJobDelay() {
		return (fLoading) ? 10000 : 400;
	}
	
}
