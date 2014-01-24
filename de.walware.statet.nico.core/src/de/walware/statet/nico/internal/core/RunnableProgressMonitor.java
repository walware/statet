/*=============================================================================#
 # Copyright (c) 2006-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.core;

import static de.walware.ecommons.ts.IToolRunnable.TOTAL_WORK;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ts.IToolRunnable;

import de.walware.statet.nico.core.runtime.IProgressInfo;


/**
 * Progress monitor for the ToolController.
 */
public class RunnableProgressMonitor implements IProgressMonitorWithBlocking, IProgressInfo {
	
	
	private final String fMainName;
	private String fSubTaskName = ""; //$NON-NLS-1$
	private IToolRunnable fRunnable = null;
	private boolean fRefreshLabel = true;
	private String fLabel;
	
	private volatile int fCurrentWorked = 0;
	private volatile String fBlockedMessage = null;
	private volatile boolean isCanceled = false;
	
	
	public RunnableProgressMonitor(final String name) {
		fMainName = (name != null) ? name : ""; //$NON-NLS-1$
	}
	
	public RunnableProgressMonitor(final IToolRunnable runnable) {
		fRunnable = runnable;
		fMainName = runnable.getLabel();
	}
	
	
/* IProgressMonitor / IProgressMonitorWithBlocking */
	
	@Override
	public void beginTask(final String taskName, final int totalWork) {
	}
	
	@Override
	public void setTaskName(final String taskName) {
	}
	
	@Override
	public void subTask(final String name) {
		fSubTaskName = name;
	}
	
	@Override
	public void done() {
		fCurrentWorked = TOTAL_WORK;
	}
	
	@Override
	public void worked(final int work) {
		
		int newValue = fCurrentWorked + work;
		if (newValue > TOTAL_WORK) {
			newValue = TOTAL_WORK;
		}
		fCurrentWorked = newValue;
	}
	
	@Override
	public void internalWorked(final double work) {
	}
	
	@Override
	public void setCanceled(final boolean value) {
		isCanceled = true;
		fRefreshLabel = true;
	}
	
	@Override
	public boolean isCanceled() {
		return isCanceled;
	}
	
	
	@Override
	public void setBlocked(final IStatus reason) {
		fBlockedMessage = (reason != null) ? reason.getMessage() : ""; //$NON-NLS-1$
		fRefreshLabel = true;
	}
	
	@Override
	public void clearBlocked() {
		fBlockedMessage = null;
		fRefreshLabel = true;
	}
	
	
/* IProgressInfo / Getter access */
	
	@Override
	public int getWorked() {
		return fCurrentWorked;
	}
	
	@Override
	public String getLabel() {
		if (fRefreshLabel) {
			fRefreshLabel = false;
			fLabel = getDisplayStringWithStatus();
		}
		return fLabel;
	}
	
	@Override
	public String getSubLabel() {
		return fSubTaskName;
	}
	
	private String getDisplayStringWithStatus() {
		if (isCanceled) {
			return NLS.bind(Messages.Progress_Canceled_label, fMainName);
		}
		final String blocked = fBlockedMessage;
		if (blocked != null) {
			return NLS.bind(Messages.Progress_Blocked_label, (new Object[] {
					fMainName, blocked }));
		}
		return fMainName;
	}
	
	@Override
	public IToolRunnable getRunnable() {
		return fRunnable;
	}
	
}
