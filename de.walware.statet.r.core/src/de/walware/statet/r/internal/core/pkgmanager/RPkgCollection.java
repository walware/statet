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

package de.walware.statet.r.internal.core.pkgmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.SortedArraySet;

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.pkgmanager.IRPkgCollection;
import de.walware.statet.r.core.pkgmanager.RPkgUtil;


public class RPkgCollection<T extends IRPkg> implements IRPkgCollection<T> {
	
	/** Package sources */
	private final List<String> sources;
	/** Packages by sources */
	private final List<RPkgList<T>> pkgLists;
	
	private volatile List<String> names;
	
	
	public RPkgCollection(final int size) {
		this.sources= new ArrayList<>(size);
		this.pkgLists= new ArrayList<>(size);
	}
	
	
	public void add(final String source, final RPkgList<T> list) {
		if (list != null) {
			this.sources.add((source != null) ? source : ""); //$NON-NLS-1$
			this.pkgLists.add(list);
		}
	}
	
	public RPkgList<T> getOrAdd(final String source) {
		RPkgList<T> list= getBySource(source);
		if (list == null) {
			list= new RPkgList<>(4);
			add(source, list);
		}
		return list;
	}
	
	
	@Override
	public List<String> getNames() {
		if (this.names == null) {
			createNames();
		}
		return this.names;
	}
	
	private synchronized void createNames() {
		if (this.names != null) {
			return;
		}
		if (this.pkgLists.isEmpty()) {
			this.names= Collections.emptyList();
			return;
		}
		{	final SortedArraySet<String> names= new SortedArraySet<>(
					new String[Math.min(16, this.pkgLists.get(0).size())], 0, RPkgUtil.COLLATOR );
			for (final RPkgList<? extends IRPkg> list : this.pkgLists) {
				for (int i= 0; i < list.size(); i++) {
					names.addE(list.get(i).getName());
				}
			}
			this.names= Collections.unmodifiableList(names);
		}
	}
	
	@Override
	public boolean containsByName(final String name) {
		for (final RPkgList<? extends IRPkg> list : this.pkgLists) {
			final int idx= list.indexOf(name);
			if (idx >= 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public T getFirstByName(final String name) {
		for (final RPkgList<T> list : this.pkgLists) {
			final int idx= list.indexOf(name);
			if (idx >= 0) {
				return list.get(idx);
			}
		}
		return null; 
	}
	
	@Override
	public List<T> getByName(final String name) {
		List<T> result= null;
		for (final RPkgList<T> list : this.pkgLists) {
			final int idx= list.indexOf(name);
			if (idx >= 0) {
				if (result == null) {
					result= new ArrayList<>(2);
				}
				result.add(list.get(idx));
			}
		}
		return (result != null) ? result : Collections.<T>emptyList(); 
	}
	
	@Override
	public List<String> getSources() {
		return this.sources;
	}
	
	@Override
	public RPkgList<T> getBySource(final String source) {
		final int idx= this.sources.indexOf(source);
		return (idx >= 0) ? this.pkgLists.get(idx) : null;
	}
	
	@Override
	public List<RPkgList<T>> getAll() {
		return this.pkgLists;
	}
	
}
