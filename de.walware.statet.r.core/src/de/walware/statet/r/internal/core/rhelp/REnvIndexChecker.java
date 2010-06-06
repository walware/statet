/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.internal.core.RCorePlugin;


public class REnvIndexChecker {
	
	
	private final IREnvConfiguration fREnvConfig;
	
	private final Set<String> fCheckedNames = new HashSet<String>(64);
	
	private int fNewPkg;
	private int fChangedPkg;
	private int fNewChange = -1;
	
	private Map<String, String> fFoundCurrent = new HashMap<String, String>();
	private Map<String, String> fFoundPrevious = new HashMap<String, String>();
	
	private Directory fIndexDirectory;
	
	private IREnvHelp fREnvHelp;
	
	private boolean fInPackageCheck;
	
	
	public REnvIndexChecker(final IREnvConfiguration rEnvConfig) {
		if (rEnvConfig == null) {
			throw new NullPointerException("rEnvConfig"); //$NON-NLS-1$
		}
		fREnvConfig = rEnvConfig;
		
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
				fIndexDirectory = FSDirectory.open(directory);
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
		if (!file.createNewFile() || !file.delete()) {
			return false;
		}
		return true;
	}
	
	
	public boolean preCheck() {
		fNewChange = (fNewChange < 0) ? 1 : 0;
		
		if (fREnvConfig == null || fIndexDirectory == null) {
			return false;
		}
		
		final IREnvHelp envHelp = RCore.getRHelpManager().getHelp(fREnvConfig.getReference());
		if ((envHelp != null) ? (fREnvHelp == null) : fREnvHelp != null) {
			fNewChange = 1;
		}
		fREnvHelp = envHelp;
		
		final IREnvConfiguration config = fREnvConfig.getReference().getConfig();
		if (!fREnvConfig.equals(config)) {
			return false;
		}
		
		try {
			if (IndexWriter.isLocked(fIndexDirectory)) {
				return false;
			}
		}
		catch (final IOException e) {
		}
		return true;
	}
	
	public void beginPackageCheck() {
		fInPackageCheck = true;
		
		final Map<String, String> tmp = fFoundCurrent;
		fFoundCurrent = fFoundPrevious;
		fFoundPrevious = tmp;
		
		fNewPkg = 0;
		fChangedPkg = 0;
		fNewChange = 0;
	}
	
	public void checkPackage(final String pkgName, final String pkgVersion) {
		if (pkgName != null && fCheckedNames.add(pkgName)) {
			final IRPackageHelp packageHelp = fREnvHelp.getRPackage(pkgName);
			if (packageHelp == null) {
				fNewPkg++;
				fFoundCurrent.put(pkgName, pkgVersion);
				if (fNewChange == 0 && !pkgVersion.equals(fFoundPrevious.get(pkgName))) {
					fNewChange = 1;
				}
			}
			else if (!packageHelp.getVersion().equals(pkgVersion)) {
				fChangedPkg++;
				fFoundCurrent.put(pkgName, pkgVersion);
				if (fNewChange == 0 && !pkgVersion.equals(fFoundPrevious.get(pkgName))) {
					fNewChange = 1;
				}
			}
		}
	}
	
	public Set<String> getCheckedPackages() {
		return fCheckedNames;
	}
	
	public void endPackageCheck() {
		fInPackageCheck = false;
		
		fFoundPrevious.clear();
	}
	
	public void cancelCheck() {
		if (fInPackageCheck) {
			final Map<String, String> tmp = fFoundCurrent;
			fFoundCurrent = fFoundPrevious;
			fFoundPrevious = tmp;
		}
		
		fFoundPrevious.clear();
	}
	
	
	public boolean hasNewChanges() {
		return (fNewChange > 0);
	}
	
	public boolean needsComplete() {
		return (fREnvHelp == null);
	}
	
	public boolean hasPackageChanges() {
		return (fNewPkg > 0 || fChangedPkg > 0);
	}
	
	public int getNewPackageCount() {
		return fNewPkg;
	}
	
	public int getChangedPackageCount() {
		return fChangedPkg;
	}
	
}
