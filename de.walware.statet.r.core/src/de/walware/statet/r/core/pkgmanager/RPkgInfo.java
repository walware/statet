/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.pkgmanager;

import de.walware.statet.r.core.renv.IRLibraryLocation;
import de.walware.statet.r.core.renv.RNumVersion;
import de.walware.statet.r.internal.core.pkgmanager.RPkg;


public class RPkgInfo extends RPkg implements IRPkgInfo {
	
	
	private final String fTitle;
	
	private final String fBuilt;
	
	private final IRLibraryLocation fLib;
	
	private final int fFlags;
	private final long fInstallStamp;
	private final String fRepoId;
	
	
	public RPkgInfo(final String name,final IRLibraryLocation lib,
			final int flags, final long installStamp, final String repoId) {
		this(name, null, null, null, lib, flags, installStamp, repoId);
	}
	
	public RPkgInfo(final String name, final RNumVersion version, final String built,
			final String title, final IRLibraryLocation lib,
			final int flags, final long installStamp, final String repoId) {
		super(name, version);
		fBuilt = (built != null) ? built : ""; //$NON-NLS-1$
		fTitle = (title !=null && !title.isEmpty()) ? title : null;
		fLib = lib;
		fFlags = flags;
		fInstallStamp = installStamp;
		fRepoId = (repoId != null) ? repoId.intern() : ""; //$NON-NLS-1$
	}
	
	
	@Override
	public String getTitle() {
		return fTitle;
	}
	
	@Override
	public String getBuilt() {
		return fBuilt;
	}
	
	@Override
	public IRLibraryLocation getLibraryLocation() {
		return fLib;
	}
	
	@Override
	public int getFlags() {
		return fFlags;
	}
	
	@Override
	public long getInstallStamp() {
		return fInstallStamp;
	}
	
	@Override
	public String getRepoId() {
		return fRepoId;
	}
	
}
