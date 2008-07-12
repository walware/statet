/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;


public abstract class AbstractRController
		extends ToolController<RWorkspace>
		implements IBasicRAdapter, ISetupRAdapter {
	
	
	protected String fIncompletePromptText;
	protected String fDefaultPromptText;
	
	
	public AbstractRController(final ToolProcess process) {
		super(process);
		process.registerFeatureSet(BasicR.FEATURESET_ID);
	}
	
	
	@Override
	protected IToolRunnable createCancelPostRunnable(final int options) {
		return new IToolRunnable() {
			public SubmitType getSubmitType() {
				return SubmitType.OTHER;
			}
			public String getTypeId() {
				return "common/cancel/post"; //$NON-NLS-1$
			}
			public String getLabel() {
				return "Reset prompt";
			}
			public void changed(final int event) {
			}
			public void run(final IToolRunnableControllerAdapter tools, final IProgressMonitor monitor) throws InterruptedException, CoreException {
				postCancelTask(options, monitor);
			}
		};
	}
	
	protected void postCancelTask(final int options, final IProgressMonitor monitor) throws CoreException {
		final String text = fCurrentPrompt.text + (
				((fCurrentPrompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) ?
						"(Input cancelled)" : "(Command cancelled)") + 
						fLineSeparator;
		fInfoStream.append(text,
				(fCurrentRunnable != null) ? fCurrentRunnable.getSubmitType() : SubmitType.TOOLS, fCurrentPrompt.meta);
	}
	
	public boolean supportsBusy() {
		return false;
	}
	
	public boolean isBusy() {
		return false;
	}
	
	
//-- Runnable Adapter
	@Override
	protected void initRunnableAdapter() {
		super.initRunnableAdapter();
		fDefaultPromptText = "> "; //$NON-NLS-1$
		fIncompletePromptText = "+ "; //$NON-NLS-1$
	}
	
	public Object getAdapter(final Class adapter) {
		if (ISetupRAdapter.class.equals(adapter)) {
			return this;
		}
		return null;
	}
	
	public void setIncompletePromptText(final String text) {
		fIncompletePromptText = text.intern();
	}
	
	protected final void setCurrentPrompt(final String text, final boolean addToHistory) {
		if (fDefaultPromptText.equals(text)) {
			if (addToHistory) {
				setCurrentPrompt(fDefaultPrompt);
			}
			else {
				setCurrentPrompt(new Prompt(text, IToolRunnableControllerAdapter.META_HISTORY_DONTADD | IToolRunnableControllerAdapter.META_PROMPT_DEFAULT));
			}
		}
		else if (fIncompletePromptText.equals(text)) {
			setCurrentPrompt(new IncompleteInputPrompt(
					fCurrentPrompt, fCurrentInput+fLineSeparator, fIncompletePromptText,
					addToHistory ? 0 : IToolRunnableControllerAdapter.META_HISTORY_DONTADD));
		}
		else {
			setCurrentPrompt(new Prompt(text, 
					addToHistory ? 0 : IToolRunnableControllerAdapter.META_HISTORY_DONTADD));
		}
	}
	
}
