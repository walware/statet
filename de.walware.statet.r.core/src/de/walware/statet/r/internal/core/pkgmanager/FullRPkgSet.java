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

import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.renv.IRPkg;
import de.walware.statet.r.core.renv.RNumVersion;


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
			info = new RPkgData(name, null, null);
			RPkgList<RPkg> depends = null;
			RPkgList<RPkg> imports = null;
			RPkgList<RPkg> linkingTo = null;
			RPkgList<RPkg> suggests = null;
			RPkgList<RPkg> enhances = null;
			for (final RPkgList<RPkgData> list : getAll()) {
				for (int i = 0; i < list.size(); i++) {
					final RPkgData pkg = list.get(i);
					if (name.equals(pkg)) {
						continue;
					}
					if (Util.findPkg(pkg.getDepends(), name) >= 0) {
						if (depends == null) {
							depends = new RPkgList<RPkg>(4);
						}
						addRev(depends, pkg);
					}
					if (Util.findPkg(pkg.getImports(), name) >= 0) {
						if (imports == null) {
							imports = new RPkgList<RPkg>(4);
						}
						addRev(imports, pkg);
					}
					if (Util.findPkg(pkg.getLinkingTo(), name) >= 0) {
						if (linkingTo == null) {
							linkingTo = new RPkgList<RPkg>(4);
						}
						addRev(linkingTo, pkg);
					}
					if (Util.findPkg(pkg.getSuggests(), name) >= 0) {
						if (suggests == null) {
							suggests = new RPkgList<RPkg>(4);
						}
						addRev(suggests, pkg);
					}
					if (Util.findPkg(pkg.getEnhances(), name) >= 0) {
						if (enhances == null) {
							enhances = new RPkgList<RPkg>(4);
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
	
	private void addRev(final RPkgList<RPkg> depends, final IRPkg v) {
		final int idx = depends.indexOf(v.getName());
		if (idx >= 0) {
			final RPkg ref = depends.get(idx);
			ref.setVersion(addVersion(ref.getVersion(), v.getVersion()));
		}
		else {
			depends.add(-idx - 1, new RPkg(v.getName(), v.getVersion()));
		}
	}
	
	private RNumVersion addVersion(final RNumVersion list, final RNumVersion v) {
		if (list.equals(v) || v == RNumVersion.NONE) {
			return list;
		}
		if (list == RNumVersion.NONE) {
			return v;
		}
		final String listString = list.toString();
		final String vString = v.toString();
		for (int i = 0; i < listString.length(); i++) {
			final int idx = listString.indexOf(v.toString(), i);
			if (idx >= 0) {
				if ((idx == 0 || listString.regionMatches(idx - 2, ", ", 0, 2)) //$NON-NLS-1$
						&& (idx + vString.length() == listString.length() || listString.regionMatches(idx + vString.length(), ", ", 0, 2)) ) {
					return list;
				}
				continue;
			}
			break;
		}
		return RNumVersion.create(listString + ", " + vString); //$NON-NLS-1$
	}
	
	public void checkPkgInfo(final RPkgData pkg) {
		{	final String priority = pkg.getPriority();
			if (!fPriorities.contains(priority)) {
				pkg.setPriority("other"); //$NON-NLS-1$
			}
		}
	}
	
}
