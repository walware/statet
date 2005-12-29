/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.ui.RUiPlugin;
import de.walware.statet.ui.util.ExceptionHandler;


public class LaunchShortcutUtil {


	public static void handleRLaunchException(Exception e, String defaultMessage) {

		CoreException core;
		if (e instanceof CoreException)
			core = (CoreException) e;
		else
			core = new CoreException(new Status(
					IStatus.ERROR,
					RUiPlugin.ID,
					IStatetStatusConstants.LAUNCHING_ERROR,
					defaultMessage,
					e));
		ExceptionHandler.handle(core, RLaunchingMessages.RLaunch_error_description);
	}
	
	
}
