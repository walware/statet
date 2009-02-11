/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;

import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;


/**
 * Life cycle: ToolProcess
 */
public class ToolWorkspace {
	
	protected class ControllerListener implements IToolStatusListener {
		
		public void controllerStatusRequested(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
		}
		
		public void controllerStatusRequestCanceled(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
		}
		
		public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
			// by definition in tool lifecycle thread
			if (isPublishPromptStatus(newStatus)) {
				if (fCurrentPrompt == null || fCurrentPrompt == fPublishedPrompt) {
					return;
				}
				fPublishedPrompt = fCurrentPrompt;
				firePrompt(fCurrentPrompt, eventCollection);
				return;
			}
			else {
				fPublishedPrompt = fDefaultPrompt;
				firePrompt(fDefaultPrompt, eventCollection);
			}
		}
		
	}
	
	
	public static final int DETAIL_PROMPT = 1;
	public static final int DETAIL_LINE_SEPARTOR = 2;
	
	private volatile String fLineSeparator;
	
	private volatile Prompt fCurrentPrompt;
	private volatile Prompt fDefaultPrompt;
	private Prompt fPublishedPrompt;
	
	private IFileStore fWorkspaceDir;
	
	
	public ToolWorkspace(final ToolController controller) {
		this(controller, null, null);
	}
	
	public ToolWorkspace(final ToolController controller, Prompt prompt, final String lineSeparator) {
		if (prompt == null) {
			prompt = Prompt.DEFAULT;
		}
		fCurrentPrompt = fDefaultPrompt = prompt;
		controlSetLineSeparator(lineSeparator);
		
		controller.addToolStatusListener(createToolStatusListener());
	}
	
	protected IToolStatusListener createToolStatusListener() {
		return new ControllerListener();
	}
	
	
	protected void refreshFromTool(final IProgressMonitor monitor) throws CoreException {
	}
	
	public final String getLineSeparator() {
		return fLineSeparator;
	}
	
	
	public Prompt getPrompt() {
		return fCurrentPrompt;
	}
	
	public final Prompt getDefaultPrompt() {
		return fDefaultPrompt;
	}
	
	public final IFileStore getWorkspaceDir() {
		return fWorkspaceDir;
	}
	
	public String getEncoding() {
		return "UTF-8"; //$NON-NLS-1$
	}
	
	
	final void controlRefresh(final IProgressMonitor monitor) throws CoreException {
		refreshFromTool(monitor);
	}
	
	/**
	 * Use only in tool main thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	final void controlSetCurrentPrompt(final Prompt prompt, final ToolStatus status) {
		if (prompt == fCurrentPrompt || prompt == null) {
			return;
		}
		fCurrentPrompt = prompt;
		if (isPublishPromptStatus(status)) {
			fPublishedPrompt = prompt;
			firePrompt(prompt, null);
		}
	}
	
	/**
	 * Use only in tool main thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	final void controlSetDefaultPrompt(final Prompt prompt) {
		if (prompt == fDefaultPrompt || prompt == null) {
			return;
		}
		final Prompt oldDefault = fDefaultPrompt;
		fDefaultPrompt = prompt;
		if (oldDefault == fCurrentPrompt) {
			fCurrentPrompt = prompt;
		}
		if (oldDefault == fPublishedPrompt) {
			fPublishedPrompt = prompt;
			firePrompt(prompt, null);
		}
	}
	
	/**
	 * Use only in tool main thread.
	 * @param newSeparator the new separator, null sets the system default separator
	 */
	final void controlSetLineSeparator(final String newSeparator) {
		final String oldSeparator = fLineSeparator;
		fLineSeparator = (newSeparator != null) ? newSeparator : System.getProperty("line.separator"); 
//		if (!fLineSeparator.equals(oldSeparator)) {
//			DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_LINE_SEPARTOR);
//			event.setData(fLineSeparator);
//			fireEvent(event);
//		}
	}
	
	final void controlSetWorkspaceDir(final IFileStore directory) {
		fWorkspaceDir = directory;
	}
	
	
	private final boolean isPublishPromptStatus(final ToolStatus status) {
		return (status == ToolStatus.STARTED_IDLING || status == ToolStatus.STARTED_PAUSED);
	}
	
	private final void firePrompt(final Prompt prompt, final List<DebugEvent> eventCollection) {
		final DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_PROMPT);
		event.setData(prompt);
		if (eventCollection != null) {
			eventCollection.add(event);
			return;
		}
		else {
			fireEvent(event);
		}
	}
	
	protected final void fireEvent(final DebugEvent event) {
		final DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}
	
}
