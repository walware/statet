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

import de.walware.rj.renv.IRPkg;

import de.walware.statet.r.core.pkgmanager.IRPkgCollection;


public class RPkgCollection<T extends IRPkg> implements IRPkgCollection<T> {
	
	
	private final List<String> fSources;
	private final List<RPkgList<T>> fLists;
	
	
	public RPkgCollection(final int size) {
		fSources = new ArrayList<String>(size);
		fLists = new ArrayList<RPkgList<T>>(size);
	}
	
	
	public void add(final String source, final RPkgList<T> list) {
		if (list != null) {
			fSources.add((source != null) ? source : ""); //$NON-NLS-1$
			fLists.add(list);
		}
	}
	
	public RPkgList<T> getOrAdd(final String source) {
		RPkgList<T> list = getBySource(source);
		if (list == null) {
			list = new RPkgList<T>(4);
			add(source, list);
		}
		return list;
	}
	
	
	@Override
	public boolean containsByName(final String name) {
		for (final RPkgList<? extends IRPkg> list : fLists) {
			final int idx = list.indexOf(name);
			if (idx >= 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public T getFirstByName(final String name) {
		for (final RPkgList<T> list : fLists) {
			final int idx = list.indexOf(name);
			if (idx >= 0) {
				return list.get(idx);
			}
		}
		return null; 
	}
	
	@Override
	public List<T> getByName(final String name) {
		List<T> result = null;
		for (final RPkgList<T> list : fLists) {
			final int idx = list.indexOf(name);
			if (idx >= 0) {
				if (result == null) {
					result = new ArrayList<T>(2);
				}
				result.add(list.get(idx));
			}
		}
		return (result != null) ? result : Collections.<T>emptyList(); 
	}
	
	@Override
	public List<String> getSources() {
		return fSources;
	}
	
	@Override
	public RPkgList<T> getBySource(final String source) {
		final int idx = fSources.indexOf(source);
		return (idx >= 0) ? fLists.get(idx) : null;
	}
	
	@Override
	public List<RPkgList<T>> getAll() {
		return fLists;
	}
	
	
	public void addNames(final RPkgNameList names) {
		for (final RPkgList<? extends IRPkg> list : fLists) {
			for (int i = 0; i < list.size(); i++) {
				names.add(list.get(i).getName());
			}
		}
	}
	
}
