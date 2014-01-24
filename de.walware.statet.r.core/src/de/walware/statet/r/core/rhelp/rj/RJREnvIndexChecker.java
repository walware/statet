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

package de.walware.statet.r.core.rhelp.rj;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.services.RService;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.pkgmanager.IRPkgManager;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.core.rhelp.REnvIndexChecker;


public class RJREnvIndexChecker {
	
	
	public static final int NOT_AVAILABLE = -1;
	/** the index is up-to-date */
	public static final int UP_TO_DATE = 0;
	/** complete index is missing */
	public static final int COMPLETE = 1;
	/** new or changed packages found in R */
	public static final int PACKAGES = 2;
	
	
	private final IREnvConfiguration fREnvConfig;
	private final REnvIndexChecker fIndex;
	
	
	public RJREnvIndexChecker(final IREnvConfiguration rEnvConfig) {
		if (rEnvConfig == null) {
			throw new NullPointerException("rEnvConfig"); //$NON-NLS-1$
		}
		fREnvConfig = rEnvConfig;
		fIndex = new REnvIndexChecker(rEnvConfig);
	}
	
	
	public int check(final RService r,
			final IProgressMonitor monitor) throws CoreException {
		if (!fIndex.preCheck()) {
			return NOT_AVAILABLE;
		}
		Exception errorCause = null;
		try {
			if (fIndex.needsComplete()) {
				return COMPLETE;
			}
			
			final IRPkgManager rPkgManager = RCore.getRPkgManager(fREnvConfig.getReference());
			final IRPkgSet rPkgSet = rPkgManager.getRPkgSet();
			final List<String> installedPkgNames = rPkgSet.getNames();
			fIndex.beginPackageCheck();
			for (final String pkgName : installedPkgNames) {
				final IRPkgInfo pkgInfo = rPkgSet.getInstalled().getFirstByName(pkgName);
				if (pkgInfo == null) {
					continue;
				}
				fIndex.checkPackage(pkgName, pkgInfo.getVersion(), pkgInfo.getBuilt());
			}
			fIndex.endPackageCheck();
			
			if (fIndex.needsComplete()) {
				return COMPLETE;
			}
			else if (fIndex.hasPackageChanges()) {
				return PACKAGES;
			}
			else {
				return UP_TO_DATE;
			}
		}
//		catch (final CoreException e) {
//			fIndex.cancelCheck();
//			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
//				throw e;
//			}
//			errorCause = e;
//		}
		catch (final Exception e) {
			fIndex.cancelCheck();
			errorCause = e;
		}
		finally {
			fIndex.finalCheck();
		}
		throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when checking the package data.", errorCause ));
	}
	
	
	public boolean wasAlreadyReported() {
		return !fIndex.hasNewChanges();
	}
	
	public int getNewPackageCount() {
		return fIndex.getNewPackageCount();
	}
	
	public int getChangedPackageCount() {
		return fIndex.getChangedPackageCount();
	}
	
}
