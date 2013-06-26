/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.List;

import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkg;

import de.walware.statet.r.core.pkgmanager.IRPkgData;


public class RPkgData extends RPkg implements IRPkgData {
	
	
	private String fLicense;
	
	private List<? extends IRPkg> fDependsList;
	private List<? extends IRPkg> fImportsList;
	private List<? extends IRPkg> fLinkingToList;
	private List<? extends IRPkg> fSuggestsList;
	private List<? extends IRPkg> fEnhancesList;
	
	private final String fRepoId;
	
	private String fPriority;
	
	
	public RPkgData(final String name, final RNumVersion version,
			final String repoId) {
		super(name, version);
		fLicense = ""; //$NON-NLS-1$
		fRepoId = (repoId != null) ? repoId.intern() : ""; //$NON-NLS-1$
		fPriority = "other"; //$NON-NLS-1$
	}
	
	
	protected void setLicense(final String s) {
		fLicense = (s != null && !s.isEmpty()) ? s : ""; //$NON-NLS-1$
	}
	
	@Override
	public String getLicense() {
		return fLicense;
	}
	
	protected void setPriority(final String s) {
		fPriority = (s != null && !s.isEmpty()) ? s.intern() : "other"; //$NON-NLS-1$
	}
	
	@Override
	public String getPriority() {
		return fPriority;
	}
	
	protected void setDepends(final List<? extends IRPkg> list) {
		fDependsList = list;
	}
	
	@Override
	public List<? extends IRPkg> getDepends() {
		return fDependsList;
	}
	
	protected void setImports(final List<? extends IRPkg> list) {
		fImportsList = list;
	}
	
	@Override
	public List<? extends IRPkg> getImports() {
		return fImportsList;
	}
	
	protected void setLinkingTo(final List<? extends IRPkg> list) {
		fLinkingToList = list;
	}
	
	@Override
	public List<? extends IRPkg> getLinkingTo() {
		return fLinkingToList;
	}
	
	protected void setSuggests(final List<? extends IRPkg> list) {
		fSuggestsList = list;
	}
	
	@Override
	public List<? extends IRPkg> getSuggests() {
		return fSuggestsList;
	}
	
	protected void setEnhances(final List<? extends IRPkg> list) {
		fEnhancesList = list;
	}
	
	@Override
	public List<? extends IRPkg> getEnhances() {
		return fEnhancesList;
	}
	
	@Override
	public String getRepoId() {
		return fRepoId;
	}
	
}
