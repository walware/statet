/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import java.io.File;
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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.net.resourcemapping.ResourceMappingUtils;
import de.walware.ecommons.runtime.core.util.PathUtils;
import de.walware.ecommons.ts.ISystemReadRunnable;
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
		
		@Override
		public void controllerStatusRequested(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
		}
		
		@Override
		public void controllerStatusRequestCanceled(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
		}
		
		@Override
		public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
			if (newStatus == ToolStatus.TERMINATED) {
				dispose();
			}
			
			// by definition in tool lifecycle thread
			if (!newStatus.isRunning()) {
				if (ToolWorkspace.this.currentPrompt == null || ToolWorkspace.this.currentPrompt == ToolWorkspace.this.publishedPrompt) {
					return;
				}
				ToolWorkspace.this.publishedPrompt= ToolWorkspace.this.currentPrompt;
				firePrompt(ToolWorkspace.this.currentPrompt, eventCollection);
				return;
			}
			else {
				ToolWorkspace.this.publishedPrompt= ToolWorkspace.this.defaultPrompt;
				firePrompt(ToolWorkspace.this.defaultPrompt, eventCollection);
			}
		}
		
	}
	
	private class AutoUpdater implements ISystemReadRunnable {
		
		
		@Override
		public String getTypeId() {
			return "common/workspace/update.auto";
		}
		
		@Override
		public String getLabel() {
			return "Auto Update";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == ToolWorkspace.this.process);
		}
		
		@Override
		public boolean changed(final int event, final ITool tool) {
			switch (event) {
			case REMOVING_FROM:
			case MOVING_FROM:
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			ToolWorkspace.this.isRefreshing= true;
			try {
				autoRefreshFromTool((IConsoleService) service, monitor);
			}
			finally {
				ToolWorkspace.this.isRefreshing= false;
			}
			firePropertiesChanged();
		}
		
	}
	
	
	public static final int DETAIL_PROMPT= 1;
	public static final int DETAIL_LINE_SEPARTOR= 2;
	
	
	private final ToolProcess process;
	
	private volatile String lineSeparator;
	private volatile char fileSeparator;
	
	private volatile Prompt currentPrompt;
	private volatile Prompt defaultPrompt;
	private Prompt publishedPrompt;
	
	private IFileStore workspaceDir;
	
	private final String remoteHost;
	private IPath remoteWorkspaceDirPath;
	
	private final Map<String, Object> properties= new HashMap<>();
	private final FastList<Listener> propertyListener= new FastList<>(Listener.class);
	
	private boolean autoRefreshEnabled= true;
	
	private boolean isRefreshing;
	
	private final ImList<IDynamicVariable> stringVariables;
	
	
	public ToolWorkspace(final ToolController controller,
			Prompt prompt, final String lineSeparator, final char fileSeparator,
			final String remoteHost) {
		this.process= controller.getTool();
		if (prompt == null) {
			prompt= Prompt.DEFAULT;
		}
		this.publishedPrompt= this.currentPrompt= this.defaultPrompt= prompt;
		this.remoteHost= remoteHost;
		controlSetLineSeparator(lineSeparator);
		controlSetFileSeparator(fileSeparator);
		
		controller.addToolStatusListener(new ControllerListener());
		controller.getQueue().addOnIdle(new AutoUpdater(), 5000);
		
		this.stringVariables= ImCollections.<IDynamicVariable>newList(
				new DateVariable(NicoVariables.SESSION_STARTUP_DATE_VARIABLE) {
					@Override
					protected long getTimestamp() {
						return ToolWorkspace.this.process.getStartupTimestamp();
					}
				},
				new TimeVariable(NicoVariables.SESSION_STARTUP_TIME_VARIABLE) {
					@Override
					protected long getTimestamp() {
						return ToolWorkspace.this.process.getStartupTimestamp();
					}
				},
				new DateVariable(NicoVariables.SESSION_CONNECTION_DATE_VARIABLE) {
					@Override
					protected long getTimestamp() {
						return ToolWorkspace.this.process.getConnectionTimestamp();
					}
				},
				new TimeVariable(NicoVariables.SESSION_CONNECTION_TIME_VARIABLE) {
					@Override
					protected long getTimestamp() {
						return ToolWorkspace.this.process.getStartupTimestamp();
					}
				},
				new DynamicVariable.LocationVariable(NicoVariables.SESSION_STARTUP_WD_VARIABLE) {
					@Override
					public String getValue(final String argument) throws CoreException {
						return ToolWorkspace.this.process.getStartupWD();
					}
				} );
	}
	
	
	public ToolProcess getProcess() {
		return this.process;
	}
	
	public boolean isWindows() {
		return (getFileSeparator() == '\\');
	}
	
	
	public void setAutoRefresh(final boolean enable) {
		synchronized (this.process.getQueue()) {
			if (this.autoRefreshEnabled != enable) {
				this.autoRefreshEnabled= enable;
				final ToolStatus status= this.process.getToolStatus();
				if (status != ToolStatus.TERMINATED) {
					if (enable && status.isWaiting()) {
						this.process.getQueue().internalResetIdle();
						this.process.getQueue().notifyAll();
					}
					addPropertyChanged("AutoRefresh.enabled", enable);
					firePropertiesChanged();
				}
			}
		}
	}
	
	public boolean isAutoRefreshEnabled() {
		return this.autoRefreshEnabled;
	}
	
	
	protected void autoRefreshFromTool(final IConsoleService s, final IProgressMonitor monitor) throws CoreException {
		if (this.autoRefreshEnabled) {
			refreshFromTool(0, s, monitor);
		}
	}
	
	protected void refreshFromTool(final int options, final IConsoleService s, final IProgressMonitor monitor) throws CoreException {
	}
	
	public final String getLineSeparator() {
		return this.lineSeparator;
	}
	
	public final char getFileSeparator() {
		return this.fileSeparator;
	}
	
	
	public final Prompt getPrompt() {
		return this.publishedPrompt;
	}
	
	protected final Prompt getCurrentPrompt() {
		return this.currentPrompt;
	}
	
	public final Prompt getDefaultPrompt() {
		return this.defaultPrompt;
	}
	
	public final IFileStore getWorkspaceDir() {
		return this.workspaceDir;
	}
	
	public String getEncoding() {
		return "UTF-8"; //$NON-NLS-1$
	}
	
	
	public final boolean isRemote() {
		return (this.remoteHost != null);
	}
	
	public String getRemoteAddress() {
		return this.remoteHost;
	}
	
	public IPath getRemoteWorkspaceDirPath() {
		return this.remoteWorkspaceDirPath;
	}
	
	public IPath createToolPath(String toolPath) {
		if (toolPath == null) {
			return null;
		}
		if (isWindows() && File.separatorChar == '/') {
			toolPath= toolPath.replace('\\', '/');
		}
		return PathUtils.check(new Path(toolPath));
	}
	
	public IFileStore toFileStore(final IPath toolPath) throws CoreException {
		if (this.remoteHost != null) {
			return ResourceMappingUtils.getManager()
					.mapRemoteResourceToFileStore(this.remoteHost, toolPath,
							(this.remoteWorkspaceDirPath != null) ? this.remoteWorkspaceDirPath : null );
		}
		return FileUtil.getFileStore(toolPath.toString(), this.workspaceDir);
	}
	
	public IFileStore toFileStore(final String toolPath) throws CoreException {
		if (this.remoteHost != null) {
			return toFileStore(createToolPath(toolPath));
		}
		return FileUtil.getFileStore(toolPath.toString(), this.workspaceDir);
	}
	
	public String toToolPath(final IFileStore fileStore) throws CoreException {
		if (this.remoteHost != null) {
			final IPath path= ResourceMappingUtils.getManager()
					.mapFileStoreToRemoteResource(this.remoteHost, fileStore);
			if (path != null) {
				return path.toString();
			}
			throw new CoreException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, "Resolving path for the remote system failed."));
		}
		return URIUtil.toPath(fileStore.toURI()).toString();
	}
	
	
	final void controlRefresh(final int options, final IConsoleService adapter, final IProgressMonitor monitor) throws CoreException {
		this.isRefreshing= true;
		try {
			refreshFromTool(options, adapter, monitor);
		}
		finally {
			this.isRefreshing= false;
		}
		firePropertiesChanged();
	}
	
	
	/**
	 * Use only in tool main thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	final void controlSetCurrentPrompt(final Prompt prompt, final ToolStatus status) {
		if (prompt == this.currentPrompt || prompt == null) {
			return;
		}
		this.currentPrompt= prompt;
		if (!status.isRunning()) {
			this.publishedPrompt= prompt;
			firePrompt(prompt, null);
		}
	}
	
	/**
	 * Use only in tool main thread.
	 * @param prompt the new prompt, null doesn't change anything
	 */
	final void controlSetDefaultPrompt(final Prompt prompt) {
		if (prompt == this.defaultPrompt || prompt == null) {
			return;
		}
		final Prompt oldDefault= this.defaultPrompt;
		this.defaultPrompt= prompt;
		if (oldDefault == this.currentPrompt) {
			this.currentPrompt= prompt;
		}
		if (oldDefault == this.publishedPrompt) {
			this.publishedPrompt= prompt;
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
		final String oldSeparator= this.lineSeparator;
		if (newSeparator != null) {
			this.lineSeparator= newSeparator;
		}
		else {
			this.lineSeparator= (this.remoteHost == null) ? System.getProperty("line.separator") : "\n"; //$NON-NLS-1$
		}
//		if (!fLineSeparator.equals(oldSeparator)) {
//			DebugEvent event= new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_LINE_SEPARTOR);
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
	final void controlSetFileSeparator(final char newSeparator) {
		final char oldSeparator= this.fileSeparator;
		if (newSeparator != 0) {
			this.fileSeparator= newSeparator;
		}
		else {
			this.fileSeparator= (isRemote()) ? '/' : File.separatorChar;
		}
	}
	
	protected final void controlSetWorkspaceDir(final IFileStore directory) {
		if ((this.workspaceDir != null) ? !this.workspaceDir.equals(directory) : directory != null) {
			this.workspaceDir= directory;
			this.properties.put("wd", directory);
			if (!this.isRefreshing) {
				firePropertiesChanged();
			}
		}
	}
	
	protected final void controlSetRemoteWorkspaceDir(final IPath toolPath) {
		this.remoteWorkspaceDirPath= toolPath;
		try {
			controlSetWorkspaceDir(toFileStore(toolPath));
		}
		catch (final CoreException e) {
			controlSetWorkspaceDir(null);
		}
	}
	
	
	private final void firePrompt(final Prompt prompt, final List<DebugEvent> eventCollection) {
		final DebugEvent event= new DebugEvent(ToolWorkspace.this, DebugEvent.CHANGE, DETAIL_PROMPT);
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
		final DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}
	
	public final void addPropertyListener(final Listener listener) {
		this.propertyListener.add(listener);
	}
	
	public final void removePropertyListener(final Listener listener) {
		this.propertyListener.remove(listener);
	}
	
	protected final void addPropertyChanged(final String property, final Object attr) {
		this.properties.put(property, attr);
	}
	
	protected final void firePropertiesChanged() {
		if (this.properties.isEmpty()) {
			return;
		}
		final Listener[] listeners= this.propertyListener.toArray();
		for (final Listener listener : listeners) {
			try {
				listener.propertyChanged(ToolWorkspace.this, this.properties);
			}
			catch (final Exception e) {
				NicoPlugin.logError(ICommonStatusConstants.INTERNAL_PLUGGED_IN, "An unexpected exception was thrown when notifying a tool workspace listener about changes.", e);
			}
		}
		this.properties.clear();
	}
	
	public ImList<IDynamicVariable> getStringVariables() {
		return this.stringVariables;
	}
	
	protected void dispose() {
	}
	
}
