/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.text.IMarkerPositionResolver;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.data.REnvironment;
import de.walware.rj.server.dbg.DbgEnablement;
import de.walware.rj.server.dbg.ElementTracepointInstallationRequest;
import de.walware.rj.server.dbg.ElementTracepointPositions;
import de.walware.rj.server.dbg.FlagTracepointInstallationRequest;
import de.walware.rj.server.dbg.SrcfileData;
import de.walware.rj.server.dbg.Srcref;
import de.walware.rj.server.dbg.Tracepoint;
import de.walware.rj.server.dbg.TracepointEvent;
import de.walware.rj.server.dbg.TracepointInstallationReport;
import de.walware.rj.server.dbg.TracepointPosition;
import de.walware.rj.server.dbg.TracepointState;
import de.walware.rj.server.dbg.TracepointStatesUpdate;

import de.walware.statet.r.console.core.RDbg;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.debug.core.IRDebugTarget;
import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint.ITargetData;
import de.walware.statet.r.debug.core.breakpoints.IRExceptionBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.RLineBreakpointValidator;
import de.walware.statet.r.debug.core.breakpoints.RLineBreakpointValidator.ModelPosition;
import de.walware.statet.r.internal.debug.core.RDebugCorePlugin;
import de.walware.statet.r.nico.AbstractRDbgController;
import de.walware.statet.r.nico.AbstractRDbgController.IRControllerTracepointAdapter;
import de.walware.statet.r.nico.IRModelSrcref;
import de.walware.statet.r.nico.IRSrcref;


public class RControllerBreakpointAdapter implements IRControllerTracepointAdapter,
		IBreakpointManagerListener, IBreakpointsListener {
	
	private static class Position extends TracepointPosition {
		
		private final IRLineBreakpoint breakpoint;
		
		private String label;
		
		public Position(final int type, final long id, final int[] exprIndex,
				final IRLineBreakpoint breakpoint) {
			super(type, id, exprIndex, null);
			this.breakpoint= breakpoint;
		}
		
		void setExprSrcref(final int[] srcref) {
			this.exprSrcref= srcref;
		}
		
		public IRLineBreakpoint getBreakpoint() {
			return this.breakpoint;
		}
		
		void setLabel(final String label) {
			this.label= label;
		}
		
		public String getElementLabel() {
			return this.label;
		}
		
	}
	
	private static class Element extends ElementTracepointPositions {
		
		private final IResource resource;
		
		public Element(final SrcfileData fileInfo, final IResource resource,
				final String elementId, final int[] elementSrcref) {
			super(fileInfo, elementId, elementSrcref);
			this.resource= resource;
		}
		
		@Override
		public List<Position> getPositions() {
			return (List<Position>) super.getPositions();
		}
		
		public IResource getResource() {
			return this.resource;
		}
		
	}
	
	
	
	private static class UpdateData {
		
		private final IResource resource;
		private final String elementId;
		
		public UpdateData(final IResource resource, final String elementId) {
			this.resource= resource;
			this.elementId= elementId;
		}
		
	}
	
	private static class ElementData implements IRBreakpoint.ITargetData {
		
		final Element installed;
		
		public ElementData(final Element installed) {
			this.installed= installed;
		}
		
		@Override
		public boolean isInstalled() {
			return (this.installed != null);
		}
		
		@Override
		public Object getAdapter(final Class adapter) {
			return null;
		}
		
	}
	
	private static final IRBreakpoint.ITargetData INSTALLED_DATA= new IRBreakpoint.ITargetData() {
		
		@Override
		public boolean isInstalled() {
			return true;
		}
		
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			return null;
		}
		
	};
	
	private static final IRBreakpoint.ITargetData NOT_INSTALLED_DATA= new IRBreakpoint.ITargetData() {
		
		@Override
		public boolean isInstalled() {
			return false;
		}
		
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			return null;
		}
		
	};
	
	
	private final IRDebugTarget debugTarget;
	private final AbstractRDbgController controller;
	
	private IBreakpointManager breakpointManager;
	
	private boolean initialized;
	
	private final List<IRLineBreakpoint> positionUpdatesBreakpoints= new ArrayList<>();
	private final List<UpdateData> positionUpdatesElements= new ArrayList<>();
	private final AtomicInteger positionModCounter= new AtomicInteger();
	private final Object positionUpdatesLock= this.positionUpdatesBreakpoints;
	
	private final Object flagUpdateLock= new Object();
	private boolean flagUpdateCheck;
	private ITargetData exceptionBreakpointData;
	
	private final List<IRBreakpoint> stateUpdatesBreakpoints= new ArrayList<>();
	private final Object stateUpdatesLock= this.stateUpdatesBreakpoints;
	
	private final Map<IResource, List<TracepointState>> stateUpdatesMap= new HashMap<>();
	
	private final ISystemRunnable updateRunnable= new ISystemRunnable() {
		
		private List<String> knownPackages= new ArrayList<>();
		
		@Override
		public String getTypeId() {
			return "r/dbg/breakpoint.update";
		}
		
		@Override
		public String getLabel() {
			return "Update Breakpoints";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == RControllerBreakpointAdapter.this.controller.getTool());
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
			boolean checkInstalled= (RControllerBreakpointAdapter.this.controller.getHotTasksState() <= 1);
			
			synchronized (RControllerBreakpointAdapter.this.updateRunnable) {
				RControllerBreakpointAdapter.this.updateRunnableScheduled= false;
			}
			
			{	// init
				if (!RControllerBreakpointAdapter.this.initialized) {
					synchronized (RControllerBreakpointAdapter.this.flagUpdateLock) {
						RControllerBreakpointAdapter.this.flagUpdateCheck= false;
					}
					final IBreakpoint[] breakpoints= RControllerBreakpointAdapter.this.breakpointManager.getBreakpoints(
							RDebugModel.IDENTIFIER );
					try {
						boolean exceptionAvailable= false;
						synchronized (RControllerBreakpointAdapter.this.stateUpdatesLock) {
							for (int i= 0; i < breakpoints.length; i++) {
								if (breakpoints[i] instanceof IRBreakpoint) {
									final IRBreakpoint breakpoint= (IRBreakpoint) breakpoints[i];
									scheduleStateUpdate((IRBreakpoint) breakpoints[i]);
									
									if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
										exceptionAvailable= true;
									}
								}
							}
						}
						{	final FlagTracepointInstallationRequest request= createFlagRequest(
									(exceptionAvailable) ? Boolean.TRUE : null );
							if (request != null) {
								installFlagTracepoints(request, breakpoints, monitor);
							}
						}
						synchronized (RControllerBreakpointAdapter.this.positionUpdatesLock) {
							for (int i= 0; i < breakpoints.length; i++) {
								if (breakpoints[i] instanceof IRLineBreakpoint) {
									schedulePositionUpdate((IRLineBreakpoint) breakpoints[i]);
								}
							}
						}
						{	final List<Element> positions= getPendingElementPositions(monitor);
							installElementTracepoints(
									new ElementTracepointInstallationRequest(positions),
									monitor );
						}
						synchronized (RControllerBreakpointAdapter.this.stateUpdatesMap) {
							final List<TracepointState> states= getPendingTracepointStates();
							RControllerBreakpointAdapter.this.controller.exec(
									new TracepointStatesUpdate(states, true),
									monitor );
						}
					}
					finally {
						RControllerBreakpointAdapter.this.initialized= true;
						checkInstalled= false;
						checkUpdates();
					}
				}
				
				// regular
				List<String> newPackages= null;
				
				final List<? extends ICombinedREnvironment> environments= RControllerBreakpointAdapter
						.this.controller.getWorkspaceData().getRSearchEnvironments();
				if (environments != null) {
					final List<String> packages= new ArrayList<>(environments.size() - 1);
					for (final ICombinedREnvironment environment : environments) {
						if (environment.getSpecialType() == REnvironment.ENVTYPE_PACKAGE) {
							final String pkgName= environment.getElementName().getSegmentName();
							packages.add(pkgName);
							if (this.knownPackages != null && !this.knownPackages.contains(pkgName)) {
								if (newPackages == null) {
									newPackages= new ArrayList<>(4);
								}
								newPackages.add(pkgName);
							}
						}
					}
					if (this.knownPackages == null) {
						newPackages= packages;
					}
					this.knownPackages= packages;
				}
				
				if (newPackages != null || checkInstalled) {
					final IBreakpoint[] breakpoints= RControllerBreakpointAdapter
							.this.breakpointManager.getBreakpoints(RDebugModel.IDENTIFIER);
					Map<String, IRProject> rProjects= null;
					boolean exceptionAvailable= false;
					for (int i= 0; i < breakpoints.length; i++) {
						if (breakpoints[i] instanceof IRBreakpoint) {
							final IRBreakpoint breakpoint= (IRBreakpoint) breakpoints[i];
							final IMarker marker= breakpoint.getMarker();
							if (marker == null) {
								continue;
							}
							
							if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
								exceptionAvailable= true;
							}
							else if (breakpoint instanceof IRLineBreakpoint) {
								final IRLineBreakpoint lineBreakpoint= (IRLineBreakpoint) breakpoint;
								
								if (checkInstalled) {
									final ITargetData targetData= lineBreakpoint.getTargetData(RControllerBreakpointAdapter.this.debugTarget);
									if (targetData != null && targetData.isInstalled()) {
										schedulePositionUpdate(lineBreakpoint);
										continue;
									}
								}
								
								if (newPackages != null) {
									final IProject project= marker.getResource().getProject();
									if (rProjects == null) {
										rProjects= new HashMap<>();
									}
									IRProject rProject= rProjects.get(project.getName());
									if (rProject == null) {
										rProject= RProjects.getRProject(project);
										if (rProject == null) {
											continue; // ?
										}
										rProjects.put(project.getName(), rProject);
									}
									
									final String pkgName= rProject.getPackageName();
									if (newPackages.contains(pkgName)) {
										schedulePositionUpdate(lineBreakpoint);
										continue;
									}
								}
							}
						}
					}
					
					{	final FlagTracepointInstallationRequest request= createFlagRequest(
								(((RControllerBreakpointAdapter.this.exceptionBreakpointData != null) ? RControllerBreakpointAdapter.this.exceptionBreakpointData.isInstalled() : false)
												!= exceptionAvailable ) ?
										Boolean.valueOf(exceptionAvailable) : null );
						if (request != null) {
							installFlagTracepoints(request, breakpoints, monitor);
						}
					}
				}
				
				while (true) {
					final List<Element> positions= getPendingElementPositions(monitor);
					if (!positions.isEmpty()) {
						installElementTracepoints(
								new ElementTracepointInstallationRequest(positions),
								monitor );
					}
					else {
						break;
					}
				}
			}
		}
		
	};
	
	private boolean updateRunnableScheduled;
	
	
	public RControllerBreakpointAdapter(final IRDebugTarget target,
			final AbstractRDbgController controller) {
		this.debugTarget= target;
		this.controller= controller;
		
		this.breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		this.breakpointManager.addBreakpointManagerListener(this);
		this.breakpointManager.addBreakpointListener(this);
		
		breakpointManagerEnablementChanged(this.breakpointManager.isEnabled());
	}
	
	public void init() {
		final Queue queue= this.controller.getTool().getQueue();
		queue.addHot(this.updateRunnable);
		queue.addOnIdle(this.updateRunnable, 5500);
	}
	
	
	public boolean supportsBreakpoint(final IRBreakpoint breakpoint) {
		final String breakpointType= breakpoint.getBreakpointType();
		switch (breakpointType) {
		case RDebugModel.R_LINE_BREAKPOINT_TYPE_ID:
		case RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID:
		case RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID:
			return true;
		default:
			return false;
		}
	}
	
	/** Call in R thread */
	@Override
	public boolean matchScriptBreakpoint(final IRModelSrcref srcref,
			final IProgressMonitor monitor) {
		try {
			if (srcref instanceof IAdaptable) {
				final IMarker marker= ((IAdaptable) srcref).getAdapter(IMarker.class);
				final ISourceUnit su= srcref.getFile();
				if (marker != null && su instanceof IRWorkspaceSourceUnit
						&& marker.getResource() == su.getResource()) {
					return doMatchScriptBreakpoint(srcref,
							(IRWorkspaceSourceUnit) su, marker,
							monitor );
				}
			}
			return false;
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when looking for script breakpoints.", e));
			return false;
		}
	}
	
	private boolean doMatchScriptBreakpoint(final IRModelSrcref srcref,
			final IRWorkspaceSourceUnit rSourceUnit, final IMarker marker,
			final IProgressMonitor monitor) throws CoreException {
		final List<IRLineBreakpoint> breakpoints= RDebugModel.getLineBreakpoints(
				(IFile) rSourceUnit.getResource() );
		if (breakpoints.isEmpty()) {
			return false;
		}
		final IMarkerPositionResolver resolver= rSourceUnit.getMarkerPositionResolver();
		synchronized ((resolver != null && resolver.getDocument() instanceof ISynchronizable) ? ((ISynchronizable) resolver.getDocument()).getLockObject() : new Object()) {
			final int lineNumber= getLineNumber(marker, resolver);
			if (lineNumber < 0) {
				return false;
			}
			for (final IRLineBreakpoint breakpoint : breakpoints) {
				try {
					if (isScriptBreakpoint(breakpoint)
							&& ((resolver != null) ? resolver.getLine(breakpoint.getMarker()) : breakpoint.getLineNumber()) == lineNumber ) {
						return breakpoint.isEnabled();
					}
				}
				catch (final CoreException e) {
					RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
							"An error occurred when checking breakpoints.", e));
				}
				
			}
			return false;
		}
	}
	
	
	private FlagTracepointInstallationRequest createFlagRequest(final Boolean exception) {
		int count= 0;
		if (exception != null) {
			count++;
		}
		if (count == 0) {
			return null;
		}
		final byte[] types= new byte[count];
		final int[] flags= new int[count];
		
		int i= 0;
		if (exception != null) {
			types[i]= Tracepoint.TYPE_EB;
			flags[i]= (exception.booleanValue()) ? TracepointState.FLAG_ENABLED : 0;
			i++;
		}
		return new FlagTracepointInstallationRequest(types, flags);
	}
	
	/** Call in R thread */
	private void installFlagTracepoints(final FlagTracepointInstallationRequest request,
			final IBreakpoint[] breakpoints,
			final IProgressMonitor monitor) {
		TracepointInstallationReport report= null;
		try {
			report= this.controller.exec(request, monitor);
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when updating breakpoints in R." , e ));
		}
		finally {
			report(request, breakpoints, report);
			checkUpdates();
		}
	}
	
	private void report(final FlagTracepointInstallationRequest request,
			final IBreakpoint[] breakpoints, final TracepointInstallationReport report) {
		IRBreakpoint.ITargetData exceptionData= null;
		final byte[] types= request.getTypes();
		final int[] results= report.getInstallationResults();
		for (int i= 0; i < types.length; i++) {
			if (types[i] == Tracepoint.TYPE_EB) {
				switch (results[i]) {
				case TracepointInstallationReport.FOUND_SET:
					exceptionData= INSTALLED_DATA;
					break;
				case TracepointInstallationReport.FOUND_UNSET:
					exceptionData= NOT_INSTALLED_DATA;
					break;
				}
			}
		}
		if (exceptionData == null) {
			return;
		}
		synchronized (this.flagUpdateLock) {
			if (exceptionData != null) {
				this.exceptionBreakpointData= exceptionData;
			}
		}
		
		for (int i= 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof IRBreakpoint) {
				final IRBreakpoint breakpoint= (IRBreakpoint) breakpoints[i];
				if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
					if (exceptionData != null) {
						breakpoint.registerTarget(this.debugTarget, exceptionData);
					}
				}
			}
		}
	}
	
	/** Call in R thread */
	@Override
	public ElementTracepointInstallationRequest getElementTracepoints(final SrcfileData srcfile,
			final IRModelSrcref srcref,
			final IProgressMonitor monitor) {
		try {
			final ISourceUnit su= srcref.getFile();
			if (su instanceof IRWorkspaceSourceUnit) {
				return doGetElementTracepoints(srcfile, srcref,
						(IRWorkspaceSourceUnit) su,
						monitor );
			}
			return null;
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when looking for script list.", e));
			return null;
		}
	}
	
	private ElementTracepointInstallationRequest doGetElementTracepoints(final SrcfileData srcfile,
			final IRModelSrcref srcref,
			final IRWorkspaceSourceUnit rSourceUnit,
			final IProgressMonitor monitor) throws CoreException, BadLocationException {
		if (rSourceUnit.getResource().getType() != IResource.FILE
				|| !rSourceUnit.getResource().exists()) {
			return null;
		}
		
		List<? extends IRLangSourceElement> elements= srcref.getElements();
		if (elements.isEmpty()) {
			return null;
		}
		final int modCounter= this.positionModCounter.get();
		final List<IRLineBreakpoint> breakpoints= RDebugModel.getLineBreakpoints(
				(IFile) rSourceUnit.getResource() );
		if (breakpoints.isEmpty()) {
			return null;
		}
		
		rSourceUnit.connect(monitor);
		try {
			final AbstractDocument document= rSourceUnit.getDocument(monitor);
			synchronized ((document instanceof ISynchronizable) ? ((ISynchronizable) document).getLockObject() : new Object()) {
				final IRModelInfo modelInfo= (IRModelInfo) rSourceUnit.getModelInfo(
						RModel.R_TYPE_ID, IRModelManager.MODEL_FILE, monitor );
				if (elements.get(0).getSourceParent() != modelInfo.getSourceElement()) {
					final List<? extends IRLangSourceElement> orgElements= elements;
					elements= modelInfo.getSourceElement().getSourceChildren(new IModelElement.Filter() {
						@Override
						public boolean include(final IModelElement element) {
							return orgElements.contains(element);
//									return map.containsKey(element.getId());
						}
					});
				}
				if (elements.isEmpty()) {
					return null;
				}
				final IMarkerPositionResolver resolver= rSourceUnit.getMarkerPositionResolver();
				final int[] lines= new int[elements.size()*2];
				for (int i= 0, j= 0; i < elements.size(); i++, j+=2) {
					final IRegion region= elements.get(i).getSourceRange();
					lines[j]= document.getLineOfOffset(region.getOffset()) + 1;
					lines[j+1]= document.getLineOfOffset(region.getOffset()+region.getLength()) + 1;
				}
				HashMap<String, Element> map= null;
				List<String> cleanup= null;
				for (final IRLineBreakpoint breakpoint : breakpoints) {
					try {
						if (isElementBreakpoint(breakpoint)) {
							final IMarker marker= breakpoint.getMarker();
							final int breakpointLineNumber= (resolver != null) ?
									resolver.getLine(breakpoint.getMarker()) :
									breakpoint.getLineNumber();
							for (int j= 0; j < lines.length; j+=2) {
								if (lines[j] <= breakpointLineNumber && lines[j+1] >= breakpointLineNumber) {
									final RLineBreakpointValidator validator= (resolver != null) ?
											new RLineBreakpointValidator(rSourceUnit,
													breakpoint.getBreakpointType(),
													resolver.getPosition(marker).getOffset(),
													monitor ) :
											new RLineBreakpointValidator(rSourceUnit,
													breakpoint, monitor );
									final String elementId;
									if (validator.getType() == breakpoint.getBreakpointType()
											&& (elementId= validator.computeElementId()) != null
											&& elements.contains(validator.getBaseElement()) ) {
										if (map == null) {
											map= new HashMap<>(elements.size());
										}
										final ElementData breakpointData= (ElementData) breakpoint.getTargetData(this.debugTarget);
										if (breakpointData != null && breakpointData.installed != null
												&& !elementId.equals(breakpointData.installed.getElementId()) ) {
											if (cleanup == null) {
												cleanup= new ArrayList<>();
											}
											if (!cleanup.contains(breakpointData.installed.getElementId())) {
												cleanup.add(breakpointData.installed.getElementId());
											}
										}
										addBreakpoint(map, srcfile, rSourceUnit.getResource(),
												elementId, breakpoint, validator, modCounter );
									}
									break;
								}
							}
						}
					}
					catch (final CoreException e) {
						RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
								"An error occurred when checking breakpoint.", e));
					}
				}
				if (cleanup != null) {
					cleanup.removeAll(map.keySet());
					if (!cleanup.isEmpty()) {
						synchronized (this.positionUpdatesLock) {
							for (int i= 0; i < cleanup.size(); i++) {
								this.positionUpdatesElements.add(
										new UpdateData(rSourceUnit.getResource(), cleanup.get(i)));
							}
						}
					}
				}
				if (map != null) {
					final ArrayList<Element> list= new ArrayList<>(map.size());
					addElements(list, map, false);
					if (!list.isEmpty()) {
						return new ElementTracepointInstallationRequest(list);
					}
				}
			}
		}
		finally {
			rSourceUnit.disconnect(monitor);
		}
		return null;
	}
	
	@Override
	public ElementTracepointInstallationRequest prepareFileElementTracepoints(final SrcfileData srcfile,
			final IRSourceUnit su,
			final IProgressMonitor monitor) {
		try {
			if (su instanceof IRWorkspaceSourceUnit) {
				return doPrepareFileElementTracepoints(srcfile, (IRWorkspaceSourceUnit) su, monitor);
			}
			return null;
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when looking for line breakpoints.", e));
			return null;
		}
	}
	
	private ElementTracepointInstallationRequest doPrepareFileElementTracepoints(final SrcfileData srcfile,
			final IRWorkspaceSourceUnit rSourceUnit,
			final IProgressMonitor monitor) throws CoreException {
		if (rSourceUnit.getResource().getType() != IResource.FILE
				|| !rSourceUnit.getResource().exists()) {
			return null;
		}
		
		final int modeCounter= this.positionModCounter.get();
		final List<IRLineBreakpoint> breakpoints= RDebugModel.getLineBreakpoints(
				(IFile) rSourceUnit.getResource() );
		if (breakpoints.isEmpty()) {
			return null;
		}
		Map<String, Element> map= null;
		for (final IRLineBreakpoint breakpoint : breakpoints) {
			try {
				if (isElementBreakpoint(breakpoint)) {
					final RLineBreakpointValidator validator= new RLineBreakpointValidator(
							rSourceUnit, breakpoint, monitor );
					final String elementId;
					if (validator.getType() == breakpoint.getBreakpointType()
							&& (elementId= validator.computeElementId()) != null ) {
//									|| (((elementId= validator.computeElementId()) != null) ?
//											!elementId.equals(breakpoint.getElementId()) :
//											null != breakpoint.getElementId() )) {
						if (map == null) {
							map= new HashMap<>(breakpoints.size());
						}
						addBreakpoint(map, srcfile, rSourceUnit.getResource(), elementId,
								breakpoint, validator, modeCounter );
					}
				}
			}
			catch (final CoreException e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
						"An error occurred when checking breakpoint.", e));
			}
		}
		if (map != null) {
			final ArrayList<Element> list= new ArrayList<>(map.size());
			addElements(list, map, false);
			if (!list.isEmpty()) {
				return new ElementTracepointInstallationRequest(list);
			}
		}
		return null;
	}
	
	@Override
	public void installElementTracepoints(final ElementTracepointInstallationRequest request,
			final IProgressMonitor monitor) {
		TracepointInstallationReport report= null;
		try {
			report= this.controller.exec(request, monitor);
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when updating breakpoints in R." , e ));
		}
		finally {
			report(request, report);
			checkUpdates();
		}
	}
	
	/** Call in R thread */
	private List<Element> getPendingElementPositions(final IProgressMonitor monitor) {
		final IRBreakpoint[] breakpointsToUpdate;
		final UpdateData[] elementsToUpdate;
		synchronized (this.positionUpdatesLock) {
			if (this.positionUpdatesBreakpoints.isEmpty() && this.positionUpdatesElements.isEmpty()) {
				return ImCollections.emptyList();
			}
			breakpointsToUpdate= this.positionUpdatesBreakpoints.toArray(new IRBreakpoint[this.positionUpdatesBreakpoints.size()]);
			this.positionUpdatesBreakpoints.clear();
			elementsToUpdate= this.positionUpdatesElements.toArray(new UpdateData[this.positionUpdatesElements.size()]);
			this.positionUpdatesElements.clear();
		}
		final Map<IResource, @Nullable Map<String, Element>> resourceMap= new HashMap<>();
		// by resources
		for (int i= 0; i < breakpointsToUpdate.length; i++) {
			if (breakpointsToUpdate[i] instanceof IRLineBreakpoint) {
				final IRLineBreakpoint lineBreakpoint= (IRLineBreakpoint) breakpointsToUpdate[i];
				try {
					final ElementData breakpointData= (ElementData) lineBreakpoint.getTargetData(this.debugTarget);
					if (breakpointData != null && breakpointData.installed != null) {
						Map<String, Element> map= resourceMap.get(breakpointData.installed.getResource());
						if (map == null) {
							map= new HashMap<>();
							resourceMap.put(breakpointData.installed.getResource(), map);
						}
						map.put(breakpointData.installed.getElementId(), null);
					}
					
					if (lineBreakpoint.isRegistered() && isElementBreakpoint(lineBreakpoint)) {
						final IResource resource= lineBreakpoint.getMarker().getResource();
						if (!resourceMap.containsKey(resource)) {
							resourceMap.put(resource, new HashMap<String, Element>());
						}
					}
					else if (breakpointData != null) {
						lineBreakpoint.unregisterTarget(this.debugTarget);
					}
				}
				catch (final CoreException e) {
					logPrepareError(e, lineBreakpoint);
				}
			}
		}
		for (int i= 0; i < elementsToUpdate.length; i++) {
			final UpdateData updateData= elementsToUpdate[i];
			Map<String, Element> map= resourceMap.get(updateData.resource);
			if (map == null) {
				map= new HashMap<>();
				resourceMap.put(updateData.resource, map);
			}
			map.put(updateData.elementId, null);
		}
		
		int n= 0;
		for (final Entry<IResource, Map<String, Element>> resourceEntry : resourceMap.entrySet()) {
			final IResource resource= resourceEntry.getKey();
			final Map<String, Element> map= resourceEntry.getValue();
			try {
				final SrcfileData srcfile= RDbg.createRJSrcfileData(resource);
				if (resource.exists() && resource.getType() == IResource.FILE) {
					final ISourceUnit su= LTK.getSourceUnitManager().getSourceUnit(
							LTK.PERSISTENCE_CONTEXT, resource, null, true, monitor );
					if (su != null) {
						try {
							if (su instanceof IRWorkspaceSourceUnit) {
								doGetPendingElementPositions(srcfile, (IRWorkspaceSourceUnit) su,
										breakpointsToUpdate, map, monitor);
								continue;
							}
						}
						finally {
							su.disconnect(monitor);
						}
					}
				}
				
				for (final String elementId : map.keySet()) {
					addClear(map, srcfile, resource, elementId);
				}
			}
			catch (final CoreException e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, -1,
						NLS.bind("An error occurred when preparing update of R line breakpoints in ''{0}''.",
								resource.getFullPath().toString() ), e ));
			}
			n += map.size();
		}
		
		final List<Element> list= new ArrayList<>(n);
		for (final Entry<IResource, Map<String, Element>> resourceEntry : resourceMap.entrySet()) {
			addElements(list, resourceEntry.getValue(), true);
		}
		return list;
	}
	
	private void doGetPendingElementPositions(final SrcfileData srcfile, final IRWorkspaceSourceUnit rSourceUnit,
			final IRBreakpoint[] breakpointsToUpdate, final Map<String, Element> map,
			final IProgressMonitor monitor) throws CoreException {
		final int modCounter= this.positionModCounter.get();
		final List<IRLineBreakpoint> breakpoints= RDebugModel.getLineBreakpoints(
				(IFile) rSourceUnit.getResource() );
		for (final IRLineBreakpoint lineBreakpoint : breakpoints) {
			if (contains(breakpointsToUpdate, lineBreakpoint)) {
				try {
					if (lineBreakpoint.isEnabled() && isElementBreakpoint(lineBreakpoint)) {
						final RLineBreakpointValidator validator= new RLineBreakpointValidator(
								rSourceUnit,
								lineBreakpoint.getBreakpointType(), lineBreakpoint.getCharStart(),
								monitor );
						final String elementId;
						if (validator.getType() == lineBreakpoint.getBreakpointType()
								&& (elementId= validator.computeElementId()) != null ) {
							addBreakpoint(map, srcfile, rSourceUnit.getResource(), elementId,
									lineBreakpoint, validator, modCounter );
						}
					}
				}
				catch (final CoreException e) {
					logPrepareError(e, lineBreakpoint);
				}
			}
		}
		for (final IRLineBreakpoint lineBreakpoint : breakpoints) {
			if (!contains(breakpointsToUpdate, lineBreakpoint)) {
				try {
					if (lineBreakpoint.isEnabled() && isElementBreakpoint(lineBreakpoint)) {
						final RLineBreakpointValidator validator= new RLineBreakpointValidator(
								rSourceUnit,
								lineBreakpoint.getBreakpointType(), lineBreakpoint.getCharStart(),
								monitor );
						final String elementId;
						if (validator.getType() == lineBreakpoint.getBreakpointType()
								&& (elementId= validator.computeElementId()) != null
								&& map.containsKey(elementId) ) {
							addBreakpoint(map, srcfile, rSourceUnit.getResource(), elementId,
									lineBreakpoint, validator, modCounter );
						}
					}
				}
				catch (final CoreException e) {
					logPrepareError(e, lineBreakpoint);
				}
			}
		}
		for (final Entry<String, Element> elementEntry : map.entrySet()) {
			if (elementEntry.getValue() == null) {
				addClear(map, srcfile, rSourceUnit.getResource(), elementEntry.getKey());
			}
		}
	}
	
	private void logPrepareError(final CoreException e, final IRBreakpoint breakpoint) {
		if (breakpoint instanceof IRLineBreakpoint) {
			String fileName= null;
			final IMarker marker= breakpoint.getMarker();
			if (marker != null) {
				final IResource resource= marker.getResource();
				if (resource != null) {
					fileName= resource.getFullPath().toString();
				}
			}
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, -1,
					NLS.bind("An error occurred when preparing update of an R line breakpoint in ''{0}''.",
							(fileName != null) ? fileName : "<missing>" ), e ));
		}
		else {
			String exceptionId= null;
			try {
				exceptionId= ((IRExceptionBreakpoint) breakpoint).getExceptionId();
			}
			catch (final CoreException ignore) {}
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, -1,
					NLS.bind("An error occurred when preparing update of an R error breakpoint ''{0}''.",
							(exceptionId != null) ? exceptionId : "<missing>" ), e ));
		}
	}
	
	/** Call in R thread */
	private void report(final ElementTracepointInstallationRequest request,
			final TracepointInstallationReport report) {
		if (request == null) {
			throw new NullPointerException();
		}
		final List<? extends ElementTracepointPositions> elements= request.getRequests();
		final int l= elements.size();
		int[] results= null;
		if (report != null && report.getInstallationResults().length == l) {
			results= report.getInstallationResults();
		}
		
		if (results == null) {
			return; // ?
		}
		
		ArrayList<Element> cleanup= null;
		List<IRLineBreakpoint> updated= null;
		
		for (int i= 0; i < l; i++) {
			if (results[i] == TracepointInstallationReport.FOUND_UNCHANGED
					|| !(elements.get(i) instanceof Element)) {
				continue;
			}
			
			final Element current= (Element) elements.get(i);
			final boolean installed= (results[i] == TracepointInstallationReport.FOUND_SET);
			for (final TracepointPosition position : current.getPositions()) {
				if (!(position instanceof Position)) {
					continue;
				}
				final IRLineBreakpoint lineBreakpoint= ((Position) position).getBreakpoint();
				try {
					if (lineBreakpoint == null || !lineBreakpoint.isRegistered()) {
						continue;
					}
					final IMarker marker= lineBreakpoint.getMarker();
					if (marker == null || marker.getId() != position.getId()) {
						continue;
					}
					final ElementData newData= new ElementData((installed) ? current : null);
					final ElementData oldData= (ElementData) lineBreakpoint.registerTarget(this.debugTarget, newData);
					if (oldData != null && oldData.installed != null
							&& oldData.installed.getElementId().equals(current.getElementId())) {
						if (cleanup == null) {
							cleanup= new ArrayList<>(l-i);
						}
						if (!contains(cleanup, oldData.installed)) {
							cleanup.add(oldData.installed);
						}
					}
					if (updated == null) {
						updated= new ArrayList<>(l - i);
					}
					updated.add(breakpoint);
				}
				catch (final CoreException e) {} // only isRegistered
			}
		}
		
		if (cleanup != null) {
			for (int i= 0; i < cleanup.size(); i++) {
				final Element current= cleanup.get(i);
				for (final TracepointPosition position : current.getPositions()) {
					if (!(position instanceof Position)) {
						continue;
					}
					final IRBreakpoint breakpoint= ((Position) position).getBreakpoint();
					try {
						if (breakpoint == null || !breakpoint.isRegistered()) {
							continue;
						}
						final ElementData data= (ElementData) breakpoint.getTargetData(this.debugTarget);
						if (data != null && data.installed == current) {
							breakpoint.registerTarget(this.debugTarget, new ElementData(null));
						}
					}
					catch (final CoreException e) {} // only isRegistered
				}
			}
		}
		
		if (updated != null) {
			synchronized (this.stateUpdatesLock) {
				for (int i= 0; i < updated.size(); i++) {
					scheduleStateUpdate(updated.get(i));
				}
			}
		}
	}
	
	private void addBreakpoint(final Map<String, Element> map,
			final SrcfileData srcfile, final IResource resource, final String elementId,
			final IRLineBreakpoint lineBreakpoint, final RLineBreakpointValidator validator,
			final int modCounter) throws CoreException {
		synchronized (this.positionUpdatesLock) {
			if (this.positionModCounter.get() == modCounter) {
				this.positionUpdatesBreakpoints.remove(lineBreakpoint);
			}
		}
		
		Element elementPositions= map.get(elementId);
		if (elementPositions == null) {
			elementPositions= new Element(srcfile, resource, elementId,
					RDbg.createRJSrcref(validator.computeElementSrcref()) );
			map.put(elementId, elementPositions);
		}
		final IMarker marker= lineBreakpoint.getMarker();
		if (marker == null) {
			return;
		}
		int[] rExpressionIndex= validator.computeRExpressionIndex();
		if (rExpressionIndex == null) {
			rExpressionIndex= new int[0];
		}
		final int type;
		if (lineBreakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
			type= Tracepoint.TYPE_FB;
		}
		else if (lineBreakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
			type= Tracepoint.TYPE_LB;
		}
		else {
			return;
		}
		final Position position= new Position(type, marker.getId(), rExpressionIndex,
				lineBreakpoint );
		if (!elementPositions.getPositions().contains(position)) {
			if (elementPositions.getElementSrcref() != null) {
				final IRSrcref rExpressionSrcref= validator.computeRExpressionSrcref();
				if (rExpressionSrcref != null) {
					position.setExprSrcref(Srcref.diff(elementPositions.getElementSrcref(),
							RDbg.createRJSrcref(rExpressionSrcref) ));
				}
			}
			{	String label= validator.computeSubLabel();
				if (label == null) {
					label= validator.computeElementLabel();
				}
				position.setLabel(label);
			}
			elementPositions.getPositions().add(position);
		}
	}
	
	private void addClear(final Map<String, Element> map,
			final SrcfileData srcfile, final IResource resource, final String elementId)
			throws CoreException {
		Element elementPositions= map.get(elementId);
		if (elementPositions != null) {
			return;
		}
		elementPositions= new Element(srcfile, resource, elementId, null);
		map.put(elementId, elementPositions);
	}
	
	private void addElements(final List<Element> list, final Map<String, Element> map, final boolean delete) {
		final Collection<Element> values= map.values();
		for (final Element elementPositions : values) {
			if (elementPositions == null) {
				continue;
			}
			if (elementPositions.getPositions().size() > 0) {
				Collections.sort(elementPositions.getPositions());
			}
			else if (!delete) {
				continue;
			}
			list.add(elementPositions);
		}
	}
	
	
	@Override
	public void breakpointManagerEnablementChanged(final boolean enabled) {
		try {
			this.controller.exec(new DbgEnablement(enabled));
		}
		catch (final CoreException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when updating breakpoint enablement in the R engine.", e));
		}
	}
	
	@Override
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		boolean check= false;
		try {
			for (int i= 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IRBreakpoint) {
					final IRBreakpoint rBreakpoint= (IRBreakpoint) breakpoints[i];
					try {
						check= true;
						if (rBreakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
							final IRBreakpoint.ITargetData data;
							synchronized (this.flagUpdateLock) {
								data= this.exceptionBreakpointData;
								if (data != INSTALLED_DATA) {
									scheduleExceptionUpdate();
								}
							}
							if (data == INSTALLED_DATA) {
								rBreakpoint.registerTarget(this.debugTarget, data);
							}
						}
						else if (rBreakpoint instanceof IRLineBreakpoint
								&& isElementBreakpoint((IRLineBreakpoint) rBreakpoint) ) {
							synchronized (this.positionUpdatesLock) {
								this.positionModCounter.incrementAndGet();
								schedulePositionUpdate((IRLineBreakpoint) rBreakpoint);
							}
						}
					}
					catch (final Exception e) {
						RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
								"An error occurred when handling creation of an R breakpoint.", e ));
					}
				}
			}
		}
		finally {
			if (check) {
				checkUpdates();
			}
		}
	}
	
	@Override
	public void breakpointsRemoved(final IBreakpoint[] breakpoints, final IMarkerDelta[] deltas) {
		boolean check= false;
		try {
			for (int i= 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IRBreakpoint) {
					final IRBreakpoint rBreakpoint= (IRBreakpoint) breakpoints[i];
					try {
						check= true;
						if (rBreakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
							final IRBreakpoint.ITargetData data;
							synchronized (this.flagUpdateLock) {
								data= this.exceptionBreakpointData;
								if (data == INSTALLED_DATA) {
									scheduleExceptionUpdate();
								}
							}
						}
						else if (rBreakpoint instanceof IRLineBreakpoint) {
							final IMarkerDelta delta= deltas[i];
							final IMarker marker= rBreakpoint.getMarker();
							
							IResource resource= null;
							if (delta != null) {
								resource= delta.getResource();
								deactivateBreakpoint(resource, delta.getId());
							}
							else if (marker != null) {
								resource= marker.getResource();
								deactivateBreakpoint(resource, marker.getId());
							}
							
							String elementId= null;
							if (delta != null) {
								elementId= delta.getAttribute(RLineBreakpoint.ELEMENT_ID_MARKER_ATTR,
										null );
							}
							else if (marker != null) {
								elementId= marker.getAttribute(RLineBreakpoint.ELEMENT_ID_MARKER_ATTR,
										null );
							}
							
							synchronized (this.positionUpdatesLock) {
								this.positionModCounter.incrementAndGet();
								schedulePositionUpdate((IRLineBreakpoint) rBreakpoint);
								
								if (elementId != null) {
									this.positionUpdatesElements.add(new UpdateData(resource, elementId));
								}
							}
						}
					}
					catch (final Exception e) {
						RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
								"An error occurred when handling deletion of an R breakpoint.", e ));
					}
				}
			}
		}
		finally {
			if (check) {
				checkUpdates();
			}
		}
	}
	
	@Override
	public void breakpointsChanged(final IBreakpoint[] breakpoints, final IMarkerDelta[] deltas) {
		boolean check= false;
		try {
			for (int i= 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IRBreakpoint) {
					final IRBreakpoint rBreakpoint= (IRBreakpoint) breakpoints[i];
					try {
						final IMarkerDelta delta= deltas[i];
						if (delta == null) {
							continue;
						}
						
						check= true;
						final IMarker marker= rBreakpoint.getMarker();
						if (!marker.getResource().equals(delta.getResource())
								|| marker.getId() != delta.getId()) {
							deactivateBreakpoint(delta.getResource(), delta.getId());
						}
						
						synchronized (this.stateUpdatesLock) {
							scheduleStateUpdate(rBreakpoint);
						}
						
//						synchronized (fPendingPositionUpdatesLock) {
//							schedulePositionUpdate(lineBreakpoint);
//						}
					}
					catch (final Exception e) {
						RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
								"An error occurred when handling changes of an R breakpoint.", e ));
					}
				}
			}
		}
		finally {
			if (check) {
				checkUpdates();
			}
		}
	}
	
	
	protected void deactivateBreakpoint(final IResource resource, final long id) {
		if (resource == null) {
			return;
		}
		synchronized (this.stateUpdatesMap) {
			List<TracepointState> list= this.stateUpdatesMap.get(resource);
			if (list == null) {
				list= new ArrayList<>(8);
				this.stateUpdatesMap.put(resource, list);
			}
			String filePath;
			if (list.size() > 0) {
				for (int i= 0; i < list.size(); i++) {
					final TracepointState state= list.get(i);
					if (state.getId() == id) {
						if (state.getType() == TracepointState.TYPE_DELETED) {
							return;
						}
						list.remove(i);
					}
				}
				filePath= list.get(0).getFilePath();
			}
			else {
				filePath= resource.getFullPath().toPortableString();
			}
			list.add(new TracepointState(TracepointState.TYPE_DELETED, filePath, id));
		}
	}
	
	private List<TracepointState> getPendingTracepointStates() {
		// requires lock of stateUpdatesMap
		final ImList<IRBreakpoint> breakpoints;
		synchronized (this.stateUpdatesLock) {
			if (this.stateUpdatesBreakpoints.isEmpty() && this.stateUpdatesMap.isEmpty()) {
				return ImCollections.emptyList();
			}
			breakpoints= ImCollections.toList(this.stateUpdatesBreakpoints);
			this.stateUpdatesBreakpoints.clear();
		}
		
		ITER_BREAKPOINTS: for (final IRBreakpoint breakpoint : breakpoints) {
			try {
				final IMarker marker= breakpoint.getMarker();
				if (marker == null || !marker.exists()) {
					continue ITER_BREAKPOINTS;
				}
				
				final TracepointState newState;
				if (breakpoint instanceof IRExceptionBreakpoint) {
					newState= createState((IRExceptionBreakpoint) breakpoint, marker);
				}
				else if (breakpoint instanceof IRLineBreakpoint) {
					newState= createState((IRLineBreakpoint) breakpoint, marker);
				}
				else {
					newState= null;
				}
				if (newState == null) {
					continue ITER_BREAKPOINTS;
				}
				
				List<TracepointState> list= this.stateUpdatesMap.get(marker.getResource());
				if (list == null) {
					list= new ArrayList<>(8);
					this.stateUpdatesMap.put(marker.getResource(), list);
				}
				for (int i= 0; i < list.size(); i++) {
					final TracepointState state= list.get(i);
					if (state.getId() == newState.getId()
							&& state.getType() == Tracepoint.TYPE_DELETED) {
						continue ITER_BREAKPOINTS;
					}
				}
				list.add(newState);
			}
			catch (final CoreException e) {
				logPrepareError(e, breakpoint);
			}
		}
		
		final List<TracepointState> list= new ArrayList<>();
		for (final Iterator<List<TracepointState>> iter= this.stateUpdatesMap.values().iterator();
				iter.hasNext(); ) {
			final TracepointState[] states;
			final List<TracepointState> statesList= iter.next();
			states= statesList.toArray(new TracepointState[statesList.size()]);
			Arrays.sort(states);
			
			boolean delete= true;
			
			for (int i= 0; i < states.length; i++) {
				list.add(states[i]);
				if (states[i].getType() != Tracepoint.TYPE_DELETED) {
					delete= false;
				}
			}
			if (delete) {
				iter.remove();
			}
			else {
				statesList.clear();
			}
		}
		
		return list;
	}
	
	
	private void schedulePositionUpdate(final IRLineBreakpoint lineBreakpoint) {
		if (!contains(this.positionUpdatesBreakpoints, lineBreakpoint)) {
			this.positionUpdatesBreakpoints.add(lineBreakpoint);
		}
	}
	
	private void scheduleExceptionUpdate() {
		this.flagUpdateCheck= true;
	}
	
	private void scheduleStateUpdate(final IRBreakpoint lineBreakpoint) {
		if (!contains(this.stateUpdatesBreakpoints, lineBreakpoint)) {
			this.stateUpdatesBreakpoints.add(lineBreakpoint);
		}
	}
	
	private void checkUpdates() {
		if (!this.initialized) {
			return;
		}
		synchronized (this.stateUpdatesMap) {
			try {
				final List<TracepointState> states= getPendingTracepointStates();
				if (!states.isEmpty() && this.controller.getStatus() != ToolStatus.TERMINATED) {
					this.controller.exec(new TracepointStatesUpdate(states, false));
				}
			}
			catch (final CoreException e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
						"An error occurred when updating breakpoint states in the R engine.", e));
			}
		}
		
		boolean scheduleInstall= false;
		synchronized (this.positionUpdatesLock) {
			scheduleInstall|= (!this.positionUpdatesBreakpoints.isEmpty());
		}
		synchronized (this.flagUpdateLock) {
			scheduleInstall|= (this.flagUpdateCheck);
		}
		if (scheduleInstall) {
			synchronized (this.updateRunnable) {
				if (!this.updateRunnableScheduled) {
					this.updateRunnableScheduled= true;
					this.controller.getTool().getQueue().addHot(this.updateRunnable);
				}
			}
		}
	}
	
	
	private boolean contains(final List<? extends Object> list, final Object object) {
		for (int i= 0; i < list.size(); i++) {
			if (list.get(i) == object) {
				return true;
			}
		}
		return false;
	}
	
	private boolean contains(final Object[] array, final Object object) {
		for (int i= 0; i < array.length; i++) {
			if (array[i] == object) {
				return true;
			}
		}
		return false;
	}
	
	private int getLineNumber(final IMarker marker, final IMarkerPositionResolver resolver) {
		return (resolver != null) ?
				resolver.getLine(marker) :
				marker.getAttribute(IMarker.LINE_NUMBER, -1);
	}
	
	private boolean isScriptBreakpoint(final IRLineBreakpoint breakpoint) throws CoreException {
		return (breakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID
				&& breakpoint.getElementType() == IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE );
	}
	
	private boolean isElementBreakpoint(final IRLineBreakpoint breakpoint) throws CoreException {
		final int elementType;
		return ((breakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID
					|| breakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID )
				&& ((elementType= breakpoint.getElementType()) == IRLineBreakpoint.R_COMMON_FUNCTION_ELEMENT_TYPE
					|| elementType == IRLineBreakpoint.R_S4_METHOD_ELEMENT_TYPE ));
	}
	
	private TracepointState createState(final IRLineBreakpoint breakpoint,
			final IMarker marker) throws CoreException {
		final int type;
		int flags= breakpoint.isEnabled() ? TracepointState.FLAG_ENABLED : 0;
		if (breakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
			type= Tracepoint.TYPE_LB;
		}
		else if (breakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
			type= Tracepoint.TYPE_FB;
			final IRMethodBreakpoint methodBreakpoint= (IRMethodBreakpoint) breakpoint;
			if (methodBreakpoint.isEntry()) {
				flags |= TracepointState.FLAG_MB_ENTRY;
			}
			if (methodBreakpoint.isExit()) {
				flags |= TracepointState.FLAG_MB_EXIT;
			}
		}
		else {
			return null;
		}
		final String expr= (breakpoint.isConditionEnabled()) ?
				breakpoint.getConditionExpr() : null;
		
		final ElementData breakpointData= (ElementData) breakpoint.getTargetData(this.debugTarget);
		final Element installed= (breakpointData != null) ? breakpointData.installed : null;
		final ModelPosition modelPosition= RLineBreakpointValidator.getModelPosition(breakpoint);
		
		String elementId= null;
		String elementLabel= null;
		int[] index= null;
		if (installed != null) {
			elementId= installed.getElementId();
			for (final Position position : installed.getPositions()) {
				if (position.getBreakpoint() == breakpoint) {
					elementLabel= position.getElementLabel();
					break;
				}
			}
		}
		if (modelPosition != null) {
			if (elementId != null) {
				if (elementId.equals(modelPosition.getElementId())) {
					index= modelPosition.getRExpressionIndex();
				}
			}
			else {
				elementId= modelPosition.getElementId();
				index= modelPosition.getRExpressionIndex();
			}
		}
		if (elementLabel == null) {
			elementLabel= breakpoint.getSubLabel();
			if (elementLabel == null) {
				elementLabel= breakpoint.getElementLabel();
			}
		}
		
		if (elementId == null) {
			return null;
		}
		if (index == null) {
			index= new int[] { -1 };
		}
		
		return new TracepointState(type,
				marker.getResource().getFullPath().toPortableString(), marker.getId(),
				elementId, index, elementLabel, flags, expr);
	}
	
	private TracepointState createState(final IRExceptionBreakpoint breakpoint,
			final IMarker marker) throws CoreException {
		final int type;
		final int flags= breakpoint.isEnabled() ? TracepointState.FLAG_ENABLED : 0;
		if (breakpoint.getBreakpointType() == RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID) {
			type= Tracepoint.TYPE_EB;
		}
		else {
			return null;
		}
		final String expr= null;
		
		final String exceptionId= breakpoint.getExceptionId();
		final String elementLabel= null;
		
		return new TracepointState(type, TracepointState.EB_FILEPATH, marker.getId(),
				exceptionId, elementLabel, flags, expr);
	}
	
	
	@Override
	public Object toEclipseData(final TracepointEvent event) {
		try {
			if (event == null) {
				return null;
			}
			IRBreakpoint breakpoint= null;
			if (event.getFilePath() != null) {
				final IWorkspace workspace= ResourcesPlugin.getWorkspace();
				final IResource resource= (event.getType() == Tracepoint.TYPE_EB) ?
						workspace.getRoot() :
						workspace.getRoot().getFile(new Path(event.getFilePath()));
				final IMarker marker= resource.getMarker(event.getId());
				final IBreakpoint b= this.breakpointManager.getBreakpoint(marker);
				if (b instanceof IRBreakpoint) {
					breakpoint= (IRBreakpoint) b;
				}
			}
			String label= event.getLabel();
			if (event.getType() == Tracepoint.TYPE_EB) {
				if (label == null || label.equals("*")) {
					label= "error";
				}
			}
			else if (label == null && breakpoint instanceof IRLineBreakpoint) {
				final IRLineBreakpoint lineBreakpoint= (IRLineBreakpoint) breakpoint;
				final ElementData breakpointData= (ElementData) breakpoint.getTargetData(this.debugTarget);
				if (breakpointData != null && breakpointData.installed != null) {
					for (final Position position : breakpointData.installed.getPositions()) {
						if (position.getBreakpoint() == breakpoint) {
							label= position.getElementLabel();
							break;
						}
					}
				}
				if (label == null) {
					try {
						label= lineBreakpoint.getSubLabel();
						if (label == null) {
							label= lineBreakpoint.getElementLabel();
						}
					}
					catch (final CoreException e) {}
				}
			}
			switch (event.getType()) {
			case Tracepoint.TYPE_FB:
				return new MethodBreakpointStatus(event, label, breakpoint);
			case Tracepoint.TYPE_EB:
				return new ExceptionBreakpointStatus(event, label, breakpoint);
			default:
				return new BreakpointStatus(event, label, breakpoint);
			}
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when creating breakpoint status.", e ));
			return null;
		}
	}
	
	
	/** Call in R thread */
	public void dispose() {
		if (this.breakpointManager != null) {
			if (DebugPlugin.getDefault() != null) {
				this.breakpointManager.removeBreakpointManagerListener(this);
				this.breakpointManager.removeBreakpointListener(this);
				
				final IBreakpoint[] breakpoints= this.breakpointManager.getBreakpoints(
						RDebugModel.IDENTIFIER );
				for (int i= 0; i < breakpoints.length; i++) {
					if (breakpoints[i] instanceof IRBreakpoint) {
						final IRBreakpoint breakpoint= (IRBreakpoint) breakpoints[i];
						breakpoint.unregisterTarget(this.debugTarget);
					}
				}
			}
			this.breakpointManager= null;
		}
		synchronized (this.stateUpdatesLock) {
			this.stateUpdatesBreakpoints.clear();
		}
		synchronized (this.stateUpdatesMap) {
			this.stateUpdatesMap.clear();
		}
		synchronized (this.positionUpdatesLock) {
			this.positionUpdatesBreakpoints.clear();
			this.positionUpdatesElements.clear();
		}
	}
	
}
