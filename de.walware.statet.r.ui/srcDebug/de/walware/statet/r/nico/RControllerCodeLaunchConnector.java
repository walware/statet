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
import de.walware.statet.nico.ToolRegistry;
import de.walware.statet.nico.console.NIConsole;
import de.walware.statet.nico.runtime.SubmitType;
import de.walware.statet.nico.runtime.ToolController;
import de.walware.statet.r.launching.IRCodeLaunchConnector;


public class RControllerCodeLaunchConnector implements IRCodeLaunchConnector {

	public void submit(String[] rCommands) throws CoreException {
		
		ToolRegistry reg = ToolRegistry.getRegistry(StatetPlugin.getActivePage());
		ToolController controller = reg.getActiveToolSession().getController();
		if (controller != null) {
			controller.submit(rCommands, SubmitType.EDITOR);
		}
		else {
			// search controller / message?
		}
	}

	public void gotoConsole() throws CoreException {
		
		ToolRegistry reg = ToolRegistry.getRegistry(StatetPlugin.getActivePage());
		NIConsole console = reg.getActiveToolSession().getConsole();
		if (console != null) {
			console.show(true);
		}
		else {
			// search console / message?
		}
	}

}
