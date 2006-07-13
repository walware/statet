/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;


public abstract class AbstractRController<
		RunnableAdapterType extends IBasicRAdapter,
		WorkspaceType extends RWorkspace> 
		extends ToolController<RunnableAdapterType, WorkspaceType> {

	
	protected abstract class AbstractRAdapter extends RunnableAdapter {

		protected String fIncompletePromptText = "$ ";
		
		public void setIncompletePromptText(String text) {
			
			fIncompletePromptText = text;
		}
		
		public Prompt createIncompleteInputPrompt(Prompt previousPrompt, String lastInput) {
			
			return new IncompleteInputPrompt(previousPrompt, lastInput+fLineSeparator, fIncompletePromptText);
		}
	}
	
	
	public AbstractRController(ToolProcess process) {
		
		super(process);
	}
}
