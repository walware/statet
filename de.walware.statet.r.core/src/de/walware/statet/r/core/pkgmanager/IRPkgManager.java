/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.pkgmanager;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.preferences.core.Preference;
import de.walware.ecommons.ts.ITool;

import de.walware.rj.services.RPlatform;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.core.pkgmanager.RRepoListPref;


public interface IRPkgManager {
	
	String PREF_QUALIFIER = RCore.PLUGIN_ID + "/r.pkgmanager"; //$NON-NLS-1$
	
	Preference<List<RRepo>> CUSTOM_REPO_PREF = new RRepoListPref(RCore.PLUGIN_ID + PREF_QUALIFIER, "CustomRepo.list"); //$NON-NLS-1$
	Preference<List<RRepo>> CUSTOM_CRAN_MIRROR_PREF = new RRepoListPref(RCore.PLUGIN_ID + PREF_QUALIFIER, "CustomCRANMirror.list"); //$NON-NLS-1$
	Preference<List<RRepo>> CUSTOM_BIOC_MIRROR_PREF = new RRepoListPref(RCore.PLUGIN_ID + PREF_QUALIFIER, "CustomBIOCMirror.list"); //$NON-NLS-1$
	
	
	int NONE = 0;
	int INITIAL = 1;
	
	int AVAILABLE = 1;
	int INSTALLED = 2;
	
	
	interface Event {
		
		
		IREnv getREnv();
		
		int reposChanged();
		
		int pkgsChanged();
		
		IRPkgSet getOldPkgSet();
		IRPkgSet getNewPkgSet();
		IRPkgChangeSet getInstalledPkgChangeSet();
		
		int viewsChanged();
		
	}
	
	interface Listener {
		
		
		void handleChange(Event event);
		
	}
	
	interface Ext extends IRPkgManager {
		
		
		boolean requiresUpdate();
		void update(RService r, IProgressMonitor monitor) throws CoreException;
		
		List<RRepo> getAvailableRepos();
		List<RRepo> getAvailableBioCMirrors();
		List<RRepo> getAvailableCRANMirrors();
		RRepo getRepo(String repoId);
		
		ISelectedRepos getSelectedRepos();
		void setSelectedRepos(ISelectedRepos settings);
		IStatus getReposStatus(ISelectedRepos settings);
		
		void refreshPkgs();
		IRPkgSet.Ext getExtRPkgSet();
		List<? extends IRView> getRViews();
//		List<? extends IRView> getBioCViews();
		
		void apply(final ITool rTool);
		
		void loadPkgs(final ITool rTool,
				final List<? extends IRPkgInfoAndData> pkgs, final boolean expliciteLocation);
		
	}
	
	
	IREnv getREnv();
	RPlatform getRPlatform();
	
	Lock getReadLock();
	Lock getWriteLock();
	
	void addListener(Listener listener);
	void removeListener(Listener listener);
	
	void clear();
	
	/**
	 * Checks the R environment for changes in R lib paths.
	 * 
	 * @param flags {@link #NONE}, {@link #INITIAL}
	 * @param r an R service of the R environment
	 * @param monitor
	 * @throws CoreException
	 */
	void check(int flags, RService r, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Returns the R libPath (state of last check).
	 * 
	 * @return the lib path
	 */
	IRLibPaths getRLibPaths();
	
	/**
	 * Returns the R package set (state of last check).
	 * 
	 * @return the R package set
	 */
	IRPkgSet getRPkgSet();
	
	IRPkgData addToCache(IFileStore store, IProgressMonitor monitor) throws CoreException;
	void perform(ITool rTool, List<? extends RPkgAction> actions);
	
}
