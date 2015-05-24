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

package de.walware.statet.r.console.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariable;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;

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
import de.walware.statet.r.internal.rdata.REnvironmentVar;
import de.walware.statet.r.internal.rdata.RReferenceVar;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.ICombinedRDataAdapter;
import de.walware.statet.r.nico.RWorkspaceConfig;


/**
 * R Tool Workspace
 */
public class RWorkspace extends ToolWorkspace {
	
	
	public static final int REFRESH_AUTO=                  0x01;
	public static final int REFRESH_COMPLETE=              0x02;
	public static final int REFRESH_PACKAGES=              0x10;
	
	private static final Set<Long> NO_ENVS_SET= Collections.emptySet();
	
	
	public static interface ICombinedRList extends RList, ICombinedRElement {
		
		
		@Override
		ICombinedRElement get(int idx);
		@Override
		ICombinedRElement get(long idx);
		@Override
		ICombinedRElement get(String name);
		
	}
	
	public static interface ICombinedREnvironment extends REnvironment, ICombinedRList {
		
		
		RProcess getSource();
		
	}
	
	public static final ImList<IStringVariable> ADDITIONAL_R_VARIABLES= ImCollections.<IStringVariable>newList(
			NicoVariables.SESSION_STARTUP_DATE_VARIABLE,
			NicoVariables.SESSION_STARTUP_TIME_VARIABLE,
			NicoVariables.SESSION_CONNECTION_DATE_VARIABLE,
			NicoVariables.SESSION_CONNECTION_TIME_VARIABLE,
			NicoVariables.SESSION_STARTUP_WD_VARIABLE );
	
	
	private static class RObjectDB {
		
		
		private final RWorkspace workspace;
		
		private final Map<Long, REnvironmentVar> envsMap;
		
		private int searchEnvsStamp;
		private List<REnvironmentVar> searchEnvs;
		private List<? extends ICombinedREnvironment> searchEnvsPublic;
		
		private int lazyEnvsStamp;
		private Set<Long> lazyEnvs;
		
		private RObjectDB previousDB;
		private boolean cacheMode;
		
		
		public RObjectDB(final RWorkspace workspace, final int stamp) {
			this.workspace= workspace;
			this.searchEnvsStamp= stamp;
			this.searchEnvsPublic= Collections.emptyList();
			this.envsMap= new ConcurrentHashMap<>();
			this.lazyEnvs= NO_ENVS_SET;
		}
		
		
		public void updateLazyEnvs(final AbstractRController r, final IProgressMonitor monitor) {
			if (this.envsMap != null && !this.envsMap.isEmpty() && !this.lazyEnvs.isEmpty()) {
				this.envsMap.keySet().removeAll(this.lazyEnvs);
			}
			this.lazyEnvsStamp= r.getCounter();
			final Set<Long> list= r.getLazyEnvironments(monitor);
			this.lazyEnvs= (list != null && !list.isEmpty()) ? list : NO_ENVS_SET;
		}
		
		public void updateSearchEnvs(final IRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			this.searchEnvsStamp= r.getController().getCounter();
			this.searchEnvs= new ArrayList<>();
			this.searchEnvsPublic= Collections.unmodifiableList(this.searchEnvs);
			final RVector<RCharacterStore> searchObj= (RVector<RCharacterStore>) r.evalData("search()", monitor);
			if (monitor.isCanceled()) {
				return;
			}
			final RCharacterStore searchData= searchObj.getData();
			for (int i= 0; i < searchData.getLength(); i++) {
				if (searchData.isNA(i)) {
					continue;
				}
				final String name= searchData.getChar(i);
				this.searchEnvs.add(new REnvironmentVar(name, true, null, null));
			}
		}
		
		public ArrayIntList createUpdateIdxs(final Set<RElementName> envirs, final RObjectDB previous,
				final boolean force) {
			final ArrayIntList updateIdxs= new ArrayIntList(this.searchEnvs.size());
			if (force) {
				for (int newIdx= 0; newIdx < this.searchEnvs.size(); newIdx++) {
					updateIdxs.add(newIdx);
				}
			}
			else {
				// reuse environments until we found a new or any none-package item
				for (int newIdx= this.searchEnvs.size()-1, oldIdx= previous.searchEnvs.size(); newIdx >= 0; newIdx--) {
					final REnvironmentVar current= this.searchEnvs.get(newIdx);
					if (current.getSpecialType() > 0 && current.getSpecialType() <= REnvironment.ENVTYPE_PACKAGE) {
						final int j= previous.searchEnvs.indexOf(current);
						if (j >= 0 && j < oldIdx) {
							oldIdx= j;
							if (envirs != null && envirs.contains(current.getElementName())) {
								updateIdxs.add(newIdx);
							}
							else {
								this.searchEnvs.set(newIdx, previous.searchEnvs.get(oldIdx));
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
			final ArrayList<REnvironmentVar> updateEnvs= new ArrayList<>(updateIdxs.size());
			for (int idx= 0; idx < updateIdxs.size(); idx++) {
				if (monitor.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				// Debug code
//				if (item.getName().equals("methods")) {
				{	final REnvironmentVar envir= this.searchEnvs.get(idx);
//					final RVector<RCharacterStore> ls= (RVector<RCharacterStore>) tools.evalData("ls(name=\""+item.getId()+"\", all.names=TRUE)", monitor);
//					final RCharacterStore lsData= ls.getData();
//					for (int i= 0; i < lsData.getLength(); i++) {
//						final String elementName= lsData.getChar(i);
////						final String elementName= lsData.getChar(133);
//						System.out.println(item.getId() + " " + elementName);
//						final RObject element= tools.evalStruct("as.environment(\""+item.getId()+"\")$\""+elementName+"\"", monitor);
//						System.out.println(element);
//					}
					
					// Regular code
					final RElementName elementName= envir.getElementName();
					try {
//						long start= System.currentTimeMillis();
						final RObject robject= r.evalCombinedStruct(elementName, RObjectFactory.F_LOAD_PROMISE, -1, monitor);
//						long end= System.currentTimeMillis();
//						System.out.println("update " + elementName.getDisplayName() + ": " + (end-start));
//						System.out.println(robject);
						if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
							final REnvironmentVar renv= (REnvironmentVar) robject;
							renv.setSource(r.getTool(), r.getController().getCounter());
							this.searchEnvs.set(idx, renv);
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
					
//					final RObject test= tools.evalStruct("yy", monitor);
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
			this.previousDB= null;
			this.cacheMode= true;
			
			for (final REnvironmentVar envir : this.searchEnvs) {
				final Long handle= envir.getHandle();
				this.envsMap.put(handle, envir);
			}
			
			for (final REnvironmentVar envir : updateEnvs) {
				check(envir, adapter, monitor);
			}
			this.previousDB= previous;
			for (final REnvironmentVar envir : this.searchEnvs) {
				if (!updateEnvs.contains(envir)) {
					check(envir, adapter, monitor);
				}
			}
			this.previousDB= null;
			return;
		}
		
		public REnvironmentVar resolveEnv(final RReferenceVar ref, final boolean cacheMode,
				final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			this.previousDB= null;
			this.cacheMode= cacheMode;
			return resolveEnv(ref, r, monitor);
		}
		
		private REnvironmentVar resolveEnv(final RReferenceVar ref,
				final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
			ref.setResolver(this.workspace);
			final Long handle= Long.valueOf(ref.getHandle());
			boolean cacheMode= this.cacheMode;
			{	final REnvironmentVar renv= this.envsMap.get(handle);
				if (renv != null) {
					if (this.cacheMode || renv.getStamp() == r.getController().getCounter()) {
						return renv;
					}
					// we are about to replace an environment because of wrong stamp
					// to be save, resolve all (for object browser), but correct stamp
					// can be loaded later (like this request) 
					cacheMode= true;
				}
			}
			final boolean lazy= this.lazyEnvs.contains(handle);
			if (!lazy && this.previousDB != null) {
				final REnvironmentVar renv= this.previousDB.envsMap.get(handle);
				if (renv != null
						&& (this.cacheMode || renv.getStamp() == r.getController().getCounter())) {
					this.envsMap.put(handle, renv);
					check(renv, r, monitor);
					return renv;
				}
			}
			if (cacheMode != this.cacheMode) {
				this.cacheMode= cacheMode;
				cacheMode= !cacheMode;
			}
			try {
				final RObject robject= r.evalCombinedStruct(ref,
						lazy ? 0 : RObjectFactory.F_LOAD_PROMISE, -1, null, monitor);
				if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
					final REnvironmentVar renv= (REnvironmentVar) robject;
					if (renv.getHandle() == handle.longValue()) {
						renv.setSource(r.getTool(), r.getController().getCounter());
						this.envsMap.put(handle, renv);
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
				this.cacheMode= cacheMode;
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
				var.setElementName(name);
				return;
			default:
				return;
			}
		}
		
		private void check(final ICombinedRList list,
				final ICombinedRDataAdapter adapter, final IProgressMonitor monitor) throws CoreException {
			if (list.hasModelChildren(null)) {
				final long length= list.getLength();
				if (length <= Integer.MAX_VALUE) {
					final int l= (int) length;
					ITER_CHILDREN : for (int i= 0; i < l; i++) {
						final RObject object= list.get(i);
						if (object != null) {
							switch (object.getRObjectType()) {
							case RObject.TYPE_REFERENCE:
								if (this.cacheMode && object.getRClassName().equals("environment")) {
									resolveEnv((RReferenceVar) object, adapter, monitor);
								}
								else {
									((RReferenceVar) object).setResolver(this.workspace);
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
				else {
					ITER_CHILDREN : for (long i= 0; i < length; i++) {
						final RObject object= list.get(i);
						if (object != null) {
							switch (object.getRObjectType()) {
							case RObject.TYPE_REFERENCE:
								if (this.cacheMode && object.getRClassName().equals("environment")) {
									resolveEnv((RReferenceVar) object, adapter, monitor);
								}
								else {
									((RReferenceVar) object).setResolver(this.workspace);
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
		
	}
	
	
	private boolean rObjectDBEnabled;
	private RObjectDB rObjectDB;
	private boolean autoRefreshDirty;
	
	
	public RWorkspace(final AbstractRController controller, final String remoteHost,
			final RWorkspaceConfig config) {
		super(  controller,
				new Prompt("> ", IConsoleService.META_PROMPT_DEFAULT),  //$NON-NLS-1$
				"\n", (char) 0,
				remoteHost);
		if (config != null) {
			this.rObjectDBEnabled= config.getEnableObjectDB();
			setAutoRefresh(config.getEnableAutoRefresh());
		}
	}
	
	
	@Override
	public RProcess getProcess() {
		return (RProcess) super.getProcess();
	}
	
	
	@Override
	public IFileStore toFileStore(final IPath toolPath) throws CoreException {
		if (!toolPath.isAbsolute() && toolPath.getDevice() == null && "~".equals(toolPath.segment(0))) { //$NON-NLS-1$
			final ToolController controller= getProcess().getController();
			if (controller != null) {
				final IPath homePath= createToolPath(controller.getProperty("R:file.~")); //$NON-NLS-1$
				if (homePath != null) {
					return super.toFileStore(homePath.append(toolPath.removeFirstSegments(1)));
				}
			}
			return null;
		}
		return super.toFileStore(toolPath);
	}
	
	@Override
	public IFileStore toFileStore(final String toolPath) throws CoreException {
		if (toolPath.startsWith("~/")) { //$NON-NLS-1$
			return toFileStore(createToolPath(toolPath));
		}
		return super.toFileStore(toolPath);
	}
	
	
	public List<? extends ICombinedREnvironment> getRSearchEnvironments() {
		final RObjectDB db= this.rObjectDB;
		return (db != null) ? db.searchEnvsPublic : null;
	}
	
	public ICombinedRElement resolve(final RReference reference, final boolean onlyUptodata) {
		final RObjectDB db= this.rObjectDB;
		if (db != null) {
			final REnvironmentVar renv= db.envsMap.get(reference.getHandle());
			if (renv != null) {
				if (!onlyUptodata) {
					return renv;
				}
				final ToolController controller= getProcess().getController();
				if (controller != null && controller.getCounter() == renv.getStamp()) {
					return renv;
				}
			}
		}
		return null;
	}
	
	public RReference createReference(final long handle, final RElementName name, final String className) {
		return new RReferenceVar(handle, className, null, name);
	}
	
	public ICombinedRElement resolve(final RReference reference,
			final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller= (AbstractRController) getProcess().getController();
		if (controller == null || !(controller instanceof ICombinedRDataAdapter)) {
			return null;
		}
//		System.out.println(controller.getCounter() + " resolve");
		RObjectDB db= this.rObjectDB;
		final RReferenceVar ref;
		if (reference instanceof RReferenceVar) {
			ref= (RReferenceVar) reference;
			ICombinedRElement parent= ref.getModelParent();
			while (parent != null) {
				if (parent.getRObjectType() == RObject.TYPE_ENV) {
					final REnvironmentVar env= (REnvironmentVar) parent;
					if (controller.getCounter() != env.getStamp()
							|| controller.getTool() != env.getSource()) { // unsafe
						return db.envsMap.get(reference.getHandle());
					}
					else {
						break;
					}
				}
				parent= parent.getModelParent();
			}
		}
		else {
			return null;
		}
		if (db == null) {
			db= new RObjectDB(this, controller.getCounter() - 1000);
			db.updateLazyEnvs(controller, monitor);
			this.rObjectDB= db;
		}
		else if (db.lazyEnvsStamp != controller.getCounter()) {
			db.updateLazyEnvs(controller, monitor);
		}
		final REnvironmentVar env= db.resolveEnv(ref, false, (ICombinedRDataAdapter) controller, monitor);
		if (env != null && ref.getElementName() != null) {
			db.checkDirectAccessName(env, ref.getElementName());
		}
		return env;
	}
	
	
	protected void enableRObjectDB(final boolean enable) {
		this.rObjectDBEnabled= enable;
		if (enable) {
			final AbstractRController controller= (AbstractRController) getProcess().getController();
			controller.briefAboutChange(REFRESH_COMPLETE);
		}
		else {
			this.rObjectDB= null;
		}
	}
	
	public boolean hasRObjectDB() {
		return (this.rObjectDBEnabled);
	}
	
	public boolean isROBjectDBDirty() {
		return this.autoRefreshDirty;
	}
	
	@Override
	protected final void autoRefreshFromTool(final IConsoleService adapter,
			final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller= (AbstractRController) adapter.getController();
		if (controller.hasBriefedChanges() || controller.isSuspended()) {
			refreshFromTool(controller, controller.getBriefedChanges(), monitor);
		}
	}
	
	@Override
	protected final void refreshFromTool(int options, final IConsoleService adapter,
			final IProgressMonitor monitor) throws CoreException {
		final AbstractRController controller= (AbstractRController) adapter.getController();
		if ((options & (REFRESH_AUTO | REFRESH_COMPLETE)) != 0) {
			options |= controller.getBriefedChanges();
		}
		refreshFromTool(controller, options, monitor);
	}
	
	protected void refreshFromTool(final AbstractRController controller, final int options,
			final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Update Workspace Data");
		if (controller.getTool().isProvidingFeatureSet(RConsoleTool.R_DATA_FEATURESET_ID)) {
			final IRDataAdapter r= (IRDataAdapter) controller;
			updateWorkspaceDir(r, monitor);
			updateOptions(r, monitor);
			if (this.rObjectDBEnabled) {
				final Set<RElementName> elements= controller.getBriefedChangedElements();
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
		
		final boolean dirty= !isAutoRefreshEnabled() && controller.hasBriefedChanges();
		if (dirty != this.autoRefreshDirty) {
			this.autoRefreshDirty= dirty;
			addPropertyChanged("RObjectDB.dirty", dirty);
		}
	}
	
	private void updateWorkspaceDir(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		final RObject rWd= r.evalData("getwd()", monitor); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rWd)) {
			final String wd= rWd.getData().getChar(0);
			if (!isRemote()) {
				controlSetWorkspaceDir(EFS.getLocalFileSystem().getStore(createToolPath(wd)));
			}
			else {
				controlSetRemoteWorkspaceDir(createToolPath(wd));
			}
		}
	}
	
	private void updateOptions(final IRDataAdapter r,
			final IProgressMonitor monitor) throws CoreException {
		final RList rOptions= (RList) r.evalData("options(\"prompt\", \"continue\")", monitor); //$NON-NLS-1$
		final RObject rPrompt= rOptions.get("prompt"); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rPrompt)) {
			if (!rPrompt.getData().isNA(0)) {
				((AbstractRController) r).setDefaultPromptTextL(rPrompt.getData().getChar(0));
			}
		}
		final RObject rContinue= rOptions.get("continue"); //$NON-NLS-1$
		if (RDataUtil.isSingleString(rContinue)) {
			if (!rContinue.getData().isNA(0)) {
				((AbstractRController) r).setContinuePromptText(rContinue.getData().getChar(0));
			}
		}
	}
	
	private void updateREnvironments(final IRDataAdapter r, final Set<RElementName> envirs, boolean force,
			final IProgressMonitor monitor) throws CoreException {
//		final long time= System.nanoTime();
//		System.out.println(controller.getCounter() + " update");
		if (!(r instanceof ICombinedRDataAdapter)) {
			return;
		}
		
		final AbstractRController controller= (AbstractRController) r.getController();
		final RObjectDB previous= this.rObjectDB;
		force|= (previous == null || previous.searchEnvs == null);
		if (!force && previous.searchEnvsStamp == controller.getCounter() && envirs.isEmpty()) {
			return;
		}
		final RObjectDB db= new RObjectDB(this, controller.getCounter());
		db.updateLazyEnvs(controller, monitor);
		db.updateSearchEnvs(r, monitor);
		final ArrayIntList updateList= db.createUpdateIdxs(envirs, previous, force);
		final List<REnvironmentVar> updateEnvs= db.createUpdateEnvs(updateList, (ICombinedRDataAdapter) r, monitor);
		db.updateEnvMap(updateEnvs, force ? null : previous, (ICombinedRDataAdapter) r, monitor);
		
		if (monitor.isCanceled()) {
			return;
		}
		this.rObjectDB= db;
		addPropertyChanged("REnvironments", updateEnvs);
//		System.out.println("RSearch Update: " + (System.nanoTime() - time));
//		int count= 0;
//		for (final REnvironmentVar env : fREnvMap.values()) {
//			final int l= env.getLength();
//			if (l > 0) {
//				count += l;
//			}
//		}
//		System.out.println("count: " + count);
	}
	
	@Override
	protected void dispose() {
		super.dispose();
		this.rObjectDB= null;
	}
	
}
