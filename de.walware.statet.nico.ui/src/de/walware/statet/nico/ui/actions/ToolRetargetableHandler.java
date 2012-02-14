/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.IToolRetargetable;


/**
 * Abstract command handler (not tool event handler)
 * supporting tool assignment by the {@link IToolRetargetable} interface
 */
public abstract class ToolRetargetableHandler extends AbstractHandler implements IToolRetargetable {
	
	
	protected static final int S_INIT = 1;
	protected static final int S_ONAIR = 2;
	protected static final int S_DISPOSED = 3;
	
	protected static class ChangedStateException extends IllegalStateException {
		private static final long serialVersionUID = 1L;
	}
	
	
	protected class ElementUpdater implements Runnable {
		
		private final String fCommandId;
		
		public ElementUpdater(final String commandId) {
			fCommandId = commandId;
			assert (getServiceLocator() != null);
		}
		
		@Override
		public void run() {
			final ICommandService commandService = (ICommandService) getServiceLocator().getService(ICommandService.class);
			if (commandService != null) {
				commandService.refreshElements(fCommandId, null);
			}
		}
		
		public void schedule() {
			final Display display = UIAccess.getDisplay(getShell());
			if (display != null) {
				if (display.getThread() == Thread.currentThread()) {
					run();
				}
				else {
					display.asyncExec(this);
				}
			}
		}
	}
	
	
	/** providing the active tool */
	private final IToolProvider fToolProvider;
	
	/** optional service locator */
	private final IServiceLocator fServiceLocator;
	
	/** internal state of this handler */
	private int fState;
	
	private ToolProcess fTool;
	
	
	public ToolRetargetableHandler(final IToolProvider toolProvider, final IServiceLocator serviceLocator) {
		super();
		fState = S_INIT;
		fServiceLocator = serviceLocator;
		fToolProvider = toolProvider;
		if (fToolProvider != null) {
			fToolProvider.addToolRetargetable(this);
		}
	}
	
	/**
	 * Must be call at the end of the constructor to finish initialization.
	 */
	protected void init() {
		setTool(fToolProvider.getTool());
		fState = S_ONAIR;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (fState >= S_DISPOSED) {
			return;
		}
		fState = S_DISPOSED;
		
		if (fToolProvider != null) {
			fToolProvider.removeToolRetargetable(this);
		}
		
		synchronized (this) {
			fTool = null;
		}
		
		super.dispose();
	}
	
	
	@Override
	public final void setTool(final ToolProcess tool) {
		boolean update = false;
		
		synchronized (this) {
			if (fState == S_DISPOSED) {
				return;
			}
			if ((fTool == null) ? 
					(tool == null && getState() == S_ONAIR) :
					(tool == fTool) ) {
				return;
			}
			
			fTool = tool;
			update = handleToolChanged();
		}
		
		if (update) {
			doRefresh();
		}
	}
	
	@Override
	public void toolTerminated() {
		boolean update = false;
		synchronized (this) {
			update = handleToolChanged();
		}
		
		if (update) {
			doRefresh();
		}
	}
	
	
	protected final int getState() {
		return fState;
	}
	
	/**
	 * @return current associated tool
	 */
	public final ToolProcess getTool() {
		return fTool;
	}
	
	/**
	 * Checks if action is enabled for the current tool and returns it
	 * 
	 * @return current associated tool
	 */
	protected final ToolProcess getCheckedTool() {
		synchronized (this) {
			if (fState != S_ONAIR || !isEnabled()) {
				throw new ChangedStateException();
			}
			return fTool;
		}
	}
	
	/**
	 * Optional service locator (window, view, page,...)
	 * 
	 * @return the service locator or <code>null</code>
	 */
	protected IServiceLocator getServiceLocator() {
		return fServiceLocator;
	}
	
	protected IProgressService getProgressService() {
		return (IProgressService) getServiceLocator().getService(IProgressService.class);
	}
	
	
	protected Shell getShell() {
		if (fServiceLocator instanceof IShellProvider) {
			return ((IShellProvider) fServiceLocator).getShell();
		}
		return null; 
	}
	
	
	/**
	 * Is called when the tool changed. Can be overwritten to
	 * update the handler state.
	 * 
	 * @return if {@link #doRefresh()} should be called
	 */
	public boolean handleToolChanged() {
		final boolean wasEnabled = isEnabled();
		final boolean isEnabled = evaluateEnabled();
		if (wasEnabled != isEnabled) {
			setBaseEnabled(isEnabled);
			return true;
		}
		return false;
	}
	
	/**
	 * Is called when the tool was terminated. Can be overwritten to
	 * update the handler state or do nothing.
	 * 
	 * @return if {@link #doRefresh()} should be called
	 */
	public boolean handleToolTerminated() {
		return handleToolChanged();
	}
	
	/**
	 * Computes the enablement state of the handler. Can be overwritten
	 * to change the criteria.
	 * 
	 * @return if handler should set to enabled
	 */
	protected boolean evaluateEnabled() {
		final ToolProcess tool = getTool();
		return (tool != null
				&& !tool.isTerminated());
	}
	
	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		if (fToolProvider != null) {
			final ToolProcess tool = fToolProvider.getTool();
			if (tool != fTool) {
				setTool(tool);
			}
		}
		
		try {
			doExecute(event);
		}
		catch (final ChangedStateException e) {
		}
		return null;
	}
	
	protected void doRefresh() {
	}
	
	protected abstract Object doExecute(final ExecutionEvent event);
	
}
