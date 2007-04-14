/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import org.eclipse.ui.console.IConsoleView;

import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.ui.console.InputGroup;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsolePage;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IncompleteInputPrompt;


public class RConsolePage extends NIConsolePage {

	public RConsolePage(NIConsole console, IConsoleView view) {
		
		super(console, view);
	}

	
	@Override
	protected IEditorConfiguration getInputEditorConfiguration() {
		
		return new RInputConfiguration();
	}
	
	@Override
	protected InputGroup createInputGroup() {
		
		return new InputGroup(this) {
			
			@Override
			protected void onPromptUpdate(Prompt prompt) {

				if ((prompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
					IncompleteInputPrompt p = (IncompleteInputPrompt) prompt;
					fDocument.setPrefix(p.previousInput);
				}
				else {
					fDocument.setPrefix(""); //$NON-NLS-1$
				}
			}
				
		};
	}
}
