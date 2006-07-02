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

package de.walware.statet.nico.core.runtime;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;

import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.core.runtime.ToolController.ToolStatus;

/**
 * 
 * Lifecycle: ToolProcess
 * 
 * @author Stephan Wahlbrink
 */
public class ToolWorkspace implements IToolStatusListener {

	
	public static final int DETAIL_PROMPT = 1;
	public static final int DETAIL_LINE_SEPARTOR = 2;
	
	private volatile String fLineSeparator;

	private Object fPromptMutex = new Object();
	private Prompt fCurrentPrompt;
	private Prompt fDefaultPrompt;
	private boolean fIsCurrentPromptPublished = false;
	private EnumSet<ToolStatus> fPublishPromptStatusSet = EnumSet.of(ToolStatus.STARTED_IDLE, ToolStatus.STARTED_PAUSED);

	
	public ToolWorkspace() {
		
		this(null, null);
	}
	
	public ToolWorkspace(Prompt prompt, String lineSeparator) {
		
		if (prompt == null) {
			prompt = Prompt.DEFAULT;
		}
		fCurrentPrompt = fDefaultPrompt = prompt;
		setLineSeparator(lineSeparator);
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
		
		return null;
	}
	
	public String getEncoding() {
		
		return "UTF-8"; //$NON-NLS-1$
	}

	
	public void controllerStatusChanged(ToolStatus oldStatus, ToolStatus newStatus, List<DebugEvent> eventCollection) {
		// by definition in tool lifecycle thread

		PUBLISH_PROMPT: if (fPublishPromptStatusSet.contains(newStatus)) {
			if (fPublishPromptStatusSet.contains(oldStatus)) {
				break PUBLISH_PROMPT;
			}
			Prompt prompt = fCurrentPrompt;
			if (prompt == null || prompt == fDefaultPrompt) {
				break PUBLISH_PROMPT;
			}
			synchronized (fPromptMutex) {
				fIsCurrentPromptPublished = true;
			}
			DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_PROMPT);
			event.setData(prompt);
			eventCollection.add(event);
		}
		else if (fIsCurrentPromptPublished) {
			synchronized (fPromptMutex) {
				fIsCurrentPromptPublished = false;
			}
			DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_PROMPT);
			event.setData(fDefaultPrompt);
			eventCollection.add(event);
		}
	}

	/**
	 * Use only in tool lifecycle thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	void setCurrentPrompt(Prompt prompt) {
		
		if (prompt == fCurrentPrompt || prompt == null) {
			return;
		}
		synchronized (fPromptMutex) {
			fCurrentPrompt = prompt;
		}
	}

	/**
	 * Use only in tool lifecycle thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	void setDefaultPrompt(Prompt prompt) {
		
		if (prompt == fDefaultPrompt || prompt == null) {
			return;
		}
		synchronized (fPromptMutex) {
			fDefaultPrompt = prompt;
		}
		DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_PROMPT);
		event.setData(prompt);
		fireEvent(event);
	}
	
	/**
	 * Use only in tool lifecycle thread.
	 * @param newSeparator the new separator, null sets the system default separator
	 */
	void setLineSeparator(String newSeparator) {
		
		String oldSeparator = fLineSeparator;
		fLineSeparator = (newSeparator != null) ? newSeparator : System.getProperty("line.separator"); //$NON-NLS-1$
		if (!fLineSeparator.equals(oldSeparator)) {
			DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_LINE_SEPARTOR);
			event.setData(fLineSeparator);
			fireEvent(event);
		}
	}
		
	
	protected void fireEvent(DebugEvent event) {
		
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}
}
