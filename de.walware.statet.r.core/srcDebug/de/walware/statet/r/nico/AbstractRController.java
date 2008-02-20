/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.r.internal.core.RCorePlugin;


public abstract class AbstractRController
		extends ToolController<RWorkspace>
		implements IBasicRAdapter, ISetupRAdapter {
	
	
	protected String fIncompletePromptText;
	protected String fDefaultPromptText;
	
	
	public AbstractRController(ToolProcess process) {
		super(process);
		process.registerFeatureSet(BasicR.FEATURESET_ID);
	}
	
	@Override
	protected void postCancelTask(int options) {
		if ((getStatus() == ToolStatus.STARTED_IDLING || getStatus() == ToolStatus.STARTED_PAUSED)
				&& (fWorkspaceData.getPrompt().meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
			cancelIncompletePrompt();
		}
	}
	
//-- Runnable Adapter
	@Override
	protected void initRunnableAdapter() {
		super.initRunnableAdapter();
		fDefaultPromptText = "> ".intern(); //$NON-NLS-1$
		fIncompletePromptText = "+ ".intern(); //$NON-NLS-1$
	}
	
	public Object getAdapter(Class adapter) {
		if (ISetupRAdapter.class.equals(adapter)) {
			return this;
		}
		return null;
	}
	
	public void setIncompletePromptText(String text) {
		fIncompletePromptText = text.intern();
	}
	
	protected final Prompt createIncompleteInputPrompt() {
		return new IncompleteInputPrompt(fCurrentPrompt, fCurrentInput+fLineSeparator, fIncompletePromptText);
	}
	
	protected final void setCurrentPrompt(String text) {
		if (fDefaultPromptText.equals(text)) {
			setCurrentPrompt(Prompt.DEFAULT);
		}
		else if (fIncompletePromptText.equals(text)) {
			setCurrentPrompt(createIncompleteInputPrompt());
		}
		else {
			setCurrentPrompt(new Prompt(text));
		}
	}
	
	protected void cancelIncompletePrompt() {
		fInfoStream.append(fCurrentPrompt.text+"(Input cancelled)"+fLineSeparator,
				(fCurrentRunnable != null) ? fCurrentRunnable.getSubmitType() : SubmitType.TOOLS, fCurrentPrompt.meta);
		setCurrentPrompt(fDefaultPrompt);
	}
	
}
