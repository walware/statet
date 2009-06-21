/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RVector;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.rdata.REnvironmentVar;
import de.walware.statet.r.internal.rdata.RReferenceVar;


/**
 * R Tool Workspace
 */
public class RWorkspace extends ToolWorkspace {
	
	
	public static final int REFRESH_AUTO =          0x1;
	public static final int REFRESH_COMPLETE =      0x2;
	
	
	public static interface ICombinedEnvironment extends REnvironment, ICombinedRElement {
		
	}
	
	
	private static class REnvironments {
		
		
		private RWorkspace fWorkspace;
		private List<REnvironmentVar> fPreviousSearchEnvs;
		private Map<Long, REnvironmentVar> fPreviousEnvMap;
		
		private boolean fForce;
		
		private List<REnvironmentVar> fSearchEnvs;
		private ArrayIntList fUpdateIdxs;
		private List<REnvironmentVar> fUpdateEnvs;
		private Map<Long, REnvironmentVar> fEnvMap;
		
		
		public REnvironments(final RWorkspace workspace) {
			fWorkspace = workspace;
			fPreviousSearchEnvs = workspace.fRSearchEnvsInternal;
			fPreviousEnvMap = workspace.fREnvMap;
		}
		
		
		private void updateREnvironments(final IRCombinedDataAdapter adapter, Set<RElementName> envirs, final boolean force, final IProgressMonitor monitor) throws CoreException {
			fForce = (force || fPreviousSearchEnvs == EMPTY_LIST || fPreviousEnvMap == EMPTY_MAP);
			createSearchEnvs(adapter, monitor);
			final ArrayIntList updateList = createUpdateIdxs(envirs);
			createUpdateEnvs(adapter, monitor);
			createEnvMap(adapter, monitor);
		}
		
		
		private void createSearchEnvs(final IRDataAdapter tools, final IProgressMonitor monitor) throws CoreException {
			fSearchEnvs = new ArrayList<REnvironmentVar>();
			final RVector<RCharacterStore> searchObj = (RVector<RCharacterStore>) tools.evalData("search()", monitor);
			if (monitor.isCanceled()) {
				return;
			}
			final RCharacterStore searchData = searchObj.getData();
			for (int i = 0; i < searchData.getLength(); i++) {
				if (searchData.isNA(i)) {
					continue;
				}
				final String name = searchData.getChar(i);
				fSearchEnvs.add(new REnvironmentVar(name, true));
			}
		}
		
		private ArrayIntList createUpdateIdxs(Set<RElementName> envirs) {
			fUpdateIdxs = new ArrayIntList(fSearchEnvs.size());
			if (fForce) {
				for (int newIdx = 0; newIdx < fSearchEnvs.size(); newIdx++) {
					fUpdateIdxs.add(newIdx);
				}
			}
			else {
				// reuse environments until we found a new or any none-package item
				for (int newIdx = fSearchEnvs.size()-1, oldIdx = fPreviousSearchEnvs.size(); newIdx >= 0; newIdx--) {
					final REnvironmentVar current = fSearchEnvs.get(newIdx);
					if (current.getSpecialType() > 0 && current.getSpecialType() <= REnvironment.ENVTYPE_PACKAGE) {
						final int j = fPreviousSearchEnvs.indexOf(current);
						if (j >= 0 && j < oldIdx) {
							oldIdx = j;
							if (envirs != null && envirs.contains(current.getElementName())) {
								fUpdateIdxs.add(newIdx);
							}
							else {
								fSearchEnvs.set(newIdx, fPreviousSearchEnvs.get(oldIdx));
							}
							continue;
						}
					}
					fUpdateIdxs.add(newIdx);
					for (newIdx--; newIdx >= 0; newIdx--) {
						fUpdateIdxs.add(newIdx);
					}
				}
			}
			return fUpdateIdxs;
		}
		
		private void createUpdateEnvs(final IRCombinedDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			fUpdateEnvs = new ArrayList<REnvironmentVar>(fUpdateIdxs.size());
			for (int idx = 0; idx < fUpdateIdxs.size(); idx++) {
				if (monitor.isCanceled()) {
					return;
				}
				// Debug code
//				if (item.getName().equals("methods")) {
				{	final REnvironmentVar envir = fSearchEnvs.get(idx);
//					final RVector<RCharacterStore> ls = (RVector<RCharacterStore>) tools.evalData("ls(name=\""+item.getId()+"\", all.names=TRUE)", monitor);
//					final RCharacterStore lsData = ls.getData();
//					for (int i = 0; i < lsData.getLength(); i++) {
//						final String elementName = lsData.getChar(i);
////						final String elementName = lsData.getChar(133);
//						System.out.println(item.getId() + " " + elementName);
//						final RObject element = tools.evalStruct("as.environment(\""+item.getId()+"\")$\""+elementName+"\"", monitor);
//						System.out.println(element);
//					}
					
					// Regular code
					final RElementName elementName = envir.getElementName();
					try {
						final RObject robject = r.evalCombinedStruct(elementName, 0, -1, monitor);
	//					System.out.println(robject);
						if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
							final REnvironmentVar renv = (REnvironmentVar) robject;
							fSearchEnvs.set(idx, renv);
							fUpdateEnvs.add(renv);
							continue;
						}
					}
					catch (final CoreException e) {
						RCorePlugin.logError(-1, "Error update environment "+elementName, e);
						if (r.getProcess().isTerminated() || monitor.isCanceled()) {
							throw e;
						}
					}
					envir.setError("update error");
					fUpdateEnvs.add(envir);
					
//					final RObject test = tools.evalStruct("yy", monitor);
//					System.out.println(test);
				}
			}
		}
		
		private void createEnvMap(final IRCombinedDataAdapter adapter, final IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return;
			}
			fEnvMap = new HashMap<Long, REnvironmentVar>(Math.max(
					fSearchEnvs.size()*6, (fPreviousEnvMap.size() > 0) ? fPreviousEnvMap.size() : 50));
			for (final REnvironmentVar envir : fSearchEnvs) {
				final Long handle = envir.getHandle();
				fEnvMap.put(handle, envir);
			}
			if (fForce) {
				for (final REnvironmentVar envir : fSearchEnvs) {
					search(envir, false, adapter, monitor);
				}
			}
			else {
				for (final REnvironmentVar envir : fUpdateEnvs) {
					search(envir, false, adapter, monitor);
				}
				for (final REnvironmentVar envir : fSearchEnvs) {
					if (!fUpdateEnvs.contains(envir)) {
						search(envir, true, adapter, monitor);
					}
				}
			}
			return;
		}
		
		private void search(final RList list, final boolean reuse, final IRCombinedDataAdapter adapter, final IProgressMonitor monitor) {
			final int length = list.getLength();
			ITER_CHILDREN : for (int i = 0; i < length; i++) {
				final RObject object = list.get(i);
				if (object != null) {
					switch (object.getRObjectType()) {
					case RObject.TYPE_REFERENCE:
						if (object.getRClassName().equals("environment")) {
							resolveEnv((RReferenceVar) object, reuse, adapter, monitor);
						}
						continue ITER_CHILDREN;
					case RObject.TYPE_LIST:
					case RObject.TYPE_S4OBJECT:
						search((RList) object, reuse, adapter, monitor);
						continue ITER_CHILDREN;
					default:
						continue ITER_CHILDREN;
					}
				}
			}
		}
		
		private void resolveEnv(final RReferenceVar ref, final boolean reuse, final IRCombinedDataAdapter adapter, final IProgressMonitor monitor) {
			final Long handle = Long.valueOf(ref.getHandle());
			ref.setResolver(fWorkspace);
			if (fEnvMap.containsKey(handle)) {
				return;
			}
			if (reuse) {
				final REnvironmentVar renv = fPreviousEnvMap.get(handle);
				if (renv != null) {
					fEnvMap.put(handle, renv);
					search(renv, reuse, adapter, monitor);
				}
				return;
			}
			try {
				final RObject robject = adapter.evalCombinedStruct(ref, 0, -1, null, monitor);
				if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
					final REnvironmentVar renv = (REnvironmentVar) robject;
					if (renv.getHandle() == handle.longValue()) {
						fEnvMap.put(handle, renv);
						return;
					}
				}
				// failed
			}
			catch (final CoreException e) {
				RCorePlugin.logError(-1, "Error update environment ref "+ref.getElementName(), e);
			}
		}
		
	}
	
	
	private boolean fRSearchEnabled;
	private List<? extends ICombinedEnvironment> fRSearchEnvsPublic = EMPTY_LIST;
	private List<REnvironmentVar> fRSearchEnvsInternal;
	private Map<Long, REnvironmentVar> fREnvMap = EMPTY_MAP;
	
	
	public RWorkspace(final AbstractRController controller) {
		this(controller, null);
	}
	
	public RWorkspace(final AbstractRController controller, final String remoteHost) {
		super(  controller,
				new Prompt("> ", IRBasicAdapter.META_PROMPT_DEFAULT),  //$NON-NLS-1$
				"\n", 
				remoteHost);
	}
	
	
	public List<? extends ICombinedEnvironment> getRSearchEnvironments() {
		return fRSearchEnvsPublic;
	}
	
	public ICombinedRElement resolve(final RReference reference) {
		return fREnvMap.get(reference.getHandle());
	}
	
	
	void enableRObjectDB(final boolean enable) {
		fRSearchEnabled = enable;
		if (enable) {
			final AbstractRController controller = (AbstractRController) getProcess().getController();
			controller.fChanged = REFRESH_COMPLETE;
		}
		else {
			fRSearchEnvsInternal = null;
			fRSearchEnvsPublic = Collections.EMPTY_LIST;
			fREnvMap = Collections.EMPTY_MAP;
		}
	}
	
	public boolean hasRObjectDB() {
		return (fRSearchEnabled);
	}
	
	@Override
	protected void autoRefreshFromTool(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller = (AbstractRController) adapter.getController();
		if (controller.fChanged != 0 || !controller.fChangedEnvirs.isEmpty()) {
			refreshFromTool(controller.fChanged, controller.fChangedEnvirs, adapter, monitor);
		}
		controller.fChanged = 0;
		controller.fChangedEnvirs.clear();
	}
	@Override
	protected void refreshFromTool(final int options, final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws CoreException {
		refreshFromTool(options, null, adapter, monitor);
	}
	protected void refreshFromTool(final int options, Set<RElementName> envirs,
			final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws CoreException {
		if (((AbstractRController) adapter.getController()).isBusy()) {
			return;
		}
		monitor.subTask("Update Workspace Data");
		if (adapter.getProcess().isProvidingFeatureSet(RTool.R_DATA_FEATURESET_ID)) {
			final IRDataAdapter r = (IRDataAdapter) adapter;
			updateWorkspaceDir(r, monitor);
			if (fRSearchEnabled) {
				updateREnvironments(r, envirs, ((options & REFRESH_COMPLETE) != 0), monitor);
			}
		}
		firePropertiesChanged();
	}
	
	private void updateWorkspaceDir(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		final RVector<RCharacterStore> robj = (RVector<RCharacterStore>) r.evalData("getwd()", monitor); //$NON-NLS-1$
		final String wd = robj.getData().getChar(0);
		if (!isRemote()) {
			controlSetWorkspaceDir(EFS.getLocalFileSystem().getStore(new Path(wd)));
		}
		else {
			controlSetRemoteWorkspaceDir(new Path(wd));
		}
	}
	
	private void updateREnvironments(final IRDataAdapter r, final Set<RElementName> envirs, final boolean force, final IProgressMonitor monitor) throws CoreException {
		if (!(r instanceof IRCombinedDataAdapter)) {
			return;
		}
		final REnvironments newEnvs = new REnvironments(this);
//		final long time = System.nanoTime();
		newEnvs.updateREnvironments((IRCombinedDataAdapter) r, envirs, force, monitor);
		if (monitor.isCanceled()) {
			return;
		}
//		System.out.println(System.nanoTime() - time);
		fRSearchEnvsInternal = newEnvs.fSearchEnvs;
		fREnvMap = newEnvs.fEnvMap;
		fRSearchEnvsPublic = Collections.unmodifiableList(fRSearchEnvsInternal);
		addPropertyChanged("REnvironments", newEnvs.fUpdateEnvs);
	}
	
	@Override
	protected void dispose() {
		super.dispose();
		fRSearchEnvsInternal = null;
		fRSearchEnvsPublic = EMPTY_LIST;
		fREnvMap = EMPTY_MAP;
	}
	
}
