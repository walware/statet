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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.ConstList;

import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkg;

import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;


public class FullRPkgSet implements IRPkgSet.Ext {
	
	
	public static final FullRPkgSet DUMMY = new FullRPkgSet();
	
	
	private final RPkgCollection<RPkgDescription> fInstalled;
	
	private final List<String> fPriorities = new ArrayList<String>(DEFAULT_PRIORITIES);
	private final RPkgCollection<RPkgData> fAvailable;
	
	private final RPkgList<RPkgData> fReverse;
	
	private List<String> fNames;
	
	
	private FullRPkgSet() {
		fInstalled = new RPkgCollection<RPkgDescription>(0);
		fAvailable = new RPkgCollection<RPkgData>(0);
		fReverse = new RPkgList<RPkgData>(0);
	}
	
	public FullRPkgSet(final int available) {
		fInstalled = new RPkgCollection<RPkgDescription>(8);
		fAvailable = new RPkgCollection<RPkgData>(available);
		fReverse = new RPkgList<RPkgData>(4);
	}
	
	private FullRPkgSet(final RPkgCollection<RPkgData> available) {
		fInstalled = new RPkgCollection<RPkgDescription>(8);
		fAvailable = available;
		fReverse = new RPkgList<RPkgData>(4);
	}
	
	public FullRPkgSet cloneAvailable() {
		final FullRPkgSet newPkgs = new FullRPkgSet(fAvailable);
		return newPkgs;
	}
	
	
	@Override
	public synchronized List<String> getNames() {
		if (fNames == null) {
			final RPkgNameList names = new RPkgNameList(64);
			fInstalled.addNames(names);
			fAvailable.addNames(names);
			fNames = Collections.unmodifiableList(names);
		}
		return fNames;
	}
	
	@Override
	public RPkgCollection<RPkgDescription> getInstalled() {
		return fInstalled;
	}
	
	@Override
	public List<String> getPriorities() {
		return fPriorities;
	}
	
	@Override
	public RPkgCollection<RPkgData> getAvailable() {
		return fAvailable;
	}
	
	
	private List<RPkgList<RPkgData>> getAll() {
		return ConstList.<RPkgList<RPkgData>>concat((List) fInstalled.getAll(), fAvailable.getAll());
	}
	
	@Override
	public IRPkgData getReverse(final String name) {
		final int idx = fReverse.indexOf(name);
		RPkgData info;
		if (idx >= 0) {
			info = fReverse.get(idx);
		}
		else {
			info = new RPkgData(name, RNumVersion.NONE, null);
			RPkgList<VersionListRPkg> depends = null;
			RPkgList<VersionListRPkg> imports = null;
			RPkgList<VersionListRPkg> linkingTo = null;
			RPkgList<VersionListRPkg> suggests = null;
			RPkgList<VersionListRPkg> enhances = null;
			for (final RPkgList<RPkgData> list : getAll()) {
				for (int i = 0; i < list.size(); i++) {
					final RPkgData pkg = list.get(i);
					if (name.equals(pkg)) {
						continue;
					}
					if (Util.findPkg(pkg.getDepends(), name) >= 0) {
						if (depends == null) {
							depends = new RPkgList<VersionListRPkg>(4);
						}
						addRev(depends, pkg);
					}
					if (Util.findPkg(pkg.getImports(), name) >= 0) {
						if (imports == null) {
							imports = new RPkgList<VersionListRPkg>(4);
						}
						addRev(imports, pkg);
					}
					if (Util.findPkg(pkg.getLinkingTo(), name) >= 0) {
						if (linkingTo == null) {
							linkingTo = new RPkgList<VersionListRPkg>(4);
						}
						addRev(linkingTo, pkg);
					}
					if (Util.findPkg(pkg.getSuggests(), name) >= 0) {
						if (suggests == null) {
							suggests = new RPkgList<VersionListRPkg>(4);
						}
						addRev(suggests, pkg);
					}
					if (Util.findPkg(pkg.getEnhances(), name) >= 0) {
						if (enhances == null) {
							enhances = new RPkgList<VersionListRPkg>(4);
						}
						addRev(enhances, pkg);
					}
				}
			}
			info.setDepends((depends != null) ? depends : Collections.<RPkg>emptyList());
			info.setImports((imports != null) ? imports : Collections.<RPkg>emptyList());
			info.setLinkingTo((linkingTo != null) ? linkingTo : Collections.<RPkg>emptyList());
			info.setSuggests((suggests != null) ? suggests : Collections.<RPkg>emptyList());
			info.setEnhances((enhances != null) ? enhances : Collections.<RPkg>emptyList());
			
			fReverse.add(-(idx + 1), info);
		}
		return info;
	}
	
	private void addRev(final RPkgList<VersionListRPkg> depends, final IRPkg v) {
		final int idx = depends.indexOf(v.getName());
		if (idx >= 0) {
			final VersionListRPkg ref = depends.get(idx);
			ref.addVersion(v.getVersion());
		}
		else {
			depends.add(-idx - 1, new VersionListRPkg(v.getName(), v.getVersion()));
		}
	}
	
	public void checkPkgInfo(final RPkgData pkg) {
		{	final String priority = pkg.getPriority();
			if (!fPriorities.contains(priority)) {
				pkg.setPriority("other"); //$NON-NLS-1$
			}
		}
	}
	
}
