/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.walware.statet.r.core.rmodel.IScope;


public class Scope implements IScope {
	
	
	public static String createId(final int type, final String name) {
		return Integer.toHexString(type)+':'+name;
	}
	
	
	static class ElementAccessList {
		
		final String name;
		final ArrayList<ElementAccess> entries;
		IScope scope;
		
		public ElementAccessList(final String name) {
			assert (name != null);
			this.name = name;
			this.entries = new ArrayList<ElementAccess>();
		}
		
	}
	
	
	private Map<String, ElementAccessList> fData;
	private Map<String, ElementAccessList> fClasses;
	private int fType;
	private String fId;
	private Scope[] fParents;
	
	private Map<String, ElementAccessList> fLateWrite;
	private Map<String, ElementAccessList> fLateRead;
	
	Scope(final int type, final String id, final Scope[] parents) {
		fType = type;
		fId = id;
		fParents = parents;
		fData = new HashMap<String, ElementAccessList>();
		fLateWrite = new HashMap<String, ElementAccessList>();
		fLateRead = new HashMap<String, ElementAccessList>();
	}
	
	void add(final String name, final ElementAccess access) {
		ElementAccessList detail = fData.get(name);
		if (detail == null) {
			detail = new ElementAccessList(name);
			detail.scope = this;
			fData.put(name, detail);
		}
		detail.entries.add(access);
		access.fShared = detail;
		
		access.fFullNode.addAttachment(access);
	}
	
	void addLateResolve(final String name, final ElementAccess access) {
		ElementAccessList detail = fData.get(name);
		if (detail == null) {
			final Map<String, ElementAccessList> late = ((access.fFlags & ElementAccess.A_WRITE) != 0) ?
					fLateWrite : fLateRead;
			detail = late.get(name);
			if (detail == null) {
				detail = new ElementAccessList(name);
				late.put(name, detail);
			}
		}
		detail.entries.add(access);
		access.fShared = detail;
		
		access.fFullNode.addAttachment(access);
	}
	
	void addClass(final String name, final ElementAccess access) {
		if (fClasses == null) {
			fClasses = new HashMap<String, ElementAccessList>();
		}
		ElementAccessList detail = fClasses.get(name);
		if (detail == null) {
			detail = new ElementAccessList(name);
			detail.scope = this;
			fClasses.put(name, detail);
		}
		detail.entries.add(access);
		access.fShared = detail;
		
		access.fFullNode.addAttachment(access);
	}
	
	void runLateResolve(final boolean onlyWrite) {
		final Scope[] searchList = createSearchList();
		
		Map<String, ElementAccessList> map = fLateWrite;
		if (map != null) {
			final IScope defaultScope = this;
			ITER_NAMES : for (final ElementAccessList detail : map.values()) {
				for (int i = 0; i < searchList.length; i++) {
					final ElementAccessList exist = searchList[i].fData.get(detail.name);
					if (exist != null) {
						for (final ElementAccess access : detail.entries) {
							access.fShared = exist;
						}
						exist.entries.addAll(detail.entries);
						continue ITER_NAMES;
					}
				}
				detail.scope = defaultScope;
				fData.put(detail.name, detail);
				continue ITER_NAMES;
			}
			fLateWrite = null;
		}
		
		if (onlyWrite) {
			return;
		}
		
		map = fLateRead;
		if (map != null) {
			Scope defaultScope = this;
			for (int i = 0; i < searchList.length; i++) {
				if (searchList[i].fType == T_PROJ) {
					defaultScope = searchList[i];
					break;
				}
			}
			ITER_NAMES : for (final ElementAccessList detail : map.values()) {
				for (int i = 0; i < searchList.length; i++) {
					final ElementAccessList exist = searchList[i].fData.get(detail.name);
					if (exist != null) {
						for (final ElementAccess access : detail.entries) {
							access.fShared = exist;
						}
						exist.entries.addAll(detail.entries);
						continue ITER_NAMES;
					}
				}
				detail.scope = defaultScope;
				defaultScope.fData.put(detail.name, detail);
				continue ITER_NAMES;
			}
			fLateRead = null;
		}
	}
	
	private Scope[] createSearchList() {
		final ArrayList<Scope> list = new ArrayList<Scope>();
		int idx = 0;
		list.add(this);
		while (idx < list.size()) {
			final Scope[] p = list.get(idx++).fParents;
			for (int i = 0; i < p.length; i++) {
				if (!list.contains(p[i])) {
					list.add(p[i]);
				}
			}
		}
		return list.toArray(new Scope[list.size()]);
	}
	
	public String getId() {
		return createId(fType, fId);
	}
	
	public boolean containsElement(final String name) {
		return fData.containsKey(name);
	}
	
}
