/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.rj.data.REnvironment;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.ui.util.RNameSearchPattern;


class ContentJob extends Job implements ToolWorkspace.Listener {
	
	
	static class ContentFilter implements IModelElement.Filter {
		
		private final boolean filterInternal;
		private final boolean filterNoPattern;
		private final SearchPattern searchPattern;
		
		
		public ContentFilter(final boolean filterInternal, final SearchPattern pattern) {
			this.filterInternal = filterInternal;
			this.filterNoPattern = (pattern == null);
			this.searchPattern = pattern;
		}
		
		
		@Override
		public boolean include(final IModelElement element) {
			final String name = element.getElementName().getSegmentName();
			if (name != null) {
				if (this.filterInternal && name.length() > 0 && name.charAt(0) == '.') {
					return false;
				}
				return (this.filterNoPattern || this.searchPattern.matches(name));
			}
			else {
				return true;
			}
		}
		
	}
	
	
	private final ObjectBrowserView view;
	
	/** true if RefreshR is running */
	private boolean forceOnWorkspaceChange;
	/** the process to update */
	private RProcess updateProcess;
	/** the process of last update */
	private RProcess lastProcess;
	/** update all environment */
	private boolean force;
	/** environments to update, if force is false*/
	private final Set<ICombinedREnvironment> updateSet = new HashSet<ICombinedREnvironment>();
	
	private List<? extends ICombinedREnvironment> rawInput;
	private List<? extends ICombinedRElement> userspaceInput;
	
	private volatile boolean isScheduled;
	
	
	public ContentJob(final ObjectBrowserView view) {
		super("R Object Browser Update");
		this.view = view;
		setSystem(true);
		setUser(false);
	}
	
	
	@Override
	public void propertyChanged(final ToolWorkspace workspace, final Map<String, Object> properties) {
		final RWorkspace rWorkspace = (RWorkspace) workspace;
		if (properties.containsKey("REnvironments")) {
			if (this.forceOnWorkspaceChange) {
				this.forceOnWorkspaceChange = false;
				final RProcess process = rWorkspace.getProcess();
				forceUpdate(process);
				schedule();
			}
			else {
				final List<RWorkspace.ICombinedREnvironment> envirs = (List<RWorkspace.ICombinedREnvironment>) properties.get("REnvironments");
				schedule(rWorkspace.getProcess(), envirs);
			}
		}
		
		final Object autorefresh = properties.get("AutoRefresh.enabled");
		if (autorefresh instanceof Boolean) {
			UIAccess.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (rWorkspace.getProcess() != ContentJob.this.view.getTool()) {
						return;
					}
					ContentJob.this.view.updateAutoRefresh(((Boolean) autorefresh).booleanValue());
				}
			});
		}
		else { // autorefresh already updates dirty
			final Object dirty = properties.get("RObjectDB.dirty");
			if (dirty instanceof Boolean) {
				UIAccess.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (rWorkspace.getProcess() != ContentJob.this.view.getTool()) {
							return;
						}
						ContentJob.this.view.updateDirty(((Boolean) dirty).booleanValue());
					}
				});
			}
		}
	}
	
	public void forceUpdate(final RProcess process) {
		synchronized (this.view.processLock) {
			if (process != this.view.getTool()) {
				return;
			}
			this.updateProcess = process;
			this.force = true;
			this.updateSet.clear();
		}
	}
	
	public void forceOnWorkspaceChange() {
		this.forceOnWorkspaceChange = true;
	}
	
	public void schedule(final RProcess process, final List<RWorkspace.ICombinedREnvironment> envirs) {
		if (envirs != null && process != null) {
			synchronized (this.view.processLock) {
				if (process != this.view.getTool()) {
					return;
				}
				this.updateProcess = process;
				if (!this.force) {
					this.updateSet.removeAll(envirs);
					this.updateSet.addAll(envirs);
				}
			}
		}
		schedule();
	}
	
	@Override
	public boolean shouldSchedule() {
		this.isScheduled = true;
		return true;
	}
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		if (!this.isScheduled) {
			return Status.CANCEL_STATUS;
		}
		this.isScheduled = false;
		
		final IWorkbenchSiteProgressService progressService = (IWorkbenchSiteProgressService) this.view.getViewSite().getService(IWorkbenchSiteProgressService.class);
		if (progressService != null) {
			progressService.incrementBusy();
		}
		
		try {
			final List<ICombinedREnvironment> updateList;
			final boolean force;
			final boolean updateInput;
			final RProcess process;
			synchronized (this.view.processLock) {
				force = this.force;
				updateInput = (force || this.updateProcess != null);
				process = (updateInput) ? this.updateProcess : this.view.getTool();
				if (process != this.view.getTool() || this.forceOnWorkspaceChange) {
					return Status.OK_STATUS;
				}
				updateList = new ArrayList<ICombinedREnvironment>(this.updateSet.size());
				updateList.addAll(this.updateSet);
				this.updateSet.clear();
				this.updateProcess = null;
				this.force = false;
			}
			
			final ContentInput input = createHandler(process, updateInput);
			
			// Update input and refresh
			final List<ICombinedREnvironment> toUpdate;
			if (updateInput) {
				toUpdate = updateInput((!force) ? updateList : null, process, input);
			}
			else {
				toUpdate = null;
			}
			
			if (this.rawInput != null) {
				prescan(input);
			}
			else if (process != null) {
				input.processChanged = false;
			}
			
			synchronized (this.view.processLock) {
				if (process != this.view.getTool()) {
					return Status.CANCEL_STATUS;
				}
				if ((!input.processChanged && this.isScheduled) || monitor.isCanceled()) {
					this.updateSet.addAll(updateList);
					this.force |= force;
					if (updateInput && this.updateProcess == null) {
						this.updateProcess = process;
					}
					return Status.CANCEL_STATUS;
				}
			}
			UIAccess.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					if (process != ContentJob.this.view.getTool()) {
						return;
					}
					ContentJob.this.view.updateViewer(toUpdate, input);
				}
			});
			
			if (this.rawInput != null) {
				this.lastProcess = process;
			}
			else {
				this.lastProcess = null;
			}
			return Status.OK_STATUS;
		}
		finally {
			if (progressService != null) {
				progressService.decrementBusy();
			}
		}
	}
	
	private ContentInput createHandler(final RProcess process, final boolean updateInput) {
		final boolean processChanged = ((process != null) ? process != this.lastProcess : this.lastProcess != null);
		final boolean filterInternal = !this.view.getShowInternal();
		final String filterText = this.view.getSearchText();
		IModelElement.Filter envFilter;
		IModelElement.Filter otherFilter;
		if (filterText != null && filterText.length() > 0) {
			final SearchPattern filterPattern = new RNameSearchPattern();
			filterPattern.setPattern(filterText);
			envFilter = new ContentFilter(filterInternal, filterPattern);
			otherFilter = (filterInternal) ? new ContentFilter(filterInternal, null) : null;
		}
		else if (filterInternal) {
			envFilter = new ContentFilter(filterInternal, null);
			otherFilter = new ContentFilter(filterInternal, null);
		}
		else {
			envFilter = null;
			otherFilter = null;
		}
		return new ContentInput(processChanged, updateInput, this.view.getShowConsenseUserspace(),
				envFilter, otherFilter );
	}
	
	private List<ICombinedREnvironment> updateInput(final List<ICombinedREnvironment> updateList, 
			final RProcess process, final ContentInput input) {
		if (process != null) {
			final List<? extends ICombinedREnvironment> oldInput = this.rawInput;
			final RWorkspace workspaceData = process.getWorkspaceData();
			this.rawInput = workspaceData.getRSearchEnvironments();
			if (this.rawInput == null || this.rawInput.size() == 0) {
				this.rawInput = null;
				this.userspaceInput = null;
				return null;
			}
			input.searchEnvirs = this.rawInput;
			// If search path (environments) is not changed and not in force mode, refresh only the updated entries
			List<ICombinedREnvironment> updateEntries = null;
			TRY_PARTIAL : if (!input.showCondensedUserspace
					&& this.rawInput != null && oldInput != null && this.rawInput.size() == oldInput.size()
					&& updateList != null && updateList.size() < this.rawInput.size() ) {
				updateEntries = new ArrayList<ICombinedREnvironment>(updateList.size());
				for (int i = 0; i < this.rawInput.size(); i++) {
					final ICombinedREnvironment envir = this.rawInput.get(i);
					if (envir.equals(oldInput.get(i))) {
						if (updateList.remove(envir)) {
							updateEntries.add(envir);
						}
					}
					else { // search path is changed
						updateEntries = null;
						break TRY_PARTIAL;
					}
				}
				if (!updateList.isEmpty()) {
					updateEntries = null;
					break TRY_PARTIAL;
				}
			}
			
			// Prepare Userspace filter
			if (input.showCondensedUserspace) {
				int length = 0;
				final List<ICombinedREnvironment> userEntries = new ArrayList<ICombinedREnvironment>(this.rawInput.size());
				for (final ICombinedREnvironment env : this.rawInput) {
					if (env.getSpecialType() > 0 && env.getSpecialType() <= REnvironment.ENVTYPE_PACKAGE) {
						continue;
					}
					userEntries.add(env);
					length += env.getLength();
				}
				final List<IModelElement> elements = new ArrayList<IModelElement>(length);
				for (final ICombinedREnvironment entry : userEntries) {
					elements.addAll(entry.getModelChildren((IModelElement.Filter) null));
				}
				
				final ICombinedRElement[] array = elements.toArray(new ICombinedRElement[elements.size()]);
				Arrays.sort(array, ObjectBrowserView.ELEMENTNAME_COMPARATOR);
				this.userspaceInput = new ConstArrayList<ICombinedRElement>(array);
			}
			else {
				this.userspaceInput = null;
			}
			return updateEntries;
		}
		else {
			this.rawInput = null;
			this.userspaceInput = null;
			return null;
		}
	}
	
	private void prescan(final ContentInput input) {
		// Prescan filter
		if (!input.showCondensedUserspace) {
			final ICombinedRElement[] array = this.rawInput.toArray(new ICombinedRElement[this.rawInput.size()]);
			if (input.hasEnvFilter()) {
				for (int i = 0; i < array.length; i++) {
					input.getEnvFilterChildren(array[i]);
				}
			}
			input.rootElements = array;
		}
		else {
			final List<? extends ICombinedRElement> list;
			if (input.hasEnvFilter()) {
				list = input.filterEnvChildren(this.userspaceInput);
			}
			else {
				list = this.userspaceInput;
			}
			input.rootElements = list.toArray(new ICombinedRElement[list.size()]);
		}
	}
	
}
