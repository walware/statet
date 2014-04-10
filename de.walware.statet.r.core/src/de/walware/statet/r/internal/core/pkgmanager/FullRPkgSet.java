/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.SortedArraySet;

import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkg;

import de.walware.statet.r.core.pkgmanager.IRPkgData;
import de.walware.statet.r.core.pkgmanager.IRPkgSet;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;


public class FullRPkgSet implements IRPkgSet.Ext {
	
	
	public static final FullRPkgSet DUMMY= new FullRPkgSet();
	
	
	private final RPkgCollection<RPkgInfoAndData> installed;
	
	private final List<String> priorities= new ArrayList<>(DEFAULT_PRIORITIES);
	private final RPkgCollection<RPkgData> available;
	
	private final RPkgList<RPkgData> reverse;
	
	private List<String> names;
	
	
	private FullRPkgSet() {
		this.installed= new RPkgCollection<>(0);
		this.available= new RPkgCollection<>(0);
		this.reverse= new RPkgList<>(0);
	}
	
	public FullRPkgSet(final int available) {
		this.installed= new RPkgCollection<>(8);
		this.available= new RPkgCollection<>(available);
		this.reverse= new RPkgList<>(4);
	}
	
	private FullRPkgSet(final RPkgCollection<RPkgData> available) {
		this.installed= new RPkgCollection<>(8);
		this.available= available;
		this.reverse= new RPkgList<>(4);
	}
	
	public FullRPkgSet cloneAvailable() {
		final FullRPkgSet newPkgs= new FullRPkgSet(this.available);
		return newPkgs;
	}
	
	
	@Override
	public synchronized List<String> getNames() {
		if (this.names == null) {
			final List<String> availableNames= this.available.getNames();
			final SortedArraySet<String> names= new SortedArraySet<>(
					availableNames.toArray(new String[availableNames.size() + 16]),
					availableNames.size(), RPkgUtil.COLLATOR );
			names.addAll(this.installed.getNames());
			this.names= Collections.unmodifiableList(names);
		}
		return this.names;
	}
	
	@Override
	public RPkgCollection<RPkgInfoAndData> getInstalled() {
		return this.installed;
	}
	
	@Override
	public List<String> getPriorities() {
		return this.priorities;
	}
	
	@Override
	public RPkgCollection<RPkgData> getAvailable() {
		return this.available;
	}
	
	
	private List<RPkgList<RPkgData>> getAll() {
		return ConstArrayList.<RPkgList<RPkgData>>concat((List) this.installed.getAll(), this.available.getAll());
	}
	
	@Override
	public IRPkgData getReverse(final String name) {
		final int idx= this.reverse.indexOf(name);
		RPkgData info;
		if (idx >= 0) {
			info= this.reverse.get(idx);
		}
		else {
			info= new RPkgData(name, RNumVersion.NONE, null);
			RPkgList<VersionListRPkg> depends= null;
			RPkgList<VersionListRPkg> imports= null;
			RPkgList<VersionListRPkg> linkingTo= null;
			RPkgList<VersionListRPkg> suggests= null;
			RPkgList<VersionListRPkg> enhances= null;
			for (final RPkgList<RPkgData> list : getAll()) {
				for (int i= 0; i < list.size(); i++) {
					final RPkgData pkg= list.get(i);
					if (name.equals(pkg)) {
						continue;
					}
					if (Util.findPkg(pkg.getDepends(), name) >= 0) {
						if (depends == null) {
							depends= new RPkgList<>(4);
						}
						addRev(depends, pkg);
					}
					if (Util.findPkg(pkg.getImports(), name) >= 0) {
						if (imports == null) {
							imports= new RPkgList<>(4);
						}
						addRev(imports, pkg);
					}
					if (Util.findPkg(pkg.getLinkingTo(), name) >= 0) {
						if (linkingTo == null) {
							linkingTo= new RPkgList<>(4);
						}
						addRev(linkingTo, pkg);
					}
					if (Util.findPkg(pkg.getSuggests(), name) >= 0) {
						if (suggests == null) {
							suggests= new RPkgList<>(4);
						}
						addRev(suggests, pkg);
					}
					if (Util.findPkg(pkg.getEnhances(), name) >= 0) {
						if (enhances == null) {
							enhances= new RPkgList<>(4);
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
			
			this.reverse.add(-(idx + 1), info);
		}
		return info;
	}
	
	private void addRev(final RPkgList<VersionListRPkg> depends, final IRPkg v) {
		final int idx= depends.indexOf(v.getName());
		if (idx >= 0) {
			final VersionListRPkg ref= depends.get(idx);
			ref.addVersion(v.getVersion());
		}
		else {
			depends.add(-idx - 1, new VersionListRPkg(v.getName(), v.getVersion()));
		}
	}
	
	public void checkPkgInfo(final RPkgData pkg) {
		{	final String priority= pkg.getPriority();
			if (!this.priorities.contains(priority)) {
				pkg.setPriority("other"); //$NON-NLS-1$
			}
		}
	}
	
}
