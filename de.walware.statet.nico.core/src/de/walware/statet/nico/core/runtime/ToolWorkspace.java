/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.net.resourcemapping.ResourceMappingUtils;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.variables.core.DateVariable;
import de.walware.ecommons.variables.core.DynamicVariable;
import de.walware.ecommons.variables.core.TimeVariable;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.NicoVariables;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.internal.core.NicoPlugin;


/**
 * It belongs to a ToolProcess and has the same life cycle.
 */
public class ToolWorkspace {
	
	
	public static interface Listener {
		
		public void propertyChanged(ToolWorkspace workspace, Map<String, Object> properties);
		
	}
	
	
	private class ControllerListener implements IToolStatusListener {
		
		public void controllerStatusRequested(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
		}
		
		public void controllerStatusRequestCanceled(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
		}
		
		public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
			if (newStatus == ToolStatus.TERMINATED) {
				dispose();
			}
			
			// by definition in tool lifecycle thread
			if (!newStatus.isRunning()) {
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
	
	private class AutoUpdater implements ISystemRunnable {
		
		
		public String getTypeId() {
			return "common/workspace/update.auto";
		}
		
		public String getLabel() {
			return "Auto Update";
		}
		
		public boolean isRunnableIn(final ITool tool) {
			return (tool == fProcess);
		}
		
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
			case MOVING_FROM:
				return false;
			}
			return true;
		}
		
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			fIsRefreshing = true;
			try {
				autoRefreshFromTool((IConsoleService) service, monitor);
			}
			finally {
				fIsRefreshing = false;
			}
			firePropertiesChanged();
		}
		
	}
	
	
	public static final int DETAIL_PROMPT = 1;
	public static final int DETAIL_LINE_SEPARTOR = 2;
	
	protected final ToolProcess fProcess;
	
	private volatile String fLineSeparator;
	private volatile String fFileSeparator;
	
	private volatile Prompt fCurrentPrompt;
	private volatile Prompt fDefaultPrompt;
	private Prompt fPublishedPrompt;
	
	private IFileStore fWorkspaceDir;
	
	private final String fRemoteHost;
	private IPath fRemoteWorkspaceDir;
	
	private final Map<String, Object> fProperties = new HashMap<String, Object>();
	private final FastList<Listener> fPropertyListener = new FastList<Listener>(Listener.class);
	
	private boolean fAutoRefreshEnabled = true;
	
	private boolean fIsRefreshing;
	
	private final FastList<IDynamicVariable> fStringVariables = new FastList<IDynamicVariable>(IDynamicVariable.class);
	
	
	public ToolWorkspace(final ToolController controller,
			Prompt prompt, final String lineSeparator,
			final String remoteHost) {
		fProcess = controller.getTool();
		if (prompt == null) {
			prompt = Prompt.DEFAULT;
		}
		fPublishedPrompt = fCurrentPrompt = fDefaultPrompt = prompt;
		fRemoteHost = remoteHost;
		controlSetLineSeparator(lineSeparator);
		controlSetFileSeparator(null);
		
		controller.addToolStatusListener(new ControllerListener());
		controller.getQueue().addOnIdle(new AutoUpdater(), 5000);
		
		fStringVariables.add(new DateVariable(NicoVariables.SESSION_STARTUP_DATE_VARIABLE) {
			@Override
			protected long getTimestamp() {
				return fProcess.getStartupTimestamp();
			}
		});
		fStringVariables.add(new TimeVariable(NicoVariables.SESSION_STARTUP_TIME_VARIABLE) {
			@Override
			protected long getTimestamp() {
				return fProcess.getStartupTimestamp();
			}
		});
		fStringVariables.add(new DateVariable(NicoVariables.SESSION_CONNECTION_DATE_VARIABLE) {
			@Override
			protected long getTimestamp() {
				return fProcess.getConnectionTimestamp();
			}
		});
		fStringVariables.add(new TimeVariable(NicoVariables.SESSION_CONNECTION_TIME_VARIABLE) {
			@Override
			protected long getTimestamp() {
				return fProcess.getStartupTimestamp();
			}
		});
		fStringVariables.add(new DynamicVariable.LocationVariable(NicoVariables.SESSION_STARTUP_WD_VARIABLE) {
			public String getValue(final String argument) throws CoreException {
				return fProcess.getStartupWD();
			}
		});
	}
	
	
	public final ToolProcess getProcess() {
		return fProcess;
	}
	
	
	public void setAutoRefresh(final boolean enable) {
		synchronized (fProcess.getQueue()) {
			if (fAutoRefreshEnabled != enable) {
				fAutoRefreshEnabled = enable;
				final ToolStatus status = fProcess.getToolStatus();
				if (status != ToolStatus.TERMINATED) {
					if (enable && status.isWaiting()) {
						fProcess.getQueue().internalResetIdle();
						fProcess.getQueue().notifyAll();
					}
					addPropertyChanged("AutoRefresh.enabled", enable);
					firePropertiesChanged();
				}
			}
		}
	}
	
	public boolean isAutoRefreshEnabled() {
		return fAutoRefreshEnabled;
	}
	
	
	protected void autoRefreshFromTool(final IConsoleService s, final IProgressMonitor monitor) throws CoreException {
		if (fAutoRefreshEnabled) {
			refreshFromTool(0, s, monitor);
		}
	}
	
	protected void refreshFromTool(final int options, final IConsoleService s, final IProgressMonitor monitor) throws CoreException {
	}
	
	public final String getLineSeparator() {
		return fLineSeparator;
	}
	
	public final String getFileSeparator() {
		return fFileSeparator;
	}
	
	
	public final Prompt getPrompt() {
		return fPublishedPrompt;
	}
	
	protected final Prompt getCurrentPrompt() {
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
	
	
	public final boolean isRemote() {
		return (fRemoteHost != null);
	}
	
	public String getRemoteAddress() {
		return fRemoteHost;
	}
	
	public IPath getRemoteWorkspaceDirPath() {
		return fRemoteWorkspaceDir;
	}
	
	public IFileStore toFileStore(final IPath toolPath) throws CoreException {
		if (fRemoteHost != null) {
			return ResourceMappingUtils.getManager()
					.mapRemoteResourceToFileStore(fRemoteHost, toolPath, fRemoteWorkspaceDir);
		}
		return FileUtil.getFileStore(toolPath.toString(), fWorkspaceDir);
	}
	
	public IFileStore toFileStore(final String toolPath) throws CoreException {
		if (fRemoteHost != null) {
			return ResourceMappingUtils.getManager()
					.mapRemoteResourceToFileStore(fRemoteHost, new Path(toolPath), fRemoteWorkspaceDir);
		}
		return FileUtil.getFileStore(toolPath, fWorkspaceDir);
	}
	
	public String toToolPath(final IFileStore fileStore) throws CoreException {
		if (fRemoteHost != null) {
			final IPath path = ResourceMappingUtils.getManager()
					.mapFileStoreToRemoteResource(fRemoteHost, fileStore);
			if (path != null) {
				return path.toString();
			}
			throw new CoreException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, "Resolving path for the remote system failed."));
		}
		return URIUtil.toPath(fileStore.toURI()).toString();
	}
	
	
	final void controlRefresh(final int options, final IConsoleService adapter, final IProgressMonitor monitor) throws CoreException {
		fIsRefreshing = true;
		try {
			refreshFromTool(options, adapter, monitor);
		}
		finally {
			fIsRefreshing = false;
		}
		firePropertiesChanged();
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
		if (!status.isRunning()) {
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
	 * 
	 * The default separator is System.getProperty("line.separator") for local
	 * workspaces, and '\n' for remote workspaces.
	 * 
	 * @param newSeparator the new line separator, null sets the default separator
	 */
	final void controlSetLineSeparator(final String newSeparator) {
		final String oldSeparator = fLineSeparator;
		if (newSeparator != null) {
			fLineSeparator = newSeparator;
		}
		else {
			fLineSeparator = (fRemoteHost == null) ? System.getProperty("line.separator") : "\n"; //$NON-NLS-1$
		}
//		if (!fLineSeparator.equals(oldSeparator)) {
//			DebugEvent event = new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_LINE_SEPARTOR);
//			event.setData(fLineSeparator);
//			fireEvent(event);
//		}
	}
	
	/**
	 * Use only in tool main thread.
	 * 
	 * The default separator is System.getProperty("file.separator") for local
	 * workspaces, and '/' for remote workspaces.
	 * 
	 * @param newSeparator the new file separator, null sets the default separator
	 */
	final void controlSetFileSeparator(final String newSeparator) {
		final String oldSeparator = fFileSeparator;
		if (newSeparator != null) {
			fFileSeparator = newSeparator;
		}
		else {
			fFileSeparator = (fRemoteHost == null) ? System.getProperty("file.separator") : "/"; //$NON-NLS-1$
		}
	}
	
	protected final void controlSetWorkspaceDir(final IFileStore directory) {
		if ((fWorkspaceDir != null) ? !fWorkspaceDir.equals(directory) : directory != null) {
			fWorkspaceDir = directory;
			fProperties.put("wd", directory);
			if (!fIsRefreshing) {
				firePropertiesChanged();
			}
		}
	}
	
	protected final void controlSetRemoteWorkspaceDir(final IPath path) {
		fRemoteWorkspaceDir = path;
		try {
			controlSetWorkspaceDir(toFileStore(path));
		}
		catch (final CoreException e) {
			controlSetWorkspaceDir(null);
		}
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
	
	public final void addPropertyListener(final Listener listener) {
		fPropertyListener.add(listener);
	}
	
	public final void removePropertyListener(final Listener listener) {
		fPropertyListener.remove(listener);
	}
	
	protected final void addPropertyChanged(final String property, final Object attr) {
		fProperties.put(property, attr);
	}
	
	protected final void firePropertiesChanged() {
		if (fProperties.isEmpty()) {
			return;
		}
		final Listener[] listeners = fPropertyListener.toArray();
		for (final Listener listener : listeners) {
			try {
				listener.propertyChanged(ToolWorkspace.this, fProperties);
			}
			catch (final Exception e) {
				NicoPlugin.logError(ICommonStatusConstants.INTERNAL_PLUGGED_IN, "An unexpected exception was thrown when notifying a tool workspace listener about changes.", e);
			}
		}
		fProperties.clear();
	}
	
	public List<IDynamicVariable> getStringVariables() {
		return new ConstList<IDynamicVariable>(fStringVariables.toArray());
	}
	
	protected void dispose() {
	}
	
}
