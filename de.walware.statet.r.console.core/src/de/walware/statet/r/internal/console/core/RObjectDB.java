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

package de.walware.statet.r.internal.console.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.jcommons.collections.ImCollections;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.services.RService;

import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.console.core.RWorkspace.ICombinedRList;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.rdata.REnvironmentVar;
import de.walware.statet.r.internal.rdata.RReferenceVar;
import de.walware.statet.r.internal.rdata.VirtualMissingVar;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.ICombinedRDataAdapter;


public class RObjectDB {
	
	
	private static final Set<Long> NO_ENVS_SET= Collections.emptySet();
	
	
	private static class NamespaceEntry {
		
		private volatile VirtualMissingVar na;
		
		private REnvironmentVar namespaceEnv;
		
		private REnvironmentVar namespaceExports;
		
		private REnvironmentVar packageEnv;
		
	}
	
	
	private static boolean isNamespaceEnv(final RElementName elementName) {
		return (elementName != null && elementName.getType() == RElementName.SCOPE_NS_INT
				&& elementName.getNextSegment() == null );
	}
	
	private static ICombinedRElement first(final ICombinedRElement first, final ICombinedRElement second) {
		return (first != null) ? first : second;
	}
	
	
	private final RWorkspace workspace;
	
	private final ConcurrentHashMap<Long, REnvironmentVar> envsMap= new ConcurrentHashMap<>();
	
	private int searchEnvsStamp;
	private List<REnvironmentVar> searchEnvs;
	private List<? extends ICombinedREnvironment> searchEnvsPublic;
	
	private int lazyEnvsStamp;
	private Set<Long> lazyEnvs;
	
	private final ConcurrentHashMap<String, RObjectDB.NamespaceEntry> namespaceMap= new ConcurrentHashMap<>();
	private List<String> forceUpdatePkgNames;
	
	private RObjectDB previousDB;
	private boolean cacheMode;
	
	private ICombinedRDataAdapter r;
	
	
	public RObjectDB(final RWorkspace workspace, final int stamp,
			final AbstractRController r, final IProgressMonitor monitor) {
		this.workspace= r.getWorkspaceData();
		
		this.searchEnvsStamp= stamp;
		this.searchEnvsPublic= Collections.emptyList();
		this.lazyEnvs= NO_ENVS_SET;
		
		updateLazyEnvs(r, monitor);
	}
	
	
	public List<? extends ICombinedREnvironment> getSearchEnvs() {
		return this.searchEnvsPublic;
	}
	
	public int getSearchEnvsStamp() {
		return this.searchEnvsStamp;
	}
	
	public REnvironmentVar getEnv(final Long handle) {
		return this.envsMap.get(handle);
	}
	
	public ICombinedRElement getNamespaceEnv(final String name) {
		if (name != null) {
			final RObjectDB.NamespaceEntry entry= this.namespaceMap.get(name);
			if (entry != null) {
				return first(entry.na, entry.namespaceEnv);
			}
		}
		return null;
	}
	
	public ICombinedRElement getNamespacePub(final String name) {
		if (name != null) {
			final RObjectDB.NamespaceEntry entry= this.namespaceMap.get(name);
			if (entry != null) {
				return first(entry.na, entry.namespaceExports);
			}
		}
		return null;
	}
	
	public ICombinedRElement getPackageEnv(final String name) {
		if (name != null) {
			final RObjectDB.NamespaceEntry entry= this.namespaceMap.get(name);
			if (entry != null) {
				return first(entry.na, entry.packageEnv);
			}
		}
		return null;
	}
	
	public REnvironmentVar getSearchEnv(final String name) {
		if (name != null) {
			for (final REnvironmentVar env : this.searchEnvs) {
				final RElementName elementName= env.getElementName();
				if (elementName != null && elementName.getType() == RElementName.SCOPE_SEARCH_ENV
						&& name.equals(elementName.getSegmentName())) {
					return env;
				}
			}
		}
		return null;
	}
	
	public ICombinedRElement getByName(final RElementName name) {
		switch (name.getType()) {
		case RElementName.SCOPE_NS:
			return getNamespacePub(name.getSegmentName());
		case RElementName.SCOPE_NS_INT:
			return getNamespaceEnv(name.getSegmentName());
		case RElementName.SCOPE_SEARCH_ENV:
			return getSearchEnv(name.getSegmentName());
		case RElementName.SCOPE_PACKAGE:
			return getPackageEnv(name.getSegmentName());
		default:
			return null;
		}
	}
	
	
	public int getLazyEnvsStamp() {
		return this.lazyEnvsStamp;
	}
	
	public void updateLazyEnvs(final AbstractRController r, final IProgressMonitor monitor) {
		if (this.envsMap != null && !this.envsMap.isEmpty() && !this.lazyEnvs.isEmpty()) {
			this.envsMap.keySet().removeAll(this.lazyEnvs);
		}
		this.lazyEnvsStamp= r.getCounter();
		final Set<Long> list= r.getLazyEnvironments(monitor);
		this.lazyEnvs= (list != null && !list.isEmpty()) ? list : NO_ENVS_SET;
	}
	
	public List<REnvironmentVar> update(
			final Set<RElementName> envs, RObjectDB previous, final boolean force,
			final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		this.r= r;
		try {
			updateSearchList(monitor);
			updateNamespaceList(monitor);
			
			if (monitor.isCanceled()) {
				return null;
			}
			
			List<String> forcePkgNames= null;
			if (previous != null) {
				if (force) {
					previous= null;
				}
				else {
					forcePkgNames= previous.forceUpdatePkgNames;
				}
			}
			
			final ArrayIntList updateList= createUpdateIdxs(envs, previous, forcePkgNames);
			final List<REnvironmentVar> updateEnvs= createUpdateEnvs(updateList, monitor);
			
			updateEnvMap(updateEnvs, previous, forcePkgNames, monitor);
			
			if (previous != null) {
				for (final Map.Entry<String, RObjectDB.NamespaceEntry> entry : previous.namespaceMap.entrySet()) {
					final String name= entry.getKey();
					if (forcePkgNames != null && forcePkgNames.contains(name)) {
						continue;
					}
					
					final RObjectDB.NamespaceEntry oldEntry= entry.getValue();
					final RObjectDB.NamespaceEntry newEntry= this.namespaceMap.get(name);
					if (oldEntry == null || oldEntry.na != null || newEntry == null) {
						continue;
					}
					
					if (newEntry.namespaceEnv == null) {
						newEntry.namespaceEnv= oldEntry.namespaceEnv;
					}
					if (newEntry.namespaceExports == null) {
						newEntry.namespaceExports= oldEntry.namespaceExports;
					}
				}
			}
			
			return updateEnvs;
		}
		catch (final UnexpectedRDataException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
					"Unexpected return value from R.", e ));
		}
		finally {
			this.r= null;
		}
	}
	
	private void updateSearchList(final IProgressMonitor monitor) throws CoreException,
			UnexpectedRDataException {
		this.searchEnvsStamp= this.r.getController().getCounter();
		this.searchEnvs= new ArrayList<>();
		this.searchEnvsPublic= Collections.unmodifiableList(this.searchEnvs);
		final RVector<RCharacterStore> searchObj= RDataUtil.checkRCharVector(
				this.r.evalData("base::search()", monitor)); //$NON-NLS-1$
		
		final RCharacterStore namesData= searchObj.getData();
		for (int i= 0; i < namesData.getLength(); i++) {
			if (namesData.isNA(i)) {
				continue;
			}
			final String name= namesData.getChar(i);
			this.searchEnvs.add(new REnvironmentVar(name, true, null, null));
		}
	}
	
	private void updateNamespaceList(final IProgressMonitor monitor)
			throws CoreException, UnexpectedRDataException {
		final RVector<RCharacterStore> searchObj= RDataUtil.checkRCharVector(
				this.r.evalData("base::loadedNamespaces()", monitor )); //$NON-NLS-1$
		
		final RCharacterStore namesData= searchObj.getData();
		for (int i= 0; i < namesData.getLength(); i++) {
			if (namesData.isNA(i)) {
				continue;
			}
			final String name= namesData.getChar(i);
			getNamespaceEntry(name);
		}
	}
	
	public ArrayIntList createUpdateIdxs(final Set<RElementName> envs,
			final RObjectDB previous, final List<String> forcePkgNames) {
		final ArrayIntList updateIdxs= new ArrayIntList(this.searchEnvs.size());
		if (previous == null) {
			for (int newIdx= 0; newIdx < this.searchEnvs.size(); newIdx++) {
				updateIdxs.add(newIdx);
			}
		}
		else {
			// reuse environments until we found a new or any none-package item
			int newIdx= this.searchEnvs.size() - 1;
			for (int oldIdx= previous.searchEnvs.size(); newIdx >= 0; newIdx--) {
				final REnvironmentVar current= this.searchEnvs.get(newIdx);
				final String pkgName;
				if (current.getSpecialType() > 0 && current.getSpecialType() <= REnvironment.ENVTYPE_PACKAGE
						&& (forcePkgNames == null || (pkgName= getPkgName(current)) == null
								|| !forcePkgNames.contains(pkgName) )) {
					final int j= previous.searchEnvs.indexOf(current);
					if (j >= 0 && j < oldIdx) {
						oldIdx= j;
						if (envs != null && envs.contains(current.getElementName())) {
							updateIdxs.add(newIdx);
						}
						else {
							this.searchEnvs.set(newIdx, previous.searchEnvs.get(oldIdx));
						}
						continue;
					}
				}
				break;
			}
			
			for (; newIdx >= 0; newIdx--) {
				updateIdxs.add(newIdx);
			}
		}
		return updateIdxs;
	}
	
	private List<REnvironmentVar> createUpdateEnvs(final ArrayIntList updateIdxs,
			final IProgressMonitor monitor) throws CoreException {
		final ArrayList<REnvironmentVar> updateEnvs= new ArrayList<>(updateIdxs.size());
		for (int idx= 0; idx < updateIdxs.size(); idx++) {
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			// Debug code
//			if (item.getName().equals("methods")) {
			{	final REnvironmentVar env= this.searchEnvs.get(idx);
//				final RVector<RCharacterStore> ls= (RVector<RCharacterStore>) tools.evalData("ls(name=\""+item.getId()+"\", all.names=TRUE)", monitor);
//				final RCharacterStore lsData= ls.getData();
//				for (int i= 0; i < lsData.getLength(); i++) {
//					final String elementName= lsData.getChar(i);
////					final String elementName= lsData.getChar(133);
//					System.out.println(item.getId() + " " + elementName);
//					final RObject element= tools.evalStruct("as.environment(\""+item.getId()+"\")$\""+elementName+"\"", monitor);
//					System.out.println(element);
//				}
				
				// Regular code
				final RElementName elementName= env.getElementName();
				try {
//					long start= System.currentTimeMillis();
					final int loadOptions= RService.LOAD_PROMISE;
					final RObject robject= this.r.evalCombinedStruct(elementName,
							loadOptions, RService.DEPTH_INFINITE,
							monitor );
//					long end= System.currentTimeMillis();
//					System.out.println("update " + elementName.getDisplayName() + ": " + (end-start));
//					System.out.println(robject);
					if (robject != null && robject.getRObjectType() == RObject.TYPE_ENV) {
						final REnvironmentVar newEnv= (REnvironmentVar) robject;
						newEnv.setSource(this.r.getTool(), this.r.getController().getCounter(),
								loadOptions );
						this.searchEnvs.set(idx, newEnv);
						updateEnvs.add(newEnv);
						continue;
					}
				}
				catch (final CoreException e) {
					RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
							-1, "Error update environment "+elementName, e ));
					if (this.r.getTool().isTerminated() || monitor.isCanceled()) {
						throw e;
					}
				}
				env.setError("update error");
				updateEnvs.add(env);
				
//				final RObject test= tools.evalStruct("yy", monitor);
//				System.out.println(test);
			}
		}
		return updateEnvs;
	}
	
	private RObjectDB.NamespaceEntry getNamespaceEntry(final String name) {
		RObjectDB.NamespaceEntry entry= this.namespaceMap.get(name);
		if (entry == null) {
			entry= new NamespaceEntry();
			this.namespaceMap.put(name, entry);
		}
		return entry;
	}
	
	private String getPkgName(final REnvironmentVar env) {
		switch (env.getSpecialType()) {
		case REnvironment.ENVTYPE_BASE:
			return REnvironment.ENVNAME_BASE;
		case REnvironment.ENVTYPE_PACKAGE:
			return env.getEnvironmentName().substring(8);
		case REnvironment.ENVTYPE_NAMESPACE:
			return env.getEnvironmentName();
		case REnvironment.ENVTYPE_NAMESPACE_EXPORTS:
			return env.getEnvironmentName();
		default:
			return null;
		}
	}
	
	private void registerEnv(final Long handle, final REnvironmentVar env,
			final boolean isUptodate) {
		if (handle != null) {
			this.envsMap.put(handle, env);
		}
		
		final String name= getPkgName(env);
		if (name != null) {
			final RObjectDB.NamespaceEntry entry= getNamespaceEntry(name);
			switch (env.getSpecialType()) {
			case REnvironment.ENVTYPE_BASE:
			case REnvironment.ENVTYPE_PACKAGE:
				if (isUptodate || (entry.na == null && entry.packageEnv == null)) {
					entry.packageEnv= env;
				}
				break;
			case REnvironment.ENVTYPE_NAMESPACE:
				if (isUptodate || (entry.na == null && entry.namespaceEnv == null)) {
					entry.namespaceEnv= env;
				}
				break;
			case REnvironment.ENVTYPE_NAMESPACE_EXPORTS:
				if (isUptodate || (entry.na == null && entry.namespaceExports == null)) {
					entry.namespaceExports= env;
				}
				break;
			default:
				return;
			}
			
			if (isUptodate) {
				entry.na= null;
			}
		}
	}
	
	private void registerNA(final VirtualMissingVar na) {
		final RObjectDB.NamespaceEntry entry= getNamespaceEntry(na.getElementName().getSegmentName());
		entry.na= na;
		entry.namespaceEnv= null;
		entry.namespaceExports= null;
	}
	
	private void updateEnvMap(final List<REnvironmentVar> updateEnvs, final RObjectDB previous,
			final List<String> forcePkgNames, final IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		this.previousDB= null;
		this.cacheMode= true;
		
		for (final REnvironmentVar env : this.searchEnvs) {
			registerEnv(Long.valueOf(env.getHandle()), env, true);
		}
		
		for (final REnvironmentVar env : updateEnvs) {
			check(env, monitor);
		}
		this.previousDB= previous;
		this.forceUpdatePkgNames= forcePkgNames;
		for (final REnvironmentVar env : this.searchEnvs) {
			if (!updateEnvs.contains(env)) {
				check(env, monitor);
			}
		}
		this.previousDB= null;
		this.forceUpdatePkgNames= null;
		return;
	}
	
	public ICombinedRElement resolve(final RReferenceVar ref, 
			final int loadOptions, final boolean cacheMode,
			final ICombinedRDataAdapter r, final IProgressMonitor monitor) throws CoreException {
		this.r= r;
		this.previousDB= null;
		this.cacheMode= cacheMode;
		try {
			final ICombinedRElement resolved= evalResolve(ref, ref.getElementName(),
					loadOptions, monitor );
			
			if (resolved != null) {
				checkDirectAccessName(resolved, ref.getElementName());
			}
			
			return resolved;
		}
		finally {
			this.r= null;
		}
	}
	
	private ICombinedRElement evalResolve(final RReferenceVar ref, final RElementName fullName,
			int loadOptions, final IProgressMonitor monitor) throws CoreException {
		Long handle= (ref.getHandle() != 0) ? Long.valueOf(ref.getHandle()) : null;
		
		final boolean savedMode= this.cacheMode;
		boolean nestedMode= this.cacheMode;
		
		boolean lazy= false;
		if (ref.getReferencedRObjectType() == RObject.TYPE_ENV && handle != null) {
			ref.setResolver(this.workspace);
			
			{	final REnvironmentVar env= this.envsMap.get(handle);
				if (env != null) {
					if (this.cacheMode || env.getStamp() == this.r.getController().getCounter()) {
						return env;
					}
					// we are about to replace an environment because of wrong stamp
					// to be save, resolve all (for object browser), but correct stamp
					// can be loaded later (like this request) 
					nestedMode= true;
				}
			}
			
			lazy= this.lazyEnvs.contains(handle);
			if (!lazy && this.previousDB != null) {
				final REnvironmentVar env= this.previousDB.envsMap.get(handle);
				final List<String> forcePkgNames= this.forceUpdatePkgNames;
				final String pkgName;
				if (env != null
						&& (this.cacheMode || env.getStamp() == this.r.getController().getCounter())
						&& (forcePkgNames == null || (pkgName= getPkgName(env)) == null 
								|| !forcePkgNames.contains(pkgName) )) {
					registerEnv(handle, env, false);
					check(env, monitor);
					return env;
				}
			}
		}
		
		this.cacheMode= nestedMode;
		try {
			ICombinedRElement element= null;
			if (ref.getReferencedRObjectType() == RObject.TYPE_ENV && handle != null) {
				if (!(lazy || isNamespaceEnv(ref.getElementName()) )) {
					loadOptions|= RService.LOAD_PROMISE;
				}
				element= this.r.evalCombinedStruct(ref,
						loadOptions, RService.DEPTH_INFINITE, null, monitor );
			}
			else if (fullName != null) {
				if (!(lazy || isNamespaceEnv(fullName) )) {
					loadOptions|= RService.LOAD_PROMISE;
				}
				element= this.r.evalCombinedStruct(fullName,
						loadOptions, RService.DEPTH_INFINITE, monitor );
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0,
						"Unsupported ref: " + ref, null ));
			}
			if (element != null && element.getRObjectType() == RObject.TYPE_ENV) {
				final REnvironmentVar env= (REnvironmentVar) element;
				if (handle == null && env.getHandle() != 0) {
					handle= Long.valueOf(env.getHandle());
				}
				env.setSource(this.r.getTool(), this.r.getController().getCounter(),
						loadOptions );
				registerEnv(handle, env, true);
				check(env, monitor);
				return env;
			}
			return element;
		}
		catch (final CoreException e) {
			if (fullName != null 
					&&fullName.getNextSegment() == null
					&& RElementName.isPackageFacetScopeType(fullName.getType()) ) {
				final VirtualMissingVar na= new VirtualMissingVar(fullName,
						this.r.getTool(), this.r.getController().getCounter());
				registerNA(na);
			}
			
			RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, -1,
					"Error update ref: " + ref.getElementName(), e ));
			if (this.r.getTool().isTerminated() || monitor.isCanceled()) {
				throw e;
			}
			return null;
		}
		finally {
			this.cacheMode= savedMode;
		}
	}
	
	private void checkDirectAccessName(final ICombinedRElement var, final RElementName name) {
		if (name == null || name.getNextSegment() != null) {
			return;
		}
		
		if (var instanceof REnvironmentVar && RElementName.isScopeType(name.getType())) {
			((REnvironmentVar) var).setElementName(name);
		}
	}
	
	private void check(final ICombinedRList list,
			final IProgressMonitor monitor) throws CoreException {
		if (list.hasModelChildren(null)) {
			final long length= list.getLength();
			if (length <= Integer.MAX_VALUE) {
				final int l= (int) length;
				ITER_CHILDREN : for (int i= 0; i < l; i++) {
					final RObject object= list.get(i);
					if (object != null) {
						switch (object.getRObjectType()) {
						case RObject.TYPE_REFERENCE:
							if (this.cacheMode
									&& ((RReferenceVar) object).getReferencedRObjectType() == RObject.TYPE_ENV) {
								evalResolve((RReferenceVar) object, null, 0, monitor);
							}
							else {
								((RReferenceVar) object).setResolver(this.workspace);
							}
							continue ITER_CHILDREN;
						case RObject.TYPE_LIST:
						case RObject.TYPE_S4OBJECT:
							check((ICombinedRList) object, monitor);
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
								evalResolve((RReferenceVar) object, null, 0, monitor);
							}
							else {
								((RReferenceVar) object).setResolver(this.workspace);
							}
							continue ITER_CHILDREN;
						case RObject.TYPE_LIST:
						case RObject.TYPE_S4OBJECT:
							check((ICombinedRList) object, monitor);
							continue ITER_CHILDREN;
						default:
							continue ITER_CHILDREN;
						}
					}
				}
			}
		}
	}
	
	public void handleRPkgChange(final List<String> names) {
		this.forceUpdatePkgNames= (this.forceUpdatePkgNames != null) ?
				ImCollections.concatList(this.forceUpdatePkgNames, names) :
				names;
	}
	
}
