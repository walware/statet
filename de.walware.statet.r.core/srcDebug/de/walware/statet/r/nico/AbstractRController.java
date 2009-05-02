/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.internal.nico.RNicoMessages;


/**
 * Abstract superclass of controllers for R.
 * 
 * All implementations of {@link ToolController} for R should extends this class.
 */
public abstract class AbstractRController
		extends ToolController<RWorkspace>
		implements IRBasicAdapter, ISetupRAdapter {
	
	
	public static class RCommandRunnable extends ConsoleCommandRunnable {
		
		protected RCommandRunnable(final String text, final SubmitType type) {
			super(text, type);
		}
		
		@Override
		public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws InterruptedException, CoreException {
			super.run(adapter, monitor);
			((IRBasicAdapter) adapter).briefAboutChange(RWorkspace.REFRESH_AUTO);
		}
		
	}
	
	
	protected String fIncompletePromptText;
	protected String fDefaultPromptText;
	
	int fChanged;
	
	
	public AbstractRController(final ToolProcess process, final Map<String, Object> initData) {
		super(process, initData);
		process.registerFeatureSet(RTool.R_BASIC_FEATURESET_ID);
		fChanged = RWorkspace.REFRESH_COMPLETE;
	}
	
	
	@Override
	protected final IToolRunnable createQuitRunnable() {
		return new IToolRunnable() {
			
			public String getTypeId() {
				return ToolController.QUIT_TYPE_ID;
			}
			
			public String getLabel() {
				return RNicoMessages.Quit_Task_label;
			}
			
			public SubmitType getSubmitType() {
				return SubmitType.TOOLS;
			}
			
			public void changed(final int event, final ToolProcess process) {
			}
			
			public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws InterruptedException, CoreException {
				((IRBasicAdapter) adapter).quit(monitor);
			}
		};
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
			public void changed(final int event, final ToolProcess process) {
			}
			public void run(final IToolRunnableControllerAdapter tools, final IProgressMonitor monitor) throws InterruptedException, CoreException {
				if (!isTerminated()) {
					postCancelTask(options, monitor);
				}
			}
		};
	}
	
	protected void postCancelTask(final int options, final IProgressMonitor monitor) throws CoreException {
		final String text = fCurrentPrompt.text + (
				((fCurrentPrompt.meta & RTool.META_PROMPT_INCOMPLETE_INPUT) != 0) ?
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
	
	public void setRObjectDB(final boolean enable) {
		fWorkspaceData.enableRObjectDB(enable);
	}
	
	public Object getAdapter(final Class adapter) {
		if (ISetupRAdapter.class.equals(adapter)) {
			return this;
		}
		return null;
	}
	
	@Override
	public IToolRunnable createCommandRunnable(final String command, final SubmitType type) {
		return new RCommandRunnable(command, type);
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
				setCurrentPrompt(new Prompt(fDefaultPromptText,
						IToolRunnableControllerAdapter.META_HISTORY_DONTADD | IToolRunnableControllerAdapter.META_PROMPT_DEFAULT));
			}
		}
		else if (fIncompletePromptText.equals(text)) {
			setCurrentPrompt(new IncompleteInputPrompt(
					fCurrentPrompt, fCurrentInput+fLineSeparator, fIncompletePromptText,
					addToHistory ? 0 : IToolRunnableControllerAdapter.META_HISTORY_DONTADD));
		}
		else if (text != null) {
			setCurrentPrompt(new Prompt(text,
					addToHistory ? 0 : IToolRunnableControllerAdapter.META_HISTORY_DONTADD));
		}
		else { // TODO log warning / exception?
			setCurrentPrompt(new Prompt("", //$NON-NLS-1$
					addToHistory ? 0 : IToolRunnableControllerAdapter.META_HISTORY_DONTADD));
		}
	}
	
	public void briefAboutChange(final int o) {
		fChanged |= o;
	}
	
	public void quit(final IProgressMonitor monitor) throws CoreException {
		final String command = "q()"; //$NON-NLS-1$
		submitToConsole(command, monitor);
	}
	
}
