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
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;


public abstract class AbstractRController<
		RunnableAdapterType extends IBasicRAdapter,
		WorkspaceType extends RWorkspace> 
		extends ToolController<RunnableAdapterType, WorkspaceType> {

	
	protected abstract class AbstractRAdapter extends RunnableAdapter implements IBasicRAdapter, ISetupRAdapter {

		protected String fIncompletePromptText = "+ ";
		
		
		public Object getAdapter(Class adapter) {
			
			if (ISetupRAdapter.class.equals(adapter)) {
				return this;
			}
			return null;
		}
		public void setIncompletePromptText(String text) {
			
			fIncompletePromptText = text;
		}
		
		protected Prompt createIncompleteInputPrompt(Prompt previousPrompt, String lastInput) {
			
			return new IncompleteInputPrompt(previousPrompt, lastInput+fLineSeparator, fIncompletePromptText);
		}
		
		public void cancelIncompletePrompt() {
			
			fInfoStream.append(fPrompt.text+"(Input cancelled)"+fLineSeparator, 
					(fCurrentRunnable != null) ? fCurrentRunnable.getType() : SubmitType.TOOLS, fPrompt.meta);
			setPrompt(fDefaultPrompt);
		}
	}
	
	
	public AbstractRController(ToolProcess process) {
		
		super(process);
		
		process.registerFeatureSet(BasicR.FEATURESET_ID);
	}
	
	
	@Override
	public boolean cancel() {
		
		synchronized (getQueue()) {
			if ((getStatus() == ToolStatus.STARTED_IDLING || getStatus() == ToolStatus.STARTED_PAUSED) 
					&& (fWorkspaceData.getPrompt().meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
				((AbstractRAdapter) fRunnableAdapter).cancelIncompletePrompt(); // Interface ?
				return true;
			}
			else {
				return super.cancel();
			}
		}
	}
	
}
