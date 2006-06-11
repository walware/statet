/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.ToolRegistry;
import de.walware.statet.nico.ui.ToolSessionInfo;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.r.launching.IRCodeLaunchConnector;


public class RControllerCodeLaunchConnector implements IRCodeLaunchConnector {

	public void submit(String[] rCommands) throws CoreException {
		
		ToolSessionInfo info = ToolRegistry.getRegistry().getActiveToolSession(
				StatetPlugin.getActivePage());
		ToolProcess process = info.getProcess();
		if (process != null) {
			ToolController controller = process.getController();
			if (controller != null) {
				controller.submit(rCommands, SubmitType.EDITOR);
				return;
			}
		}
		// search controller / message?
	}

	public void gotoConsole() throws CoreException {
		
		ToolSessionInfo info = ToolRegistry.getRegistry().getActiveToolSession(
				StatetPlugin.getActivePage());
		NIConsole console = info.getConsole();
		if (console != null) {
			console.show(true);
		}
		else {
			// search console / message?
		}
	}

}
