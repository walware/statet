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

package de.walware.statet.nico.internal.core;

import static de.walware.statet.nico.core.runtime.IToolRunnable.TOTAL_WORK;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.core.runtime.IProgressInfo;
import de.walware.statet.nico.core.runtime.IToolRunnable;


/**
 * Progress monitor for the ToolController.
 */
public class RunnableProgressMonitor implements IProgressMonitorWithBlocking, IProgressInfo {
	
	
	private String fMainName;
	private String fSubTaskName = ""; //$NON-NLS-1$
	private IToolRunnable fRunnable = null;
	private boolean fRefreshLabel = true;
	private String fLabel;
		
	private volatile int fCurrentWorked = 0;
	private volatile String fBlockedMessage = null;
	private volatile boolean isCanceled = false;
		
	
	public RunnableProgressMonitor(String name) {
		
		fMainName = (name != null) ? name : ""; //$NON-NLS-1$
	}
	
	public RunnableProgressMonitor(IToolRunnable runnable) {
		
		fRunnable = runnable;
		fMainName = runnable.getLabel();
	}
	
	
/* IProgressMonitor / IProgressMonitorWithBlocking */
	
	public void beginTask(String taskName, int totalWork) {
	}

	public void setTaskName(String taskName) {
	}

	public void subTask(String name) {

		fSubTaskName = name;
	}

	public void done() {

		fCurrentWorked = TOTAL_WORK;
	}

	public void worked(int work) {
		
		int newValue = fCurrentWorked + work;
		if (newValue > TOTAL_WORK) {
			newValue = TOTAL_WORK;
		}
		fCurrentWorked = newValue;
	}

	public void internalWorked(double work) {
	}

	public void setCanceled(boolean value) {

		isCanceled = true;
		fRefreshLabel = true;
	}

	public boolean isCanceled() {

		return isCanceled;
	}

	
	public void setBlocked(IStatus reason) {

		fBlockedMessage = (reason != null) ? reason.getMessage() : ""; //$NON-NLS-1$
		fRefreshLabel = true;
	}

	public void clearBlocked() {

		fBlockedMessage = null;
		fRefreshLabel = true;
	}

	
/* IProgressInfo / Getter access */
	
	public int getWorked() {
		
		return fCurrentWorked;
	}
	
	public String getLabel() {
		
		if (fRefreshLabel) {
			fRefreshLabel = false;
			fLabel = getDisplayStringWithStatus();
		}
		return fLabel;
	}
	
	public String getSubLabel() {
		
		return fSubTaskName;
	}
	
	private String getDisplayStringWithStatus() {

		if (isCanceled) {
			return NLS.bind(Messages.Progress_Canceled_label, fMainName);
		}
		String blocked = fBlockedMessage;
		if (blocked != null) {
			return NLS.bind(Messages.Progress_Blocked_label, (new Object[] {
					fMainName, blocked }));
		}
		return fMainName;
	}
	
	public IToolRunnable getRunnable() {
		
		return fRunnable;
	}
}
