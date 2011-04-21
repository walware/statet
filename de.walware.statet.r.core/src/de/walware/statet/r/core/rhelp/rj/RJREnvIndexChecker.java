/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp.rj;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.services.RService;
import de.walware.statet.r.core.RCore;
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
	
	
	private final REnvIndexChecker fIndex;
	
	private double fLastChange = 0;
	
	private boolean fRJPackageFound;
	
	
	public RJREnvIndexChecker(final IREnvConfiguration rEnvConfig) {
		fIndex = new REnvIndexChecker(rEnvConfig);
	}
	
	
	public int check(final RService r,
			final IProgressMonitor monitor) throws CoreException {
		if (!fIndex.preCheck()) {
			return NOT_AVAILABLE;
		}
		Exception errorCause = null;
		try {
			final double lastChange = RDataUtil.checkSingleNumValue(r.evalData("max(file.info(.libPaths())$mtime)", monitor)); //$NON-NLS-1$
			if (fLastChange != lastChange || fIndex.hasNewChanges()) {
				fLastChange = lastChange;
				
				fRJPackageFound = false;
				
				final RVector<RCharacterStore> pkgData = RDataUtil.checkRCharVector(
						r.evalData("installed.packages()[,\"Version\"]", monitor)); //$NON-NLS-1$
				final RCharacterStore pkgVersions = pkgData.getData();
				final RStore names = pkgData.getNames();
				
				if (fIndex.needsComplete()) {
					fRJPackageFound = names.contains("rj"); //$NON-NLS-1$
				}
				else {
					fIndex.beginPackageCheck();
					
					final int count = names.getLength();
					for (int i = 0; i < count; i++) {
						fIndex.checkPackage(names.getChar(i), pkgVersions.get(i));
					}
					fRJPackageFound = fIndex.getCheckedPackages().contains("rj"); //$NON-NLS-1$
					
					fIndex.endPackageCheck();
				}
			}
			
			if (fIndex.needsComplete()) {
				return COMPLETE;
			} else if (fIndex.hasPackageChanges()) {
				return PACKAGES;
			}
			else {
				return UP_TO_DATE;
			}
		}
		catch (final CoreException e) {
			fIndex.cancelCheck();
			if (e.getStatus().getSeverity() == IStatus.CANCEL) {
				throw e;
			}
			errorCause = e;
		}
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
	
	public boolean isRJPackageInstalled() {
		return fRJPackageFound;
	}
	
}
