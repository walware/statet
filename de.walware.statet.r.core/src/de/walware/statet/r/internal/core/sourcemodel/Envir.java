/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceUnit;

import de.walware.statet.r.core.model.IElementAccess;
import de.walware.statet.r.core.model.IFrame;
import de.walware.statet.r.core.model.IFrameInSource;


abstract class Envir implements IFrameInSource {
	
	
	public static String createId(final int type, final String name, final int alt) {
		return (name != null) ? 
				Integer.toHexString(type)+":`"+name+'`' : //$NON-NLS-1$
				Integer.toHexString(type)+":#"+Integer.toHexString(alt); //$NON-NLS-1$
	}
	
	static class ElementAccessList {
		
		final String name;
		final List<ElementAccess> entries;
		IFrame frame;
		int isCreated; // 0=no, 1=search, 2=explicit 
		
		public ElementAccessList(final String name) {
			this.name = name;
			this.entries = new ArrayList<ElementAccess>(4);
			this.isCreated = 0;
		}
		
	}
	
	static class RunScope extends Envir {
		
		
		public RunScope(final int type, final String id, final Envir parent) {
			super(type, id, new Envir[] { parent });
		}
		
		@Override
		void add(final String name, final ElementAccess access) {
			fParents.get(0).add(name, access);
		}
		
		@Override
		void addLateResolve(final String name, final ElementAccess access) {
			fParents.get(0).addLateResolve(name, access);
		}
		
		@Override
		void addRunResolve(final String name, final ElementAccess access) {
			ElementAccessList detail = fData.get(name);
			if (detail == null) {
				detail = new ElementAccessList(name);
				detail.frame = this;
				fData.put(name, detail);
			}
			detail.entries.add(access);
			if (access.isWriteAccess() && !access.isDeletion()) {
				detail.isCreated = 2;
			}
			access.fShared = detail;
			
			access.fFullNode.addAttachment(access);
		}
		
		@Override
		void addClass(final String name, final ElementAccess access) {
			fParents.get(0).addClass(name, access);
		}
		
		@Override
		void runLateResolve(final boolean onlyWrite) {
		}
		
	}
	
	static class DefScope extends Envir {
		
		private Map<String, ElementAccessList> fLateWrite;
		private Map<String, ElementAccessList> fLateRead;
		
		private Map<String, ElementAccessList> fClasses;
		
		
		DefScope(final int type, final String id, final Envir[] parents) {
			super(type, id, parents);
			fLateWrite = new HashMap<String, ElementAccessList>();
			fLateRead = new HashMap<String, ElementAccessList>();
		}
		
		
		@Override
		void add(final String name, final ElementAccess access) {
			ElementAccessList detail = fData.get(name);
			if (detail == null) {
				detail = new ElementAccessList(name);
				detail.frame = this;
				fData.put(name, detail);
			}
			detail.entries.add(access);
			if (access.isWriteAccess() && !access.isDeletion()) {
				detail.isCreated = 2;
			}
			access.fShared = detail;
			
			access.fFullNode.addAttachment(access);
		}
		
		@Override
		void addLateResolve(final String name, final ElementAccess access) {
			if (name == null) {
				add(null, access);
				return;
			}
			ElementAccessList detail = fData.get(name);
			if (detail != null && detail.isCreated <= 0) {
				detail = null;
			}
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
		
		@Override
		void addClass(final String name, final ElementAccess access) {
			if (fClasses == null) {
				fClasses = new HashMap<String, ElementAccessList>();
			}
			ElementAccessList detail = fClasses.get(name);
			if (detail == null) {
				detail = new ElementAccessList(name);
				detail.frame = this;
				fClasses.put(name, detail);
			}
			detail.entries.add(access);
			access.fShared = detail;
			
			access.fFullNode.addAttachment(access);
		}
		
		@Override
		void addRunResolve(final String name, final ElementAccess access) {
		}
		
		@Override
		void runLateResolve(final boolean onlyWrite) {
			final Envir[] searchList = createSearchList();
			
			Map<String, ElementAccessList> map = fLateWrite;
			if (map != null) {
				final IFrame defaultScope = this;
				ITER_NAMES : for (final ElementAccessList detail : map.values()) {
					for (int requiredCreation = 1; requiredCreation >= 0; requiredCreation--) {
						for (int i = 0; i < searchList.length; i++) {
							final ElementAccessList exist = searchList[i].fData.get(detail.name);
							if (exist != null && exist.isCreated >= requiredCreation) {
								for (final ElementAccess access : detail.entries) {
									access.fShared = exist;
								}
								exist.entries.addAll(detail.entries);
								continue ITER_NAMES;
							}
						}
					}
					detail.frame = defaultScope;
					detail.isCreated = 1;
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
				Envir defaultScope = this;
				for (int i = 0; i < searchList.length; i++) {
					if (searchList[i].fType == T_PROJ) {
						defaultScope = searchList[i];
						break;
					}
				}
				ITER_NAMES : for (final ElementAccessList detail : map.values()) {
					for (int requiredCreation = 1; requiredCreation >= 0; requiredCreation--) {
						for (int i = 0; i < searchList.length; i++) {
							final ElementAccessList exist = searchList[i].fData.get(detail.name);
							if (exist != null && exist.isCreated >= requiredCreation) {
								for (final ElementAccess access : detail.entries) {
									access.fShared = exist;
								}
								exist.entries.addAll(detail.entries);
								continue ITER_NAMES;
							}
						}
					}
					detail.frame = defaultScope;
					defaultScope.fData.put(detail.name, detail);
					continue ITER_NAMES;
				}
				fLateRead = null;
			}
		}
		
	}
	
	
	protected final Map<String, ElementAccessList> fData;
	protected final int fType;
	protected final String fId;
	protected final List<Envir> fParents;
	
	private IModelElement fModelElement;
	
	
	Envir(final int type, final String id, final Envir[] parents) {
		fType = type;
		fId = id;
		fParents = new ArrayList<Envir>(parents.length);
		fParents.addAll(Arrays.asList(parents));
		fData = new HashMap<String, ElementAccessList>();
	}
	
	
	abstract void add(final String name, final ElementAccess access);
	abstract void addLateResolve(final String name, final ElementAccess access);
	abstract void addRunResolve(final String name, final ElementAccess access);
	abstract void addClass(final String name, final ElementAccess access);
	
	abstract void runLateResolve(final boolean onlyWrite);
	
	public String getName() {
		return null;
	}
	
	void setModelElement(final IModelElement element) {
		fModelElement = element;
	}
	
	public IModelElement getModelElement() {
		return fModelElement;
	}
	
	protected Envir[] createSearchList() {
		final ArrayList<Envir> list = new ArrayList<Envir>();
		int idx = 0;
		list.add(this);
		while (idx < list.size()) {
			final List<Envir> ps = list.get(idx++).fParents;
			for (final Envir p : ps) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		return list.toArray(new Envir[list.size()]);
	}
	
	public ISourceUnit getSourceUnit() {
		return null;
	}
	
	public int getFrameType() {
		return fType;
	}
	
	public String getId() {
		return fId;
	}
	
	public List<Envir> getUnderneathEnvirs() {
		return fParents;
	}
	
	
	public boolean containsElement(final String name) {
		return fData.containsKey(name);
	}
	
	public Set<String> getElementNames() {
		return Collections.unmodifiableSet(fData.keySet());
	}
	
	public List<? extends IElementAccess> getAllAccessOfElement(final String name) {
		final ElementAccessList list = fData.get(name);
		if (list == null) {
			return null;
		}
		return Collections.unmodifiableList(list.entries);
	}
	
	public Object getAdapter(final Class adapter) {
		return null;
	}
	
//	@Override
//	public String toString() {
//		switch (fType) {
//		case IScope.T_PKG:
//			return "package:"+id;
//		case IScope.T_PROJ:
//			return ".GlobalEnv";
//		case IScope.T_CLASS:
//			return "class:"+id;
//		case IScope.T_FUNCTION:
//			return "function:"+id;
//		case IScope.T_EXPLICIT:
//			return "env:"+id;
//		}
//		return getId();
//	}
	
}
