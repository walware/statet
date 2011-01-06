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
import org.eclipse.core.variables.IStringVariable;

import de.walware.ecommons.ConstList;

import de.walware.statet.nico.core.NicoVariables;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
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
	
	
	public static interface ICombinedList extends RList, ICombinedRElement {
		
	}
	
	public static interface ICombinedEnvironment extends REnvironment, ICombinedList {
		
	}
	
	public static final List<IStringVariable> ADDITIONAL_R_VARIABLES = new ConstList<IStringVariable>(
			NicoVariables.SESSION_STARTUP_DATE_VARIABLE,
			NicoVariables.SESSION_STARTUP_TIME_VARIABLE,
			NicoVariables.SESSION_CONNECTION_DATE_VARIABLE,
			NicoVariables.SESSION_CONNECTION_TIME_VARIABLE,
			NicoVariables.SESSION_STARTUP_WD_VARIABLE );
	
	
	private static class REnvironments {
		
		
		private final RWorkspace fWorkspace;
		private final List<REnvironmentVar> fPreviousSearchEnvs;
		private final Map<Long, REnvironmentVar> fPreviousEnvMap;
		
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
		
		
		private void updateREnvironments(final IRCombinedDataAdapter adapter, final Set<RElementName> envirs, final boolean force, final IProgressMonitor monitor) throws CoreException {
			fForce = (force || fPreviousSearchEnvs == EMPTY_LIST || fPreviousEnvMap == EMPTY_MAP);
//			long start = System.currentTimeMillis();
			createSearchEnvs(adapter, monitor);
			final ArrayIntList updateList = createUpdateIdxs(envirs);
			createUpdateEnvs(adapter, monitor);
			createEnvMap(adapter, monitor);
//			long end = System.currentTimeMillis();
//			System.out.println("----\ncomplete: " + (end-start) + "\n");
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
		
		private ArrayIntList createUpdateIdxs(final Set<RElementName> envirs) {
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
//						long start = System.currentTimeMillis();
						final RObject robject = r.evalCombinedStruct(elementName, 0, -1, monitor);
//						long end = System.currentTimeMillis();
//						System.out.println("update " + elementName.getDisplayName() + ": " + (end-start));
//						System.out.println(robject);
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
		
		private void createEnvMap(final IRCombinedDataAdapter adapter, final IProgressMonitor monitor) throws CoreException {
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
		
		private void search(final ICombinedList list, final boolean reuse, final IRCombinedDataAdapter adapter, final IProgressMonitor monitor) throws CoreException {
			if (list.hasModelChildren(null)) {
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
							search((ICombinedList) object, reuse, adapter, monitor);
							continue ITER_CHILDREN;
						default:
							continue ITER_CHILDREN;
						}
					}
				}
			}
		}
		
		private void resolveEnv(final RReferenceVar ref, final boolean reuse, final IRCombinedDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			final Long handle = Long.valueOf(ref.getHandle());
			ref.setResolver(fWorkspace);
			if (fEnvMap.containsKey(handle)) {
				return;
			}
			if (reuse) {
				final REnvironmentVar renv = fPreviousEnvMap.get(handle);
				if (renv != null) {
					fEnvMap.put(handle, renv);
					search(renv, reuse, r, monitor);
				}
				return;
			}
			try {
				final RObject robject = r.evalCombinedStruct(ref, 0, -1, null, monitor);
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
				if (r.getProcess().isTerminated() || monitor.isCanceled()) {
					throw e;
				}
			}
		}
		
	}
	
	
	private boolean fRObjectDBEnabled;
	private List<? extends ICombinedEnvironment> fRSearchEnvsPublic = Collections.emptyList();
	private List<REnvironmentVar> fRSearchEnvsInternal;
	private Map<Long, REnvironmentVar> fREnvMap = Collections.emptyMap();
	private boolean fAutoRefreshDirty;
	
	
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
		fRObjectDBEnabled = enable;
		if (enable) {
			final AbstractRController controller = (AbstractRController) getProcess().getController();
			controller.fChanged = REFRESH_COMPLETE;
		}
		else {
			fRSearchEnvsInternal = null;
			fRSearchEnvsPublic = Collections.emptyList();
			fREnvMap = Collections.emptyMap();
		}
	}
	
	public boolean hasRObjectDB() {
		return (fRObjectDBEnabled);
	}
	
	public boolean isROBjectDBDirty() {
		return fAutoRefreshDirty;
	}
	
	@Override
	protected final void autoRefreshFromTool(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller = (AbstractRController) adapter.getController();
		if (controller.fChanged != 0 || !controller.fChangedEnvirs.isEmpty()) {
			refreshFromTool(controller, controller.fChanged, monitor);
		}
	}
	
	@Override
	protected final void refreshFromTool(int options, final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller = (AbstractRController) adapter.getController();
		if ((options & (REFRESH_AUTO | REFRESH_COMPLETE)) != 0) {
			options |= controller.fChanged;
		}
		refreshFromTool(controller, options, monitor);
	}
	
	protected void refreshFromTool(final AbstractRController controller, final int options, final IProgressMonitor monitor) throws CoreException {
		if (controller.isBusy() || getCurrentPrompt().text.startsWith("Browse")) {
			return;
		}
		monitor.subTask("Update Workspace Data");
		if (controller.getProcess().isProvidingFeatureSet(RTool.R_DATA_FEATURESET_ID)) {
			final IRDataAdapter r = (IRDataAdapter) controller;
			updateWorkspaceDir(r, monitor);
			updateOptions(r, monitor);
			if (fRObjectDBEnabled) {
				if ( ((options & REFRESH_COMPLETE) != 0)
						|| ( ((((options & REFRESH_AUTO)) != 0) || !controller.fChangedEnvirs.isEmpty())
								&& isAutoRefreshEnabled() ) ) {
					updateREnvironments(r, controller.fChangedEnvirs, ((options & REFRESH_COMPLETE) != 0), monitor);
					controller.fChanged = 0;
					controller.fChangedEnvirs.clear();
				}
			}
			else {
				controller.fChanged = 0;
				controller.fChangedEnvirs.clear();
			}
		}
		else {
			controller.fChanged = 0;
			controller.fChangedEnvirs.clear();
		}
		
		final boolean dirty = !isAutoRefreshEnabled()
				&& (controller.fChanged != 0 || !controller.fChangedEnvirs.isEmpty());
		if (dirty != fAutoRefreshDirty) {
			fAutoRefreshDirty = dirty;
			addPropertyChanged("RObjectDB.dirty", dirty);
		}
	}
	
	private void updateWorkspaceDir(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		final RObject rWd = r.evalData("getwd()", monitor); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rWd)) {
			final String wd = rWd.getData().getChar(0);
			if (!isRemote()) {
				controlSetWorkspaceDir(EFS.getLocalFileSystem().getStore(new Path(wd)));
			}
			else {
				controlSetRemoteWorkspaceDir(new Path(wd));
			}
		}
	}
	
	private void updateOptions(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		final IRSetupAdapter rsetup = ((IRSetupAdapter) r);
		final RList rOptions = (RList) r.evalData("options(\"prompt\", \"continue\")", monitor); //$NON-NLS-1$
		final RObject rPrompt = rOptions.get("prompt"); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rPrompt)) {
			if (!rPrompt.getData().isNA(0)) {
				rsetup.setDefaultPromptText(rPrompt.getData().getChar(0));
			}
		}
		final RObject rContinue = rOptions.get("continue"); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rContinue)) {
			if (!rContinue.getData().isNA(0)) {
				rsetup.setContinuePromptText(rContinue.getData().getChar(0));
			}
		}
	}
	
	private void updateREnvironments(final IRDataAdapter r, final Set<RElementName> envirs, final boolean force, final IProgressMonitor monitor) throws CoreException {
//		final long time = System.nanoTime();
		if (!(r instanceof IRCombinedDataAdapter)) {
			return;
		}
		final REnvironments newEnvs = new REnvironments(this);
		newEnvs.updateREnvironments((IRCombinedDataAdapter) r, envirs, force, monitor);
		if (monitor.isCanceled()) {
			return;
		}
		fRSearchEnvsInternal = newEnvs.fSearchEnvs;
		fREnvMap = newEnvs.fEnvMap;
		
		fRSearchEnvsPublic = Collections.unmodifiableList(fRSearchEnvsInternal);
		addPropertyChanged("REnvironments", newEnvs.fUpdateEnvs);
//		System.out.println("RSearch Update: " + (System.nanoTime() - time));
//		int count = 0;
//		for (final REnvironmentVar env : fREnvMap.values()) {
//			final int l = env.getLength();
//			if (l > 0) {
//				count += l;
//			}
//		}
//		System.out.println("count: " + count);
	}
	
	@Override
	protected void dispose() {
		super.dispose();
		fRSearchEnvsInternal = null;
		fRSearchEnvsPublic = EMPTY_LIST;
		fREnvMap = EMPTY_MAP;
	}
	
}
