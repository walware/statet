/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.rhelp.index.REnvIndexWriter;


public class REnvIndexChecker {
	
	
	private static boolean equalVersion(final IRPkgInfo pkg1, final IRPkgInfo pkg2) {
		return (pkg2 != null
				&& pkg1.getVersion().equals(pkg2.getVersion())
				&& pkg1.getBuilt().equals(pkg2.getBuilt()) );
	}
	
	
	private final IREnvConfiguration rEnvConfig;
	
	private int newPkg;
	private int changedPkg;
	private int newChange= -1;
	
	private Map<String, IRPkgInfo> needUpdate= new HashMap<>();
	private Map<String, IRPkgInfo> needUpdatePrevious= new HashMap<>();
	
	private Directory indexDirectory;
	
	private IREnvHelp rEnvHelp;
	private boolean rEnvHelpLock;
	
	private boolean inPackageCheck;
	
	
	public REnvIndexChecker(final IREnvConfiguration rEnvConfig) {
		if (rEnvConfig == null) {
			throw new NullPointerException("rEnvConfig"); //$NON-NLS-1$
		}
		this.rEnvConfig = rEnvConfig;
		
		final File directory = SaveUtil.getIndexDirectory(rEnvConfig);
		if (directory != null) {
			try {
				// directory writable?
				if (!isWritable(directory)) {
					if (REnvIndexWriter.DEBUG) {
						RCorePlugin.log(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1,
								NLS.bind("The index directory ''{0}'' is not writable.", directory.toString()), //$NON-NLS-1$
								null));
					}
					return;
				}
				
				// Lucene directory
				this.indexDirectory = new SimpleFSDirectory(directory);
			}
			catch (final IOException e) {
				RCorePlugin.log(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1,
						NLS.bind("The index directory ''{0}'' is not accessible.", directory.toString()), //$NON-NLS-1$
						e));
			}
		}
	}
	
	
	private boolean isWritable(final File directory) throws IOException {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				return false;
			}
		}
		final File file = new File(directory, "test"+System.nanoTime()); //$NON-NLS-1$
		try {
			if (!file.createNewFile() || !file.delete()) {
				return false;
			}
		}
		catch (final IOException e) {
			return false;
		}
		return true;
	}
	
	
	public boolean preCheck() {
		this.newChange = (this.newChange < 0) ? 1 : 0;
		
		if (this.rEnvConfig == null || this.indexDirectory == null) {
			return false;
		}
		
		final IREnvHelp envHelp = RCore.getRHelpManager().getHelp(this.rEnvConfig.getReference());
		if ((envHelp != null) ? (this.rEnvHelp == null) : this.rEnvHelp != null) {
			this.newChange = 1;
		}
		this.rEnvHelp = envHelp;
		this.rEnvHelpLock = (this.rEnvHelp != null);
		
		try {
			if (!this.rEnvConfig.equals(this.rEnvConfig.getReference().getConfig())
					|| IndexWriter.isLocked(this.indexDirectory) ) {
				return false;
			}
		}
		catch (final IOException e) {
			return false;
		}
		return true;
	}
	
	public void beginPackageCheck() {
		this.inPackageCheck = true;
		
		final Map<String, IRPkgInfo> tmp = this.needUpdate;
		this.needUpdate = this.needUpdatePrevious;
		this.needUpdatePrevious = tmp;
		
		this.newPkg = 0;
		this.changedPkg = 0;
		this.newChange = 0;
	}
	
	public void checkPackage(final IRPkgInfo pkgInfo) {
		if (!REnvIndexWriter.IGNORE_PKG_NAMES.contains(pkgInfo.getName())) {
			final IRPkgHelp pkgHelp= this.rEnvHelp.getPkgHelp(pkgInfo.getName());
			if (pkgHelp == null) {
				this.newPkg++;
				this.needUpdate.put(pkgInfo.getName(), pkgInfo);
				if (this.newChange == 0 && !equalVersion(pkgInfo, this.needUpdatePrevious.get(pkgInfo.getName()))) {
					this.newChange= 1;
				}
			}
			else if (!(pkgInfo.getVersion().toString().equals(pkgHelp.getVersion())
					&& pkgInfo.getBuilt().equals(pkgHelp.getBuilt()) )) {
				this.changedPkg++;
				this.needUpdate.put(pkgInfo.getName(), pkgInfo);
				if (this.newChange == 0 && !equalVersion(pkgInfo, this.needUpdatePrevious.get(pkgInfo.getName()))) {
					this.newChange= 1;
				}
			}
		}
	}
	
	public void endPackageCheck() {
		this.inPackageCheck = false;
		
		this.needUpdatePrevious.clear();
	}
	
	public void cancelCheck() {
		if (this.inPackageCheck) {
			final Map<String, IRPkgInfo> tmp = this.needUpdate;
			this.needUpdate = this.needUpdatePrevious;
			this.needUpdatePrevious = tmp;
		}
		
		this.needUpdatePrevious.clear();
		unlock();
	}
	
	public void finalCheck() {
		unlock();
	}
	
	private void unlock() {
		if (this.rEnvHelpLock) {
			this.rEnvHelpLock = false;
			this.rEnvHelp.unlock();
		}
	}
	
	
	public boolean hasNewChanges() {
		return (this.newChange > 0);
	}
	
	public boolean needsComplete() {
		return (this.rEnvHelp == null);
	}
	
	public boolean hasPackageChanges() {
		return (this.newPkg > 0 || this.changedPkg > 0);
	}
	
	public int getNewPackageCount() {
		return this.newPkg;
	}
	
	public int getChangedPackageCount() {
		return this.changedPkg;
	}
	
}
