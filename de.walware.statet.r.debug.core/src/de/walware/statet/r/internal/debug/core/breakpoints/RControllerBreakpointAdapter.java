/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.IWorkspaceSourceUnit;
import de.walware.ecommons.text.IMarkerPositionResolver;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.ToolStatus;

import de.walware.rj.data.REnvironment;
import de.walware.rj.server.dbg.DbgEnablement;
import de.walware.rj.server.dbg.ElementTracepointInstallationReport;
import de.walware.rj.server.dbg.ElementTracepointInstallationRequest;
import de.walware.rj.server.dbg.ElementTracepointPositions;
import de.walware.rj.server.dbg.SrcfileData;
import de.walware.rj.server.dbg.Srcref;
import de.walware.rj.server.dbg.Tracepoint;
import de.walware.rj.server.dbg.TracepointEvent;
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
			this.breakpoint = breakpoint;
		}
		
		void setExprSrcref(final int[] srcref) {
			this.exprSrcref = srcref;
		}
		
		public IRLineBreakpoint getBreakpoint() {
			return this.breakpoint;
		}
		
		void setLabel(final String label) {
			this.label = label;
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
			this.resource = resource;
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
			this.resource = resource;
			this.elementId = elementId;
		}
		
	}
	
	private static class BreakpointData implements IRBreakpoint.ITargetData {
		
		final Element installed;
		
		public BreakpointData(final Element installed) {
			this.installed = installed;
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
	
	
	private final IRDebugTarget fDebugTarget;
	private final AbstractRDbgController fController;
	
	private IBreakpointManager fBreakpointManager;
	
	private final AtomicInteger fPositionModCounter = new AtomicInteger();
	
	private boolean fInitialized;
	
	private final List<IRLineBreakpoint> fPositionUpdatesBreakpoints = new ArrayList<>();
	private final List<UpdateData> fPositionUpdatesElements = new ArrayList<>();
	private final Object fPositionUpdatesLock = fPositionUpdatesBreakpoints;
	
	private final List<IRLineBreakpoint> fStateUpdatesBreakpoints = new ArrayList<>();
	private final Map<IResource, List<TracepointState>> fStateUpdatesMap = new HashMap<>();
	private final Object fStateUpdatesLock = fStateUpdatesBreakpoints;
	
	private final IToolRunnable fUpdateRunnable = new ISystemRunnable() {
		
		private List<String> fKnownPackages = new ArrayList<>();
		
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
			return (tool == fController.getTool());
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
			if (!fInitialized) {
				final IBreakpoint[] breakpoints = fBreakpointManager.getBreakpoints(
						RDebugModel.IDENTIFIER );
				try {
					{	final List<Element> positions;
						synchronized (fPositionUpdatesLock) {
							for (int i = 0; i < breakpoints.length; i++) {
								if (breakpoints[i] instanceof IRLineBreakpoint) {
									schedulePositionUpdate((IRLineBreakpoint) breakpoints[i]);
								}
							}
							
							positions = getPendingElementPositions(monitor);
						}
						installElementTracepoints(new ElementTracepointInstallationRequest(
								(positions != null) ? positions : Collections.<Element>emptyList() ),
								monitor );
					}
					{	final List<TracepointState> states;
						synchronized (fStateUpdatesLock) {
							for (int i = 0; i < breakpoints.length; i++) {
								if (breakpoints[i] instanceof IRLineBreakpoint) {
									fStateUpdatesBreakpoints.add((IRLineBreakpoint) breakpoints[i]);
								}
							}
							
							states = getPendingTracepointStates();
						}
						fController.exec(new TracepointStatesUpdate(states, true), monitor);
					}
				}
				finally {
					fInitialized = true;
					checkUpdates();
				}
			}
			try {
				final List<? extends ICombinedREnvironment> environments = fController.getWorkspaceData().getRSearchEnvironments();
				if (environments != null) {
					final List<String> packages = new ArrayList<>(environments.size() - 1);
					List<String> newPackages = null;
					for (final ICombinedREnvironment environment : environments) {
						if (environment.getSpecialType() == REnvironment.ENVTYPE_PACKAGE) {
							final String pkgName = environment.getElementName().getSegmentName();
							packages.add(pkgName);
							if (fKnownPackages != null && !fKnownPackages.contains(pkgName)) {
								if (newPackages == null) {
									newPackages = new ArrayList<>(4);
								}
								newPackages.add(pkgName);
							}
						}
					}
					if (fKnownPackages == null) {
						newPackages = packages;
					}
					fKnownPackages = packages;
					
					if (newPackages != null) {
						final IBreakpoint[] breakpoints = fBreakpointManager.getBreakpoints(RDebugModel.IDENTIFIER);
						final Map<IProject, IRProject> rProjects = new HashMap<IProject, IRProject>();
						for (int i = 0; i < breakpoints.length; i++) {
							if (breakpoints[i] instanceof IRLineBreakpoint) {
								final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoints[i];
								final IMarker marker = lineBreakpoint.getMarker();
								if (marker == null) {
									continue;
								}
								final IProject project = marker.getResource().getProject();
								IRProject rProject = rProjects.get(project);
								if (rProject == null) {
									rProject = RProjects.getRProject(project);
									if (rProject == null) {
										continue; // ?
									}
									rProjects.put(project, rProject);
								}
								final String pkgName = rProject.getPackageName();
								if (newPackages.contains(pkgName)) {
									schedulePositionUpdate(lineBreakpoint);
								}
							}
						}
					}
				}
				
				while (true) {
					final List<Element> positions;
					synchronized (fPositionUpdatesLock) {
						positions = getPendingElementPositions(monitor);
					}
					if (positions == null) {
						break;
					}
					installElementTracepoints(new ElementTracepointInstallationRequest(positions),
							monitor );
				}
			}
			finally {
				fUpdateRunnableScheduled = false;
			}
		}
		
	};
	private boolean fUpdateRunnableScheduled;
	
	
	public RControllerBreakpointAdapter(final IRDebugTarget target,
			final AbstractRDbgController controller) {
		fDebugTarget = target;
		fController = controller;
		
		fBreakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		fBreakpointManager.addBreakpointManagerListener(this);
		fBreakpointManager.addBreakpointListener(this);
		
		breakpointManagerEnablementChanged(fBreakpointManager.isEnabled());
	}
	
	public void init() {
		final Queue queue = fController.getTool().getQueue();
		queue.addHot(fUpdateRunnable);
		queue.addOnIdle(fUpdateRunnable, 5500);
	}
	
	
	public boolean supportsBreakpoint(final IRBreakpoint breakpoint) {
		final String breakpointType = breakpoint.getBreakpointType();
		return (breakpointType.equals(RDebugModel.R_LINE_BREAKPOINT_TYPE_ID)
				|| breakpointType.equals(RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) );
	}
	
	/** Call in R thread */
	@Override
	public boolean matchScriptBreakpoint(final IRModelSrcref srcref,
			final IProgressMonitor monitor) {
		try {
			if (srcref instanceof IAdaptable) {
				final IMarker marker = (IMarker) ((IAdaptable) srcref).getAdapter(IMarker.class);
				final ISourceUnit su = srcref.getFile();
				if (marker != null && su instanceof IWorkspaceSourceUnit
						&& marker.getResource() == su.getResource() ) {
					final List<IRLineBreakpoint> breakpoints = RDebugModel.getRLineBreakpoints(
							(IFile) su.getResource() );
					if (breakpoints.isEmpty()) {
						return false;
					}
					final IMarkerPositionResolver resolver = ((IWorkspaceSourceUnit) su).getMarkerPositionResolver();
					synchronized ((resolver != null && resolver.getDocument() instanceof ISynchronizable) ? ((ISynchronizable) resolver.getDocument()).getLockObject() : new Object()) {
						final int lineNumber = getLineNumber(marker, resolver);
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
					}
				}
			}
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when looking for script breakpoints.", e));
		}
		return false;
	}
	
	/** Call in R thread */
	@Override
	public ElementTracepointInstallationRequest getElementTracepoints(final SrcfileData srcfile,
			final IRModelSrcref srcref,
			final IProgressMonitor monitor) {
		try {
			if (!(srcref.getFile() instanceof IRWorkspaceSourceUnit)) {
				return null;
			}
			final IRWorkspaceSourceUnit workspaceSu = (IRWorkspaceSourceUnit) srcref.getFile();
			final IResource resource = workspaceSu.getResource();
			if (!resource.exists() || resource.getType() != IResource.FILE) {
				return null;
			}
			List<? extends IRLangSourceElement> elements = srcref.getElements();
			if (elements.isEmpty()) {
				return null;
			}
			final int modCounter = fPositionModCounter.get();
			final List<IRLineBreakpoint> breakpoints = RDebugModel.getRLineBreakpoints(
					(IFile) resource );
			if (breakpoints.isEmpty()) {
				return null;
			}
			
			workspaceSu.connect(monitor);
			try {
				final AbstractDocument document = workspaceSu.getDocument(monitor);
				synchronized ((document instanceof ISynchronizable) ? ((ISynchronizable) document).getLockObject() : new Object()) {
					final IRModelInfo modelInfo = (IRModelInfo) workspaceSu.getModelInfo(
							RModel.TYPE_ID, IRModelManager.MODEL_FILE, monitor );
					if (elements.get(0).getSourceParent() != modelInfo.getSourceElement()) {
						final List<? extends IRLangSourceElement> orgElements = elements;
						elements = modelInfo.getSourceElement().getSourceChildren(new IModelElement.Filter() {
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
					final IMarkerPositionResolver resolver = ((IWorkspaceSourceUnit) workspaceSu)
							.getMarkerPositionResolver();
					final int[] lines = new int[elements.size()*2];
					for (int i = 0, j = 0; i < elements.size(); i++, j+=2) {
						final IRegion region = elements.get(i).getSourceRange();
						lines[j] = document.getLineOfOffset(region.getOffset()) + 1;
						lines[j+1] = document.getLineOfOffset(region.getOffset()+region.getLength()) + 1;
					}
					HashMap<String, Element> map = null;
					List<String> cleanup = null;
					for (final IRLineBreakpoint breakpoint : breakpoints) {
						try {
							if (isElementBreakpoint(breakpoint)) {
								final IMarker marker = breakpoint.getMarker();
								final int breakpointLineNumber = (resolver != null) ?
										resolver.getLine(breakpoint.getMarker()) :
										breakpoint.getLineNumber();
								for (int j = 0; j < lines.length; j+=2) {
									if (lines[j] <= breakpointLineNumber && lines[j+1] >= breakpointLineNumber) {
										final RLineBreakpointValidator validator = (resolver != null) ?
												new RLineBreakpointValidator(workspaceSu,
												breakpoint.getBreakpointType(),
												resolver.getPosition(marker).getOffset(),
												monitor ) :
												new RLineBreakpointValidator(workspaceSu,
														breakpoint, monitor );
										final String elementId;
										if (validator.getType() == breakpoint.getBreakpointType()
												&& (elementId = validator.computeElementId()) != null
												&& elements.contains(validator.getBaseElement()) ) {
											if (map == null) {
												map = new HashMap<String, Element>(elements.size());
											}
											final BreakpointData breakpointData = (BreakpointData) breakpoint.getTargetData(fDebugTarget);
											if (breakpointData != null && breakpointData.installed != null
													&& !elementId.equals(breakpointData.installed.getElementId()) ) {
												if (cleanup == null) {
													cleanup = new ArrayList<String>();
												}
												if (!cleanup.contains(breakpointData.installed.getElementId())) {
													cleanup.add(breakpointData.installed.getElementId());
												}
											}
											addBreakpoint(map, srcfile, resource, elementId,
													breakpoint, validator, modCounter );
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
							synchronized (fPositionUpdatesLock) {
								for (int i = 0; i < cleanup.size(); i++) {
									fPositionUpdatesElements.add(
											new UpdateData(resource, cleanup.get(i)));
								}
							}
						}
					}
					if (map != null) {
						final ArrayList<Element> list = new ArrayList<>(map.size());
						addElements(list, map, false);
						if (!list.isEmpty()) {
							return new ElementTracepointInstallationRequest(list);
						}
					}
				}
			}
			finally {
				workspaceSu.disconnect(monitor);
			}
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when looking for script list.", e));
		}
		return null;
	}
	
	@Override
	public ElementTracepointInstallationRequest prepareFileElementTracepoints(final SrcfileData srcfile,
			final IRSourceUnit su,
			final IProgressMonitor monitor) {
		try {
			if (!(su instanceof IRWorkspaceSourceUnit)) {
				return null;
			}
			final IRWorkspaceSourceUnit workspaceSu = (IRWorkspaceSourceUnit) su;
			final IResource resource = workspaceSu.getResource();
			if (!resource.exists() || resource.getType() != IResource.FILE) {
				return null;
			}
			final int modeCounter = fPositionModCounter.get();
			final List<IRLineBreakpoint> breakpoints = RDebugModel.getRLineBreakpoints(
					(IFile) resource );
			if (breakpoints.isEmpty()) {
				return null;
			}
			Map<String, Element> map = null;
			for (final IRLineBreakpoint breakpoint : breakpoints) {
				try {
					if (isElementBreakpoint(breakpoint)) {
						final RLineBreakpointValidator validator = new RLineBreakpointValidator(
								workspaceSu, breakpoint, monitor );
						final String elementId;
						if (validator.getType() == breakpoint.getBreakpointType()
								&& (elementId = validator.computeElementId()) != null ) {
//									|| (((elementId = validator.computeElementId()) != null) ?
//											!elementId.equals(breakpoint.getElementId()) :
//											null != breakpoint.getElementId() )) {
							if (map == null) {
								map = new HashMap<String, Element>(breakpoints.size());
							}
							addBreakpoint(map, srcfile, resource, elementId, breakpoint, validator,
									modeCounter );
						}
					}
				}
				catch (final CoreException e) {
					RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
							"An error occurred when checking breakpoint.", e));
				}
			}
			if (map != null) {
				final ArrayList<Element> list = new ArrayList<>(map.size());
				addElements(list, map, false);
				if (!list.isEmpty()) {
					return new ElementTracepointInstallationRequest(list);
				}
			}
		}
		catch (final Exception e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when looking for line breakpoints.", e));
		}
		return null;
	}
	
	@Override
	public void installElementTracepoints(final ElementTracepointInstallationRequest request,
			final IProgressMonitor monitor) {
		ElementTracepointInstallationReport report = null;
		try {
			report = fController.exec(request, monitor);
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
	public List<Element> getPendingElementPositions(final IProgressMonitor monitor) {
		final IRBreakpoint[] breakpointsToUpdate;
		final UpdateData[] elementsToUpdate;
		synchronized (fPositionUpdatesLock) {
			if (fPositionUpdatesBreakpoints.isEmpty() && fPositionUpdatesElements.isEmpty()) {
				return null;
			}
			breakpointsToUpdate = fPositionUpdatesBreakpoints.toArray(new IRBreakpoint[fPositionUpdatesBreakpoints.size()]);
			fPositionUpdatesBreakpoints.clear();
			elementsToUpdate = fPositionUpdatesElements.toArray(new UpdateData[fPositionUpdatesElements.size()]);
			fPositionUpdatesElements.clear();
		}
		final Map<IResource, Map<String, Element>> resourceMap = new HashMap<>();
		// by resources
		for (int i = 0; i < breakpointsToUpdate.length; i++) {
			if (breakpointsToUpdate[i] instanceof IRLineBreakpoint) {
				final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpointsToUpdate[i];
				try {
					final BreakpointData breakpointData = (BreakpointData) lineBreakpoint.getTargetData(fDebugTarget);
					if (breakpointData != null && breakpointData.installed != null) {
						Map<String, Element> map = resourceMap.get(breakpointData.installed.getResource());
						if (map == null) {
							map = new HashMap<String, Element>();
							resourceMap.put(breakpointData.installed.getResource(), map);
						}
						map.put(breakpointData.installed.getElementId(), null);
					}
					
					if (lineBreakpoint.isRegistered() && isElementBreakpoint(lineBreakpoint)) {
						final IResource resource = lineBreakpoint.getMarker().getResource();
						if (!resourceMap.containsKey(resource)) {
							resourceMap.put(resource, new HashMap<String, Element>());
						}
					}
					else if (breakpointData != null) {
						lineBreakpoint.unregisterTarget(fDebugTarget);
					}
				}
				catch (final CoreException e) {
					logPrepareError(e, lineBreakpoint);
				}
			}
		}
		for (int i = 0; i < elementsToUpdate.length; i++) {
			final UpdateData updateData = elementsToUpdate[i];
			Map<String, Element> map = resourceMap.get(updateData.resource);
			if (map == null) {
				map = new HashMap<>();
				resourceMap.put(updateData.resource, map);
			}
			map.put(updateData.elementId, null);
		}
		
		int n = 0;
		for (final Entry<IResource, Map<String, Element>> resourceEntry : resourceMap.entrySet()) {
			final IResource resource = resourceEntry.getKey();
			final Map<String, Element> map = resourceEntry.getValue();
			try {
				final SrcfileData srcfile = RDbg.createRJSrcfileData(resource);
				if (resource.exists() && resource.getType() == IResource.FILE) {
					final IRWorkspaceSourceUnit workspaceSu = (IRWorkspaceSourceUnit) LTK.getSourceUnitManager()
							.getSourceUnit(RModel.TYPE_ID, LTK.PERSISTENCE_CONTEXT, resource, true,
									monitor );
					final int modCounter = fPositionModCounter.get();
					final List<IRLineBreakpoint> breakpoints = RDebugModel.getRLineBreakpoints(
							(IFile) resource );
					for (final IRLineBreakpoint lineBreakpoint : breakpoints) {
						if (contains(breakpointsToUpdate, lineBreakpoint)) {
							try {
								if (lineBreakpoint.isEnabled() && isElementBreakpoint(lineBreakpoint)) {
									final RLineBreakpointValidator validator = new RLineBreakpointValidator(
											workspaceSu,
											lineBreakpoint.getBreakpointType(), lineBreakpoint.getCharStart(),
											monitor );
									final String elementId;
									if (validator.getType() == lineBreakpoint.getBreakpointType()
											&& (elementId = validator.computeElementId()) != null ) {
										addBreakpoint(map, srcfile, resource, elementId, lineBreakpoint, validator,
												modCounter );
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
									final RLineBreakpointValidator validator = new RLineBreakpointValidator(
											workspaceSu,
											lineBreakpoint.getBreakpointType(), lineBreakpoint.getCharStart(),
											monitor );
									final String elementId;
									if (validator.getType() == lineBreakpoint.getBreakpointType()
											&& (elementId = validator.computeElementId()) != null
											&& map.containsKey(elementId) ) {
										addBreakpoint(map, srcfile, resource, elementId, lineBreakpoint, validator,
												modCounter );
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
							addClear(map, srcfile, resource, elementEntry.getKey());
						}
					}
				}
				else {
					for (final String elementId : map.keySet()) {
						addClear(map, srcfile, resource, elementId);
					}
				}
			}
			catch (final CoreException e) {
				RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, -1,
						NLS.bind("An error occurred when preparing update of R line breakpoints in ''{0}''.",
								resource.getFullPath().toString() ), e ));
			}
			n += map.size();
		}
		
		final List<Element> list = new ArrayList<>(n);
		for (final Entry<IResource, Map<String, Element>> resourceEntry : resourceMap.entrySet()) {
			addElements(list, resourceEntry.getValue(), true);
		}
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}
	
	private void logPrepareError(final CoreException e, final IRLineBreakpoint breakpoint) {
		String fileName = null;
		if (breakpoint != null) {
			final IMarker marker = breakpoint.getMarker();
			if (marker != null) {
				final IResource resource = marker.getResource();
				if (resource != null) {
					fileName = resource.getFullPath().toString();
				}
			}
		}
		RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, -1,
				NLS.bind("An error occurred when preparing update of an R line breakpoint in ''{0}''.",
						(fileName != null) ? fileName : "<missing>" ), e ));
	}
	
	/** Call in R thread */
	private void report(final ElementTracepointInstallationRequest request,
			final ElementTracepointInstallationReport report) {
		if (request == null) {
			throw new NullPointerException();
		}
		final List<? extends ElementTracepointPositions> elements = request.getRequests();
		final int l = elements.size();
		int[] results = null;
		if (report != null && report.getInstallationResults().length == l) {
			results = report.getInstallationResults();
		}
		
		if (results == null) {
			return; // ?
		}
		
		ArrayList<Element> cleanup = null;
		List<IRLineBreakpoint> updated = null;
		
		for (int i = 0; i < l; i++) {
			if (results[i] == ElementTracepointInstallationReport.FOUND_UNCHANGED
					|| !(elements.get(i) instanceof Element)) {
				continue;
			}
			
			final Element current = (Element) elements.get(i);
			final boolean installed = (results[i] == ElementTracepointInstallationReport.FOUND_SET);
			for (final TracepointPosition position : current.getPositions()) {
				if (!(position instanceof Position)) {
					continue;
				}
				final IRLineBreakpoint lineBreakpoint = ((Position) position).getBreakpoint();
				try {
					if (lineBreakpoint == null || !lineBreakpoint.isRegistered()) {
						continue;
					}
					final IMarker marker = lineBreakpoint.getMarker();
					if (marker == null || marker.getId() != position.getId()) {
						continue;
					}
					final BreakpointData newData = new BreakpointData((installed) ? current : null);
					final BreakpointData oldData = (BreakpointData) lineBreakpoint.registerTarget(fDebugTarget, newData);
					if (oldData != null && oldData.installed != null
							&& oldData.installed.getElementId().equals(current.getElementId())) {
						if (cleanup == null) {
							cleanup = new ArrayList<>(l-i);
						}
						if (!contains(cleanup, oldData.installed)) {
							cleanup.add(oldData.installed);
						}
					}
					if (updated == null) {
						updated = new ArrayList<>(l-i);
						updated.add(lineBreakpoint);
					}
				}
				catch (final CoreException e) {} // only isRegistered
			}
		}
		
		if (cleanup != null) {
			for (int i = 0; i < cleanup.size(); i++) {
				final Element current = cleanup.get(i);
				for (final TracepointPosition position : current.getPositions()) {
					if (!(position instanceof Position)) {
						continue;
					}
					final IRBreakpoint breakpoint = ((Position) position).getBreakpoint();
					try {
						if (breakpoint == null || !breakpoint.isRegistered()) {
							continue;
						}
						final BreakpointData data = (BreakpointData) breakpoint.getTargetData(fDebugTarget);
						if (data != null && data.installed == current) {
							breakpoint.registerTarget(fDebugTarget, new BreakpointData(null));
						}
					}
					catch (final CoreException e) {} // only isRegistered
				}
			}
		}
		
		if (updated != null) {
			synchronized (fStateUpdatesLock) {
				for (int i = 0; i < updated.size(); i++) {
					scheduleStateUpdate(updated.get(i));
				}
			}
		}
	}
	
	private void addBreakpoint(final Map<String, Element> map,
			final SrcfileData srcfile, final IResource resource, final String elementId,
			final IRLineBreakpoint lineBreakpoint, final RLineBreakpointValidator validator,
			final int modCounter) throws CoreException {
		synchronized (fPositionUpdatesLock) {
			if (fPositionModCounter.get() == modCounter) {
				fPositionUpdatesBreakpoints.remove(lineBreakpoint);
			}
		}
		
		Element elementPositions = map.get(elementId);
		if (elementPositions == null) {
			elementPositions = new Element(srcfile, resource, elementId,
					RDbg.createRJSrcref(validator.computeElementSrcref()) );
			map.put(elementId, elementPositions);
		}
		final IMarker marker = lineBreakpoint.getMarker();
		if (marker == null) {
			return;
		}
		int[] rExpressionIndex = validator.computeRExpressionIndex();
		if (rExpressionIndex == null) {
			rExpressionIndex = new int[0];
		}
		final int type;
		if (lineBreakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
			type = Tracepoint.TYPE_FB;
		}
		else if (lineBreakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
			type = Tracepoint.TYPE_LB;
		}
		else {
			return;
		}
		final Position position = new Position(type, marker.getId(), rExpressionIndex,
				lineBreakpoint );
		if (!elementPositions.getPositions().contains(position)) {
			if (elementPositions.getElementSrcref() != null) {
				final IRSrcref rExpressionSrcref = validator.computeRExpressionSrcref();
				if (rExpressionSrcref != null) {
					position.setExprSrcref(Srcref.diff(elementPositions.getElementSrcref(),
							RDbg.createRJSrcref(rExpressionSrcref) ));
				}
			}
			{	String label = validator.computeSubLabel();
				if (label == null) {
					label = validator.computeElementLabel();
				}
				position.setLabel(label);
			}
			elementPositions.getPositions().add(position);
		}
	}
	
	private void addClear(final Map<String, Element> map,
			final SrcfileData srcfile, final IResource resource, final String elementId)
			throws CoreException {
		Element elementPositions = map.get(elementId);
		if (elementPositions != null) {
			return;
		}
		elementPositions = new Element(srcfile, resource, elementId, null);
		map.put(elementId, elementPositions);
	}
	
	private void addElements(final List<Element> list, final Map<String, Element> map, final boolean delete) {
		final Collection<Element> values = map.values();
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
			fController.exec(new DbgEnablement(enabled));
		}
		catch (final CoreException e) {
			RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
					"An error occurred when updating breakpoint enablement in the R engine.", e));
		}
	}
	
	@Override
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		boolean check = false;
		try {
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IRLineBreakpoint) {
					try {
						final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoints[i];
						
						if (isElementBreakpoint(lineBreakpoint)) {
							check = true;
							synchronized (fPositionUpdatesLock) {
								fPositionModCounter.incrementAndGet();
								schedulePositionUpdate(lineBreakpoint);
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
		boolean check = false;
		try {
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IRLineBreakpoint) {
					try {
						final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoints[i];
						final IMarkerDelta delta = deltas[i];
						final IMarker marker = lineBreakpoint.getMarker();
						check = true;
						
						IResource resource = null;
						if (delta != null) {
							resource = delta.getResource();
							deactivateBreakpoint(resource, delta.getId());
						}
						else if (marker != null) {
							resource = marker.getResource();
							deactivateBreakpoint(resource, marker.getId());
						}
						
						String elementId = null;
						if (delta != null) {
							elementId = delta.getAttribute(RLineBreakpoint.ELEMENT_ID_MARKER_ATTR,
									null );
						}
						else if (marker != null) {
							elementId = marker.getAttribute(RLineBreakpoint.ELEMENT_ID_MARKER_ATTR,
									null );
						}
						
						synchronized (fPositionUpdatesLock) {
							fPositionModCounter.incrementAndGet();
							schedulePositionUpdate(lineBreakpoint);
							
							if (elementId != null) {
								fPositionUpdatesElements.add(new UpdateData(resource, elementId));
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
		boolean check = false;
		try {
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof IRLineBreakpoint) {
					try {
						final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoints[i];
						final IMarkerDelta delta = deltas[i];
						if (delta == null) {
							continue;
						}
						
						check = true;
						final IMarker marker = lineBreakpoint.getMarker();
						if (!marker.getResource().equals(delta.getResource())
								|| marker.getId() != delta.getId()) {
							deactivateBreakpoint(delta.getResource(), delta.getId());
						}
						
						synchronized (fStateUpdatesLock) {
							scheduleStateUpdate(lineBreakpoint);
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
		synchronized (fStateUpdatesLock) {
			List<TracepointState> list = fStateUpdatesMap.get(resource);
			if (list == null) {
				list = new ArrayList<TracepointState>(8);
				fStateUpdatesMap.put(resource, list);
			}
			String filePath;
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					final TracepointState state = list.get(i);
					if (state.getId() == id) {
						if (state.getType() == TracepointState.TYPE_DELETED) {
							return;
						}
						list.remove(i);
					}
				}
				filePath = list.get(0).getFilePath();
			}
			else {
				filePath = resource.getFullPath().toPortableString();
			}
			list.add(new TracepointState(TracepointState.TYPE_DELETED, filePath, id));
		}
	}
	
	private List<TracepointState> getPendingTracepointStates() {
		ITER_BREAKPOINTS: for (final IRLineBreakpoint lineBreakpoint : fStateUpdatesBreakpoints) {
			try {
				final IMarker marker = lineBreakpoint.getMarker();
				if (marker == null || !marker.exists()) {
					continue ITER_BREAKPOINTS;
				}
				final TracepointState newState = createState(lineBreakpoint);
				if (newState == null) {
					continue ITER_BREAKPOINTS;
				}
				List<TracepointState> list = fStateUpdatesMap.get(marker.getResource());
				if (list == null) {
					list = new ArrayList<>(8);
					fStateUpdatesMap.put(marker.getResource(), list);
				}
				for (int i = 0; i < list.size(); i++) {
					final TracepointState state = list.get(i);
					if (state.getId() == newState.getId()
							&& state.getType() == Tracepoint.TYPE_DELETED) {
						continue ITER_BREAKPOINTS;
					}
				}
				list.add(newState);
			}
			catch (final CoreException e) {
				logPrepareError(e, lineBreakpoint);
			}
		}
		fStateUpdatesBreakpoints.clear();
		
		final List<TracepointState> list = new ArrayList<>();
		for (final Iterator<List<TracepointState>> iter = fStateUpdatesMap.values().iterator();
				iter.hasNext(); ) {
			final TracepointState[] states;
			final List<TracepointState> statesList = iter.next();
			states = statesList.toArray(new TracepointState[statesList.size()]);
			Arrays.sort(states);
			
			boolean delete = true;
			
			for (int i = 0; i < states.length; i++) {
				list.add(states[i]);
				if (states[i].getType() != Tracepoint.TYPE_DELETED) {
					delete = false;
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
	
	
	private void scheduleStateUpdate(final IRLineBreakpoint lineBreakpoint) {
		if (!contains(fStateUpdatesBreakpoints, lineBreakpoint)) {
			fStateUpdatesBreakpoints.add(lineBreakpoint);
		}
	}
	
	private void schedulePositionUpdate(final IRLineBreakpoint lineBreakpoint) {
		if (!contains(fPositionUpdatesBreakpoints, lineBreakpoint)) {
			fPositionUpdatesBreakpoints.add(lineBreakpoint);
		}
	}
	
	private void checkUpdates() {
		if (!fInitialized) {
			return;
		}
		synchronized (fStateUpdatesLock) {
			if (!fStateUpdatesMap.isEmpty() || !fStateUpdatesBreakpoints.isEmpty()) {
				try {
					final List<TracepointState> states = getPendingTracepointStates();
					if (!states.isEmpty() && fController.getStatus() != ToolStatus.TERMINATED) {
						fController.exec(new TracepointStatesUpdate(states, false));
					}
				}
				catch (final CoreException e) {
					RDebugCorePlugin.log(new Status(IStatus.ERROR, RDebugCorePlugin.PLUGIN_ID, 0,
							"An error occurred when updating breakpoint states in the R engine.", e));
				}
			}
		}
		synchronized (fPositionUpdatesLock) {
			if (!fUpdateRunnableScheduled && !fPositionUpdatesBreakpoints.isEmpty()) {
				fUpdateRunnableScheduled = true;
				fController.getTool().getQueue().addHot(fUpdateRunnable);
			}
		}
	}
	
	
	private boolean contains(final List<? extends Object> list, final Object object) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == object) {
				return true;
			}
		}
		return false;
	}
	
	private boolean contains(final Object[] array, final Object object) {
		for (int i = 0; i < array.length; i++) {
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
				&& ((elementType = breakpoint.getElementType()) == IRLineBreakpoint.R_COMMON_FUNCTION_ELEMENT_TYPE
					|| elementType == IRLineBreakpoint.R_S4_METHOD_ELEMENT_TYPE ));
	}
	
	private TracepointState createState(final IRLineBreakpoint lineBreakpoint) throws CoreException {
		final int type;
		int flags = lineBreakpoint.isEnabled() ? TracepointState.FLAG_ENABLED : 0;
		if (lineBreakpoint.getBreakpointType() == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
			type = Tracepoint.TYPE_LB;
		}
		else if (lineBreakpoint.getBreakpointType() == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
			type = Tracepoint.TYPE_FB;
			final IRMethodBreakpoint methodBreakpoint = (IRMethodBreakpoint) lineBreakpoint;
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
		final IMarker marker = lineBreakpoint.getMarker();
		if (marker == null) {
			return null;
		}
		final String expr = (lineBreakpoint.isConditionEnabled()) ?
				lineBreakpoint.getConditionExpr() : null;
		
		final BreakpointData breakpointData = (BreakpointData) lineBreakpoint.getTargetData(fDebugTarget);
		final Element installed = (breakpointData != null) ? breakpointData.installed : null;
		final ModelPosition modelPosition = RLineBreakpointValidator.getModelPosition(lineBreakpoint);
		
		String elementId = null;
		String elementLabel = null;
		int[] index = null;
		if (installed != null) {
			elementId = installed.getElementId();
			for (final Position position : installed.getPositions()) {
				if (position.getBreakpoint() == lineBreakpoint) {
					elementLabel = position.getElementLabel();
					break;
				}
			}
		}
		if (modelPosition != null) {
			if (elementId != null) {
				if (elementId.equals(modelPosition.getElementId())) {
					index = modelPosition.getRExpressionIndex();
				}
			}
			else {
				elementId = modelPosition.getElementId();
				index = modelPosition.getRExpressionIndex();
			}
		}
		if (elementLabel == null) {
			elementLabel = lineBreakpoint.getSubLabel();
			if (elementLabel == null) {
				elementLabel = lineBreakpoint.getElementLabel();
			}
		}
		
		if (elementId == null) {
			return null;
		}
		if (index == null) {
			index = new int[] { -1 };
		}
		
		return new TracepointState(type,
				marker.getResource().getFullPath().toPortableString(), marker.getId(),
				elementId, index, elementLabel, flags, expr);
	}
	
	
	@Override
	public Object toEclipseData(final TracepointEvent event) {
		if (event == null) {
			return null;
		}
		IRBreakpoint breakpoint = null;
		if (event.getFilePath() != null) {
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(event.getFilePath()));
			final IMarker marker = file.getMarker(event.getId());
			final IBreakpoint b = fBreakpointManager.getBreakpoint(marker);
			if (b instanceof IRBreakpoint) {
				breakpoint = (IRBreakpoint) b;
			}
		}
		String label = event.getLabel();
		if (label == null && breakpoint != null) {
			final BreakpointData breakpointData = (BreakpointData) breakpoint.getTargetData(fDebugTarget);
			if (breakpointData != null && breakpointData.installed != null) {
				for (final Position position : breakpointData.installed.getPositions()) {
					if (position.getBreakpoint() == breakpoint) {
						label = position.getElementLabel();
						break;
					}
				}
			}
			if (label == null && breakpoint instanceof IRLineBreakpoint) {
				final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoint;
				try {
					label = lineBreakpoint.getSubLabel();
					if (label == null) {
						label = lineBreakpoint.getElementLabel();
					}
				}
				catch (final CoreException e) {}
			}
		}
		return new BreakpointStatus(event, label, breakpoint);
	}
	
	
	/** Call in R thread */
	public void dispose() {
		if (fBreakpointManager != null) {
			if (DebugPlugin.getDefault() != null) {
				fBreakpointManager.removeBreakpointManagerListener(this);
				fBreakpointManager.removeBreakpointListener(this);
				
				final IBreakpoint[] breakpoints = fBreakpointManager.getBreakpoints(
						RDebugModel.IDENTIFIER );
				for (int i = 0; i < breakpoints.length; i++) {
					if (breakpoints[i] instanceof IRLineBreakpoint) {
						final IRLineBreakpoint lineBreakpoint = (IRLineBreakpoint) breakpoints[i];
						lineBreakpoint.unregisterTarget(fDebugTarget);
					}
				}
			}
			fBreakpointManager = null;
		}
		synchronized (fStateUpdatesLock) {
			fStateUpdatesBreakpoints.clear();
			fStateUpdatesMap.clear();
		}
		synchronized (fPositionUpdatesLock) {
			fPositionUpdatesBreakpoints.clear();
			fPositionUpdatesElements.clear();
		}
	}
	
}
