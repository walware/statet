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

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;


public abstract class AbstractRController extends ToolController {

	
	protected String fLineDelimiter;
	protected String fPromptNewInput;
	protected String fPromptContinueInput;
	
	
	public AbstractRController(ToolProcess process) {
		
		super(process);
		
		fLineDelimiter = System.getProperty("line.separator");
		fPromptNewInput = "> ";
		fPromptContinueInput = "$ ";
	}

	
	@Override
	protected void doOnCommandRun(String command, SubmitType type) {
		
		super.doOnCommandRun(command, type);
		fInfoStream.append(fLineDelimiter, type);
	}
	
	protected void doOnCommandFinished(boolean complete, SubmitType type) {
		
		// TODO cache state / inform console? / print with next input type?
		fInfoStream.append(complete ? fPromptNewInput : fPromptContinueInput, type);
	}
}
