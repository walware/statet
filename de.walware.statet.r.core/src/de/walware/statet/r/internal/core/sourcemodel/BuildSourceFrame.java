/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ltk.IModelElement.Filter;

import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRFrameInSource;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;


abstract class BuildSourceFrame implements IRFrameInSource {
	
	
	static final int CREATED_NO = 0;
	static final int CREATED_SEARCH = 1;
	static final int CREATED_RESOLVED = 2;
	static final int CREATED_EXPLICIT = 3;
	static final int CREATED_IMPORTED = 4;
	
	
	private static final ConstList<BuildSourceFrame> NO_PARENTS = new ConstList<BuildSourceFrame>();
	
	
	public static String createId(final int type, final String name, final int alt) {
		if (type == IRFrame.PACKAGE && name != null) {
			return "package:"+name;
		}
		return (name != null) ? 
				Integer.toHexString(type)+":`"+name+'`' : //$NON-NLS-1$
				Integer.toHexString(type)+":#"+Integer.toHexString(alt); //$NON-NLS-1$
	}
	
	static class ElementAccessList {
		
		final String name;
		final List<ElementAccess> entries;
		IRFrame frame;
		int isCreated;
		
		public ElementAccessList(final String name) {
			this.name = name;
			this.entries = new ArrayList<ElementAccess>(4);
			this.isCreated = CREATED_NO;
		}
		
		
		public void postAdd(final ElementAccess access) {
			access.fShared = this;
			entries.add(access);
			access.fFullNode.addAttachment(access);
		}
	}
	
	static class RunScope extends BuildSourceFrame {
		
		
		public RunScope(final int type, final String id, final BuildSourceFrame parent) {
			super(type, id, new BuildSourceFrame[] { parent });
		}
		
		
		public RElementName getElementName() {
			return null;
		}
		
		
		@Override
		public void add(final String name, final ElementAccess access) {
			fParents.get(0).add(name, access);
		}
		
		@Override
		public void addLateResolve(final String name, final ElementAccess access) {
			fParents.get(0).addLateResolve(name, access);
		}
		
		@Override
		public void addRunResolve(final String name, final ElementAccess access) {
			ElementAccessList detail = fData.get(name);
			if (detail == null) {
				detail = new ElementAccessList(name);
				detail.frame = this;
				fData.put(name, detail);
			}
			detail.entries.add(access);
			if (access.isWriteAccess() && !access.isDeletion()) {
				detail.isCreated = CREATED_EXPLICIT;
			}
			else if (access.isImport()) {
				detail.isCreated = CREATED_IMPORTED;
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
	
	static class DefScope extends BuildSourceFrame {
		
		private Map<String, ElementAccessList> fLateWrite;
		private Map<String, ElementAccessList> fLateRead;
		
		private Map<String, ElementAccessList> fClasses;
		
		private RElementName fElementName;
		
		
		DefScope(final int type, final String id, final String name, final BuildSourceFrame[] parents) {
			super(type, id, parents);
			fLateWrite = new HashMap<String, ElementAccessList>();
			fLateRead = new HashMap<String, ElementAccessList>();
			switch (type) {
			case PROJECT:
				fElementName = null;
//				fElementName = RElementName.create(RElementName.MAIN_SEARCH_ENV, ".GlobalEnv");
				fClasses = new HashMap<String, ElementAccessList>();
				break;
			case PACKAGE:
				fElementName = RElementName.create(RElementName.MAIN_PACKAGE, name);
				fClasses = new HashMap<String, ElementAccessList>();
				break;
			default:
				fClasses = null;
			}
		}
		
		
		public RElementName getElementName() {
			return fElementName;
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
				detail.isCreated = CREATED_EXPLICIT;
			}
			else if (access.isImport()) {
				detail.isCreated = CREATED_IMPORTED;
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
			if (detail != null && detail.isCreated <= CREATED_NO) {
				detail = null;
			}
			if (detail == null) {
				final Map<String, ElementAccessList> late = 
						((access.fFlags & (0xf | ElementAccess.A_SUB)) == ElementAccess.A_WRITE) ?
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
				fParents.get(0).addClass(name, access);
				return;
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
			final BuildSourceFrame[] searchList = createSearchList();
			
			Map<String, ElementAccessList> map = fLateWrite;
			if (map != null) {
				final IRFrame defaultScope = this;
				ITER_NAMES : for (final ElementAccessList detail : map.values()) {
					for (int requiredCreation = CREATED_SEARCH; requiredCreation >= 0; requiredCreation--) {
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
					detail.isCreated = CREATED_SEARCH;
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
				BuildSourceFrame defaultScope = this;
				for (int i = 0; i < searchList.length; i++) {
					if (searchList[i].fType <= IRFrame.PACKAGE) { // package or project
						defaultScope = searchList[i];
						break;
					}
				}
				ITER_NAMES : for (final ElementAccessList detail : map.values()) {
					for (int requiredCreation = CREATED_SEARCH; requiredCreation >= 0; requiredCreation--) {
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
	ConstList<BuildSourceFrame> fParents;
	private List<IBuildSourceFrameElement> fElements = Collections.emptyList();
	private WeakReference<List<IRLangSourceElement>> fModelChildren;
	
	
	BuildSourceFrame(final int type, final String id, final BuildSourceFrame[] parents) {
		fType = type;
		fId = id;
		if (parents != null) {
			fParents = new ConstList<BuildSourceFrame>(parents);
		}
		else {
			fParents = NO_PARENTS;
		}
		fData = new HashMap<String, ElementAccessList>();
	}
	
	
	void addFrameElement(final IBuildSourceFrameElement element) {
		final int length = fElements.size();
		final IBuildSourceFrameElement[] elements = new IBuildSourceFrameElement[length+1];
		for (int i = 0; i < length; i++) {
			elements[i] = fElements.get(i);
		}
		elements[length] = element;
		fElements = new ConstList<IBuildSourceFrameElement>(elements);
	}
	
	abstract void add(final String name, final ElementAccess access);
	abstract void addLateResolve(final String name, final ElementAccess access);
	abstract void addRunResolve(final String name, final ElementAccess access);
	abstract void addClass(final String name, final ElementAccess access);
	
	abstract void runLateResolve(final boolean onlyWrite);
	
	
	protected BuildSourceFrame[] createSearchList() {
		final ArrayList<BuildSourceFrame> list = new ArrayList<BuildSourceFrame>();
		int idx = 0;
		list.add(this);
		while (idx < list.size()) {
			final List<BuildSourceFrame> ps = list.get(idx++).fParents;
			for (final BuildSourceFrame p : ps) {
				if (!list.contains(p)) {
					list.add(p);
				}
			}
		}
		return list.toArray(new BuildSourceFrame[list.size()]);
	}
	
	public int getFrameType() {
		return fType;
	}
	
	public String getFrameId() {
		return fId;
	}
	
	public List<? extends IRFrame> getPotentialParents() {
		return fParents;
	}
	
	
	public Set<String> getAllAccessNames() {
		return Collections.unmodifiableSet(fData.keySet());
	}
	
	public List<? extends RElementAccess> getAllAccessOfElement(final String name) {
		final ElementAccessList list = fData.get(name);
		if (list == null) {
			return null;
		}
		return Collections.unmodifiableList(list.entries);
	}
	
	public boolean isResolved(final String name) {
		final ElementAccessList accessList = fData.get(name);
		return (accessList != null && accessList.isCreated >= CREATED_RESOLVED); 
	}
	
	
	public List<? extends IRElement> getModelElements() {
		return fElements;
	}
	
	public boolean hasModelChildren(final Filter<? super IRLangElement> filter) {
		for (final ElementAccessList list : fData.values()) {
			for (final ElementAccess access : list.entries) {
				if (access.fModelElement != null 
						&& (filter == null || filter.include(access.fModelElement)) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<? extends IRLangSourceElement> getModelChildren(final Filter filter) {
		if (fData.isEmpty()) {
			return RSourceElements.NO_R_SOURCE_CHILDREN;
		}
		List<IRLangSourceElement> children = (fModelChildren != null) ? fModelChildren.get() : null;
		if (children != null) {
			if (filter == null) {
				return children;
			}
			else {
				return RSourceElements.getChildren(children, filter);
			}
		}
		else {
			children = new ArrayList<IRLangSourceElement>();
			for (final ElementAccessList list : fData.values()) {
				for (final ElementAccess access : list.entries) {
					if (access.fModelElement != null 
							&& (filter == null || filter.include(access.fModelElement)) ) {
						children.add(access.fModelElement);
					}
				}
			}
			children = Collections.unmodifiableList(children);
			if (filter == null) {
				fModelChildren = new WeakReference<List<IRLangSourceElement>>(children);
			}
			return children;
		}
	}
	
}
