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

import org.eclipse.ui.console.IConsoleView;

import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.nico.console.NIConsole;
import de.walware.statet.nico.console.NIConsolePage;


public class RConsolePage extends NIConsolePage {

	public RConsolePage(NIConsole console, IConsoleView view) {
		
		super(console, view);
	}

	
	@Override
	protected IEditorConfiguration getInputEditorConfiguration() {
		
		return new RInputConfiguration();
	}
}
