/*******************************************************************************
 * Copyright (c) 2006-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.console.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariable;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.nico.core.NicoVariables;
import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.data.RVector;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.console.core.RConsoleCorePlugin;
import de.walware.statet.r.internal.rdata.CombinedFactory;
import de.walware.statet.r.internal.rdata.REnvironmentVar;
import de.walware.statet.r.internal.rdata.RReferenceVar;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.ICombinedRDataAdapter;
import de.walware.statet.r.nico.RWorkspaceConfig;


/**
 * R Tool Workspace
 */
public class RWorkspace extends ToolWorkspace {
	
	
	public static final int REFRESH_AUTO =                  0x01;
	public static final int REFRESH_COMPLETE =              0x02;
	public static final int REFRESH_PACKAGES =              0x10;
	
	private static final Set<Long> NO_ENVS_SET = Collections.emptySet();
	
	
	public static interface ICombinedRList extends RList, ICombinedRElement {
		
	}
	
	public static interface ICombinedREnvironment extends REnvironment, ICombinedRList {
		
	}
	
	public static final List<IStringVariable> ADDITIONAL_R_VARIABLES = new ConstList<IStringVariable>(
			NicoVariables.SESSION_STARTUP_DATE_VARIABLE,
			NicoVariables.SESSION_STARTUP_TIME_VARIABLE,
			NicoVariables.SESSION_CONNECTION_DATE_VARIABLE,
			NicoVariables.SESSION_CONNECTION_TIME_VARIABLE,
			NicoVariables.SESSION_STARTUP_WD_VARIABLE );
	
	
	private static class RObjectDB {
		
		
		private final RWorkspace fWorkspace;
		
		private final Map<Long, REnvironmentVar> fEnvsMap;
		
		private int fSearchEnvsStamp;
		private List<REnvironmentVar> fSearchEnvs;
		private List<? extends ICombinedREnvironment> fSearchEnvsPublic;
		
		private int fLazyEnvsStamp;
		private Set<Long> fLazyEnvs;
		
		private RObjectDB fPreviousDB;
		private boolean fCacheMode;
		
		
		public RObjectDB(final RWorkspace workspace, final int stamp) {
			fWorkspace = workspace;
			fSearchEnvsStamp = stamp;
			fSearchEnvsPublic = Collections.emptyList();
			fEnvsMap = new ConcurrentHashMap<Long, REnvironmentVar>();
			fLazyEnvs = NO_ENVS_SET;
		}
		
		
		public void updateLazyEnvs(final AbstractRController r, final IProgressMonitor monitor) {
			if (fEnvsMap != null && !fEnvsMap.isEmpty() && !fLazyEnvs.isEmpty()) {
				fEnvsMap.keySet().removeAll(fLazyEnvs);
			}
			fLazyEnvsStamp = r.getCounter();
			final Set<Long> list = r.getLazyEnvironments(monitor);
			fLazyEnvs = (list != null && !list.isEmpty()) ? list : NO_ENVS_SET;
		}
		
		public void updateSearchEnvs(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			fSearchEnvsStamp = r.getController().getCounter();
			fSearchEnvs = new ArrayList<REnvironmentVar>();
			fSearchEnvsPublic = Collections.unmodifiableList(fSearchEnvs);
			final RVector<RCharacterStore> searchObj = (RVector<RCharacterStore>) r.evalData("search()", monitor);
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
		
		public ArrayIntList createUpdateIdxs(final Set<RElementName> envirs, final RObjectDB previous,
				final boolean force) {
			final ArrayIntList updateIdxs = new ArrayIntList(fSearchEnvs.size());
			if (force) {
				for (int newIdx = 0; newIdx < fSearchEnvs.size(); newIdx++) {
					updateIdxs.add(newIdx);
				}
			}
			else {
				// reuse environments until we found a new or any none-package item
				for (int newIdx = fSearchEnvs.size()-1, oldIdx = previous.fSearchEnvs.size(); newIdx >= 0; newIdx--) {
					final REnvironmentVar current = fSearchEnvs.get(newIdx);
					if (current.getSpecialType() > 0 && current.getSpecialType() <= REnvironment.ENVTYPE_PACKAGE) {
						final int j = previous.fSearchEnvs.indexOf(current);
						if (j >= 0 && j < oldIdx) {
							oldIdx = j;
							if (envirs != null && envirs.contains(current.getElementName())) {
								updateIdxs.add(newIdx);
							}
							else {
								fSearchEnvs.set(newIdx, previous.fSearchEnvs.get(oldIdx));
							}
							continue;
						}
					}
					updateIdxs.add(newIdx);
					for (newIdx--; newIdx >= 0; newIdx--) {
						updateIdxs.add(newIdx);
					}
				}
			}
			return updateIdxs;
		}
		
		private List<REnvironmentVar> createUpdateEnvs(final ArrayIntList updateIdxs,
				final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			final ArrayList<REnvironmentVar> updateEnvs = new ArrayList<REnvironmentVar>(updateIdxs.size());
			for (int idx = 0; idx < updateIdxs.size(); idx++) {
				if (monitor.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
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
						final RObject robject = r.evalCombinedStruct(elementName, RObjectFactory.F_LOAD_PROMISE, -1, monitor);
//						long end = System.currentTimeMillis();
//						System.out.println("update " + elementName.getDisplayName() + ": " + (end-start));
//						System.out.println(robject);
						if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
							final REnvironmentVar renv = (REnvironmentVar) robject;
							renv.setStamp(r.getController().getCounter());
							fSearchEnvs.set(idx, renv);
							updateEnvs.add(renv);
							continue;
						}
					}
					catch (final CoreException e) {
						RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
								-1, "Error update environment "+elementName, e ));
						if (r.getTool().isTerminated() || monitor.isCanceled()) {
							throw e;
						}
					}
					envir.setError("update error");
					updateEnvs.add(envir);
					
//					final RObject test = tools.evalStruct("yy", monitor);
//					System.out.println(test);
				}
			}
			return updateEnvs;
		}
		
		private void updateEnvMap(final List<REnvironmentVar> updateEnvs, final RObjectDB previous,
				final ICombinedRDataAdapter adapter, final IProgressMonitor monitor) throws CoreException {
			if (monitor.isCanceled()) {
				return;
			}
			fPreviousDB = null;
			fCacheMode = true;
			
			for (final REnvironmentVar envir : fSearchEnvs) {
				final Long handle = envir.getHandle();
				fEnvsMap.put(handle, envir);
			}
			
			for (final REnvironmentVar envir : updateEnvs) {
				check(envir, adapter, monitor);
			}
			fPreviousDB = previous;
			for (final REnvironmentVar envir : fSearchEnvs) {
				if (!updateEnvs.contains(envir)) {
					check(envir, adapter, monitor);
				}
			}
			fPreviousDB = null;
			return;
		}
		
		public REnvironmentVar resolveEnv(final RReferenceVar ref, final boolean cacheMode,
				final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			fPreviousDB = null;
			fCacheMode = cacheMode;
			return resolveEnv(ref, r, monitor);
		}
		
		private REnvironmentVar resolveEnv(final RReferenceVar ref,
				final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			ref.setResolver(fWorkspace);
			final Long handle = Long.valueOf(ref.getHandle());
			boolean cacheMode = fCacheMode;
			{	final REnvironmentVar renv = fEnvsMap.get(handle);
				if (renv != null) {
					if (fCacheMode || renv.getStamp() == r.getController().getCounter()) {
						return renv;
					}
					// we are about to replace an environment because of wrong stamp
					// to be save, resolve all (for object browser), but correct stamp
					// can be loaded later (like this request) 
					cacheMode = true;
				}
			}
			final boolean lazy = fLazyEnvs.contains(handle);
			if (!lazy && fPreviousDB != null) {
				final REnvironmentVar renv = fPreviousDB.fEnvsMap.get(handle);
				if (renv != null
						&& (fCacheMode || renv.getStamp() == r.getController().getCounter())) {
					fEnvsMap.put(handle, renv);
					check(renv, r, monitor);
					return renv;
				}
			}
			if (cacheMode != fCacheMode) {
				fCacheMode = cacheMode;
				cacheMode = !cacheMode;
			}
			try {
				final RObject robject = r.evalCombinedStruct(ref,
						lazy ? 0 : RObjectFactory.F_LOAD_PROMISE, -1, null, monitor);
				if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
					final REnvironmentVar renv = (REnvironmentVar) robject;
					if (renv.getHandle() == handle.longValue()) {
						renv.setStamp(r.getController().getCounter());
						fEnvsMap.put(handle, renv);
						check(renv, r, monitor);
						return renv;
					}
				}
				return null;
			}
			catch (final CoreException e) {
				RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, -1,
						"Error update environment ref "+ref.getElementName(), e ));
				if (r.getTool().isTerminated() || monitor.isCanceled()) {
					throw e;
				}
				return null;
			}
			finally {
				fCacheMode = cacheMode;
			}
		}
		
		private void checkDirectAccessName(final REnvironmentVar var, final RElementName name) {
			if (name == null || name.getNextSegment() != null) {
				return;
			}
			switch (name.getType()) {
			case RElementName.MAIN_SEARCH_ENV:
			case RElementName.MAIN_PACKAGE:
			case RElementName.MAIN_SYSFRAME:
			case RElementName.MAIN_PROJECT:
				CombinedFactory.INSTANCE.setElementName(var, name);
				return;
			default:
				return;
			}
		}
		
		private void check(final ICombinedRList list,
				final ICombinedRDataAdapter adapter, final IProgressMonitor monitor) throws CoreException {
			if (list.hasModelChildren(null)) {
				final int length = list.getLength();
				ITER_CHILDREN : for (int i = 0; i < length; i++) {
					final RObject object = list.get(i);
					if (object != null) {
						switch (object.getRObjectType()) {
						case RObject.TYPE_REFERENCE:
							if (fCacheMode && object.getRClassName().equals("environment")) {
								resolveEnv((RReferenceVar) object, adapter, monitor);
							}
							else {
								((RReferenceVar) object).setResolver(fWorkspace);
							}
							continue ITER_CHILDREN;
						case RObject.TYPE_LIST:
						case RObject.TYPE_S4OBJECT:
							check((ICombinedRList) object, adapter, monitor);
							continue ITER_CHILDREN;
						default:
							continue ITER_CHILDREN;
						}
					}
				}
			}
		}
		
	}
	
	
	private boolean fRObjectDBEnabled;
	private RObjectDB fRObjectDB;
	private boolean fAutoRefreshDirty;
	
	
	public RWorkspace(final AbstractRController controller, final String remoteHost,
			final RWorkspaceConfig config) {
		super(  controller,
				new Prompt("> ", IConsoleService.META_PROMPT_DEFAULT),  //$NON-NLS-1$
				"\n", 
				remoteHost);
		if (config != null) {
			fRObjectDBEnabled = config.getEnableObjectDB();
			setAutoRefresh(config.getEnableAutoRefresh());
		}
	}
	
	
	@Override
	public RProcess getProcess() {
		return (RProcess) super.getProcess();
	}
	
	public List<? extends ICombinedREnvironment> getRSearchEnvironments() {
		final RObjectDB db = fRObjectDB;
		return (db != null) ? db.fSearchEnvsPublic : null;
	}
	
	public ICombinedRElement resolve(final RReference reference, final boolean onlyUptodata) {
		final RObjectDB db = fRObjectDB;
		if (db != null) {
			final REnvironmentVar renv = db.fEnvsMap.get(reference.getHandle());
			if (renv != null) {
				if (!onlyUptodata) {
					return renv;
				}
				final ToolController controller = getProcess().getController();
				if (controller != null && controller.getCounter() == renv.getStamp()) {
					return renv;
				}
			}
		}
		return null;
	}
	
	public RReference createReference(final long handle, final RElementName name, final String className) {
		return new RReferenceVar(handle, className, name);
	}
	
	public ICombinedRElement resolve(final RReference reference, final IProgressMonitor monitor)
			throws CoreException {
		final AbstractRController controller = (AbstractRController) getProcess().getController();
		if (controller == null || !(controller instanceof ICombinedRDataAdapter)) {
			return null;
		}
//		System.out.println(controller.getCounter() + " resolve");
		RObjectDB db = fRObjectDB;
		final RReferenceVar ref;
		if (reference instanceof RReferenceVar) {
			ref = (RReferenceVar) reference;
			ICombinedRElement parent = ref.getModelParent();
			while (parent != null) {
				if (parent.getRObjectType() == RObject.TYPE_ENV) {
					final int stamp = ((REnvironmentVar) parent).getStamp();
					if (controller.getCounter() != stamp) { // unsafe
						return db.fEnvsMap.get(reference.getHandle());
					}
					else {
						break;
					}
				}
				parent = parent.getModelParent();
			}
		}
		else {
			return null;
		}
		if (db == null) {
			db = new RObjectDB(this, controller.getCounter()-1000);
			db.updateLazyEnvs(controller, monitor);
			fRObjectDB = db;
		}
		else if (db.fLazyEnvsStamp != controller.getCounter()) {
			db.updateLazyEnvs(controller, monitor);
		}
		final REnvironmentVar env = db.resolveEnv(ref, false, (ICombinedRDataAdapter) controller, monitor);
		if (env != null && ref.getElementName() != null) {
			db.checkDirectAccessName(env, ref.getElementName());
		}
		return env;
	}
	
	
	protected void enableRObjectDB(final boolean enable) {
		fRObjectDBEnabled = enable;
		if (enable) {
			final AbstractRController controller = (AbstractRController) getProcess().getController();
			controller.briefAboutChange(REFRESH_COMPLETE);
		}
		else {
			fRObjectDB = null;
		}
	}
	
	public boolean hasRObjectDB() {
		return (fRObjectDBEnabled);
	}
	
	public boolean isROBjectDBDirty() {
		return fAutoRefreshDirty;
	}
	
	@Override
	protected final void autoRefreshFromTool(final IConsoleService adapter, final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller = (AbstractRController) adapter.getController();
		if (controller.hasBriefedChanges() || controller.isSuspended()) {
			refreshFromTool(controller, controller.getBriefedChanges(), monitor);
		}
	}
	
	@Override
	protected final void refreshFromTool(int options, final IConsoleService adapter, final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller = (AbstractRController) adapter.getController();
		if ((options & (REFRESH_AUTO | REFRESH_COMPLETE)) != 0) {
			options |= controller.getBriefedChanges();
		}
		refreshFromTool(controller, options, monitor);
	}
	
	protected void refreshFromTool(final AbstractRController controller, final int options, final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Update Workspace Data");
		if (controller.getTool().isProvidingFeatureSet(RConsoleTool.R_DATA_FEATURESET_ID)) {
			final IRDataAdapter r = (IRDataAdapter) controller;
			updateWorkspaceDir(r, monitor);
			updateOptions(r, monitor);
			if (fRObjectDBEnabled) {
				final Set<RElementName> elements = controller.getBriefedChangedElements();
				if ( ((options & REFRESH_COMPLETE) != 0)
						|| ( ((((options & REFRESH_AUTO)) != 0) || !elements.isEmpty()
								|| controller.isSuspended() )
								&& isAutoRefreshEnabled() ) ) {
					updateREnvironments(r, elements, ((options & REFRESH_COMPLETE) != 0), monitor);
					controller.clearBriefedChanges();
				}
			}
			else {
				controller.clearBriefedChanges();
			}
		}
		else {
			controller.clearBriefedChanges();
		}
		
		final boolean dirty = !isAutoRefreshEnabled() && controller.hasBriefedChanges();
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
		final RList rOptions = (RList) r.evalData("options(\"prompt\", \"continue\")", monitor); //$NON-NLS-1$
		final RObject rPrompt = rOptions.get("prompt"); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rPrompt)) {
			if (!rPrompt.getData().isNA(0)) {
				((AbstractRController) r).setDefaultPromptTextL(rPrompt.getData().getChar(0));
			}
		}
		final RObject rContinue = rOptions.get("continue"); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rContinue)) {
			if (!rContinue.getData().isNA(0)) {
				((AbstractRController) r).setContinuePromptText(rContinue.getData().getChar(0));
			}
		}
	}
	
	private void updateREnvironments(final IRDataAdapter r, final Set<RElementName> envirs, boolean force, final IProgressMonitor monitor) throws CoreException {
//		final long time = System.nanoTime();
//		System.out.println(controller.getCounter() + " update");
		if (!(r instanceof ICombinedRDataAdapter)) {
			return;
		}
		
		final AbstractRController controller = (AbstractRController) r.getController();
		final RObjectDB previous = fRObjectDB;
		force = (force || previous == null || previous.fSearchEnvs == null);
		if (!force && previous.fSearchEnvsStamp == controller.getCounter()
				&& envirs.isEmpty()) {
			return;
		}
		final RObjectDB db = new RObjectDB(this, controller.getCounter());
		db.updateLazyEnvs(controller, monitor);
		db.updateSearchEnvs(r, monitor);
		final ArrayIntList updateList = db.createUpdateIdxs(envirs, previous, force);
		final List<REnvironmentVar> updateEnvs = db.createUpdateEnvs(updateList, (ICombinedRDataAdapter) r, monitor);
		db.updateEnvMap(updateEnvs, force ? null : previous, (ICombinedRDataAdapter) r, monitor);
		
		if (monitor.isCanceled()) {
			return;
		}
		fRObjectDB = db;
		addPropertyChanged("REnvironments", updateEnvs);
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
		fRObjectDB = null;
	}
	
}
