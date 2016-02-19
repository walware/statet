/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.core.model.IModelElement;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RUI;


public final class RFrameSearchPath implements Iterable<IRFrame> {
	
	
	public static final int WORKSPACE_MODE= 1;
	public static final int CONSOLE_MODE= 2;
	public static final int ENGINE_MODE= 3;
	
	
	private static final int LOCAL_ID= 0;
	private static final int WORKSPACE_ID= 1;
	private static final int RUNTIME_ID= 2;
	
	
	public class RFrameIterator implements Iterator<IRFrame> {
		
		private int listIter0;
		private int listIter1= -1;
		private IRFrame next;
		
		@Override
		public boolean hasNext() {
			if (this.next != null) {
				return true;
			}
			ITER_0 : while (this.listIter0 < RFrameSearchPath.this.frames.length) {
				if (++this.listIter1 < RFrameSearchPath.this.frames[this.listIter0].size()) {
					this.next= RFrameSearchPath.this.frames[this.listIter0].get(this.listIter1);
					return true;
				}
				else {
					this.listIter0++;
					this.listIter1= -1;
					continue ITER_0;
				}
			}
			return false;
		}
		
		@Override
		public IRFrame next() {
			if (hasNext()) {
				final IRFrame frame= this.next;
				this.next= null;
				return frame;
			}
			throw new NoSuchElementException();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		public int getEnvirGroup() {
			return this.listIter0;
		}
		
		public int getRelevance() {
			switch (getEnvirGroup()) {
			case LOCAL_ID:
				return Math.max(9 - this.listIter1, 3);
			case WORKSPACE_ID:
				return 1;
			default:
				return -5;
			}
		}
		
	}
	
	
	private final List<IRFrame>[] frames= new List[3];
	
	private RElementName expliciteScope;
	private boolean packageMode;
	private Set<String> importedPackages;
	
	private final Set<String> workspacePackages= new HashSet<>();
	
	private RWorkspace runtimeWorkspace;
	
	
	public RFrameSearchPath() {
		this.frames[RUNTIME_ID]= new ArrayList<>();
	}
	
	
	public void init(final RAssistInvocationContext context, final RAstNode node,
			final int mode, final RElementName expliciteScope) {
		this.expliciteScope= (expliciteScope != null && RElementName.isPackageFacetScopeType(expliciteScope.getType())) ?
				expliciteScope : null;
		
		final IRFrameInSource envir= RModel.searchFrame(node);
		if (envir != null && mode <= CONSOLE_MODE) {
			this.frames[LOCAL_ID]= RModel.createDirectFrameList(envir, this.expliciteScope);
		}
		else {
			this.frames[LOCAL_ID]= new ArrayList<>();
		}
		
		if (mode == WORKSPACE_MODE) {
			final IRSourceUnit su= context.getSourceUnit();
			if (su != null) {
				if (this.expliciteScope == null && su instanceof IRWorkspaceSourceUnit) {
					final IRProject rProject= RProjects.getRProject(((IRWorkspaceSourceUnit) su)
							.getResource().getProject() );
					this.packageMode= (rProject != null && rProject.getPackageName() != null);
				}
				
				this.importedPackages= (this.expliciteScope != null) ?
						ImCollections.newSet(this.expliciteScope.getSegmentName()) :
						RModel.createImportedPackageList((IRModelInfo) context.getModelInfo());
				
				try {
					this.frames[WORKSPACE_ID]= RModel.createProjectFrameList(null,
							su,
							true, (this.expliciteScope == null),
							this.importedPackages, this.workspacePackages );
				}
				catch (final CoreException e) {
					// CANCELLED possible?
				}
				if (this.frames[WORKSPACE_ID] != null && !this.frames[WORKSPACE_ID].isEmpty()) {
					this.frames[LOCAL_ID].add(this.frames[WORKSPACE_ID].remove(0));
				}
			}
		}
		if (this.frames[WORKSPACE_ID] == null) {
			this.frames[WORKSPACE_ID]= new ArrayList<>();
		}
		
		this.runtimeWorkspace= getRuntimeWorkspace(context);
		
		addRuntimeFrames(context, mode >= CONSOLE_MODE);
	}
	
	private RWorkspace getRuntimeWorkspace(final RAssistInvocationContext context) {
		final RProcess tool= context.getTool();
		return (tool != null) ? tool.getWorkspaceData() : null;
	}
	
	private void addRuntimeFrames(final RAssistInvocationContext context,
			final boolean complete) {
		if (this.runtimeWorkspace != null && this.runtimeWorkspace.hasRObjectDB()) {
			if (complete) {
				if (this.expliciteScope != null) {
					final IRFrame frame= resolve(this.expliciteScope, context);
					this.frames[WORKSPACE_ID].add(frame);
					return;
				}
				
				addDebugFrame(context);
				
				final List<? extends ICombinedREnvironment> searchEnvs= this.runtimeWorkspace
						.getRSearchEnvironments();
				if (searchEnvs != null && !searchEnvs.isEmpty()) {
					for (final ICombinedREnvironment env : searchEnvs) {
						final IRFrame frame= (IRFrame) env;
						if (frame.getFrameType() == IRFrame.PROJECT) {
							this.frames[LOCAL_ID].add(frame);
						}
						else {
							this.frames[WORKSPACE_ID].add(frame);
						}
					}
				}
			}
			else if (this.importedPackages != null) {
				if (this.expliciteScope != null
						&& !this.workspacePackages.contains(
								this.expliciteScope.getSegmentName() )) {
					final IRFrame frame= resolve(this.expliciteScope, context);
					if (frame != null) {
						this.frames[RUNTIME_ID].add(frame);
					}
				}
				else if (this.packageMode) {
					final List<? extends ICombinedREnvironment> searchEnvs= 
							this.runtimeWorkspace.getRSearchEnvironments();
					
					for (final String pkgName : this.importedPackages) {
						if (!this.workspacePackages.contains(pkgName)) {
							IRFrame frame;
							frame= resolve(RElementName.create(RElementName.SCOPE_NS, pkgName),
									context );
							if (frame == null) { // timeout
								frame= searchPackage(searchEnvs, pkgName);
							}
							if (frame != null) {
								this.frames[RUNTIME_ID].add(frame);
							}
						}
					}
				}
				else {
					final List<? extends ICombinedREnvironment> searchEnvs=
							this.runtimeWorkspace.getRSearchEnvironments();
					for (final String pkgName : this.importedPackages) {
						if (!this.workspacePackages.contains(pkgName)) {
							IRFrame frame;
							frame= searchPackage(searchEnvs, pkgName);
							if (frame == null) {
								frame= resolve(RElementName.create(RElementName.SCOPE_NS, pkgName),
										context );
							}
							if (frame != null) {
								this.frames[RUNTIME_ID].add(frame);
							}
						}
					}
				}
			}
		}
	}
	
	
	private IRFrame searchPackage(final List<? extends ICombinedREnvironment> searchEnvs,
			final String pkgName) {
		if (searchEnvs != null) {
			for (final ICombinedREnvironment env : searchEnvs) {
				final IRFrame frame= (IRFrame) env;
				if (frame.getFrameType() == IRFrame.PACKAGE
						&& frame.getElementName().getSegmentName().equals(pkgName) ) {
					return frame;
				}
			}
		}
		return null;
	}
	
	private IRFrame resolve(final RElementName name,
			final RAssistInvocationContext context) {
		ICombinedRElement element= this.runtimeWorkspace.resolve(name,
				RWorkspace.RESOLVE_INDICATE_NA );
		if (element != null) {
			return (!this.runtimeWorkspace.isNA(element)
							&& element instanceof IRFrame) ?
					(IRFrame) element : null;
		}
		
		element= context.getToolReferencesUtil().resolve(name, 0);
		
		return (element instanceof IRFrame) ?
				(IRFrame) element : null;
	}
	
	private void addDebugFrame(final RAssistInvocationContext context) {
		final IDebugTarget debugTarget= context.getTool().getLaunch().getDebugTarget();
		if (debugTarget == null) {
			return;
		}
		try {
			final IThread[] threads= debugTarget.getThreads();
			if (threads.length > 0) {
				final IStackFrame top= threads[0].getTopStackFrame();
				if (top != null) {
					final ICombinedRElement envir= (ICombinedRElement) top.getAdapter(IModelElement.class);
					if (envir instanceof IRFrame) {
						final IRFrame frame= (IRFrame) envir;
						if (frame.getFrameType() != IRFrame.PACKAGE) {
							this.frames[LOCAL_ID].add(frame);
						}
					}
				}
			}
		}
		catch (final DebugException e) {
			if (e.getStatus().getCode() == DebugException.NOT_SUPPORTED
					|| e.getStatus().getSeverity() == IStatus.CANCEL) {
				return;
			}
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occurred when collecting environments for content assist.",
					e ));
		}
	}
	
	
	@Override
	public RFrameIterator iterator() {
		return new RFrameIterator();
	}
	
//	public RFrameIterator iterator(final RElementName name) {
//		if (name == null) {
//			return new RFrameIterator();
//		}
//		if (name.getType() == RElementName.SCOPE_NS_INT) {
//			this.runtimeWorkspace.getProcess().getQueue().add
//		}
//	}
	
	
	public void clear() {
		this.frames[LOCAL_ID]= null;
		this.frames[WORKSPACE_ID]= null;
		this.frames[RUNTIME_ID].clear();
		
		this.expliciteScope= null;
		this.importedPackages= null;
		this.workspacePackages.clear();
		
		this.runtimeWorkspace= null;
	}
	
}
