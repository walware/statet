/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
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
			PUBLISH_PROMPT: if (fPublishPromptStatusSet.contains(newStatus)) {
				if (fPublishPromptStatusSet.contains(oldStatus)) {
					break PUBLISH_PROMPT;
				}
				final Prompt prompt = fCurrentPrompt;
				if (prompt == null || prompt == fDefaultPrompt) {
					break PUBLISH_PROMPT;
				}
				synchronized (fPromptMutex) {
					fIsCurrentPromptPublished = true;
				}
				firePrompt(prompt);
			}
			else if (fIsCurrentPromptPublished) {
				synchronized (fPromptMutex) {
					fIsCurrentPromptPublished = false;
				}
				firePrompt(fDefaultPrompt);
			}
		}
	}
	
	
	public static final int DETAIL_PROMPT = 1;
	public static final int DETAIL_LINE_SEPARTOR = 2;
	
	private volatile String fLineSeparator;
	
	private Object fPromptMutex = new Object();
	private Prompt fCurrentPrompt;
	private Prompt fDefaultPrompt;
	private boolean fIsCurrentPromptPublished = false;
	private EnumSet<ToolStatus> fPublishPromptStatusSet = EnumSet.of(ToolStatus.STARTED_IDLING, ToolStatus.STARTED_PAUSED);
	
	private IFileStore fWorkspaceDir;
	
	
	public ToolWorkspace(final ToolController controller) {
		this(controller, null, null);
	}
	
	public ToolWorkspace(final ToolController controller, Prompt prompt, final String lineSeparator) {
		if (prompt == null) {
			prompt = Prompt.DEFAULT;
		}
		fCurrentPrompt = fDefaultPrompt = prompt;
		setLineSeparator(lineSeparator);
		
		controller.addToolStatusListener(createToolStatusListener());
	}
	
	protected IToolStatusListener createToolStatusListener() {
		return new ControllerListener();
	}
	
	
	public String getLineSeparator() {
		return fLineSeparator;
	}
	
	
	public Prompt getPrompt() {
		synchronized (fPromptMutex) {
			if (fIsCurrentPromptPublished) {
				return fCurrentPrompt;
			} else {
				return fDefaultPrompt;
			}
		}
	}
	
	public Prompt getDefaultPrompt() {
		return fDefaultPrompt;
	}
	
	public IFileStore getWorkspaceDir() {
		return fWorkspaceDir;
	}
	
	public String getEncoding() {
		return "UTF-8"; //$NON-NLS-1$
	}
	
	/**
	 * Use only in tool lifecycle thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	void setCurrentPrompt(final Prompt prompt, final ToolStatus status) {
		if (prompt == fCurrentPrompt || prompt == null) {
			return;
		}
		boolean firePrompt = false;
		synchronized (fPromptMutex) {
			fCurrentPrompt = prompt;
			if (fPublishPromptStatusSet.contains(status)
					&& (prompt != fDefaultPrompt || fIsCurrentPromptPublished) ) {
				firePrompt = true;
				fIsCurrentPromptPublished = (prompt != fDefaultPrompt);
			}
		}
		if (firePrompt) {
			firePrompt(prompt);
		}
	}
	
	/**
	 * Use only in tool lifecycle thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	void setDefaultPrompt(final Prompt prompt) {
		if (prompt == fDefaultPrompt || prompt == null) {
			return;
		}
		synchronized (fPromptMutex) {
			fDefaultPrompt = prompt;
		}
		if (!fIsCurrentPromptPublished) {
			firePrompt(fDefaultPrompt);
		}
	}
	
	/**
	 * Use only in tool lifecycle thread.
	 * @param newSeparator the new separator, null sets the system default separator
	 */
	void setLineSeparator(final String newSeparator) {
		final String oldSeparator = fLineSeparator;
		fLineSeparator = (newSeparator != null) ? newSeparator : System.getProperty("line.separator"); //$NON-NLS-1$
//		if (!fLineSeparator.equals(oldSeparator)) {
//			DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_LINE_SEPARTOR);
//			event.setData(fLineSeparator);
//			fireEvent(event);
//		}
	}
	
	void setWorkspaceDir(final IFileStore directory) {
		fWorkspaceDir = directory;
	}
	
	
	private void firePrompt(final Prompt prompt) {
		final DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_PROMPT);
		event.setData(prompt);
		fireEvent(event);
	}
	
	protected void fireEvent(final DebugEvent event) {
		final DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}
	
}
