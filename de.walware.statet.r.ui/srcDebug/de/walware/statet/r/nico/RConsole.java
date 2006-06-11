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

package de.walware.statet.r.nico;

import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.console.NIConsole;


public class RConsole extends NIConsole {

	
	public RConsole(ToolProcess process) {

		super(process);
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		
		return new RConsolePage(this, view);
	}
}
