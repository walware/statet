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

package de.walware.statet.nico.ui.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.ui.statushandlers.StatusManager;


/**
 * Interface Core <-> UI for status handler.
 */
public class WorkbenchStatusHandler implements IStatusHandler {
	
	
	@Override
	public Object handleStatus(final IStatus status, final Object source) throws CoreException {
		StatusManager.getManager().handle(status,
				StatusManager.LOG | StatusManager.SHOW);
		return Boolean.TRUE;
	}
	
}
