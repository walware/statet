/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.views.navigator.ResourceComparator;

import de.walware.ecommons.collections.SortedArraySet;
import de.walware.ecommons.collections.SortedListSet;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.workbench.search.ui.ExtTextSearchResult;
import de.walware.ecommons.workbench.search.ui.LineElement;
import de.walware.ecommons.workbench.search.ui.TextSearchResultTreeContentProvider;

import de.walware.statet.r.core.model.IRSourceUnit;


public class RElementSearchResultTreeContentProvider extends TextSearchResultTreeContentProvider<IRSourceUnit, RElementMatch> {
	
	
	private static class LevelComparator extends ResourceComparator implements Comparator<Object> {
		
		
		public LevelComparator() {
			super(NAME);
		}
		
		
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof ISourceUnit) {
				o1= ((ISourceUnit) o1).getResource();
			}
			if (o2 instanceof ISourceUnit) {
				o2= ((ISourceUnit) o2).getResource();
			}
			return compare(null, o1, o2);
		}
		
	}
	
	
	protected static final int BY_PROJECT= 1;
	protected static final int BY_SOURCE_UNIT= 5;
	
	
	private int level= BY_PROJECT;
	
	private final Map<Object, SortedListSet<Object>> levelChildren= new HashMap<Object, SortedListSet<Object>>();
	
	
	public RElementSearchResultTreeContentProvider(final RElementSearchResultPage page, final TreeViewer viewer) {
		super(page, viewer);
	}
	
	
	@Override
	protected void reset() {
		super.reset();
		this.levelChildren.clear();
	}
	
	public void setLevel(final int level) {
		this.level= level;
		clear();
	}
	
	@Override
	protected int getElementLimit() {
		if (this.level < BY_SOURCE_UNIT) {
			return Integer.MAX_VALUE;
		}
		return super.getElementLimit();
	}
	
	@Override
	public Object[] getElements(final Object inputElement) {
		if (this.level < BY_SOURCE_UNIT) {
			if (!this.active) {
				final ExtTextSearchResult<IRSourceUnit, RElementMatch> result= getInput();
				assert (inputElement == result);
				if (result == null) {
					return NO_ELEMENTS;
				}
				assert (this.levelChildren.isEmpty());
				
				final IRSourceUnit[] elements= result.getElements();
				for (int i= 0; i < elements.length; i++) {
					doAdd(null, elements[i]);
				}
				this.active= true;
			}
			final SortedListSet<Object> children= this.levelChildren.get(null);
			return (children != null) ? children.toArray() : NO_ELEMENTS;
		}
		return super.getElements(inputElement);
	}
	
	@Override
	public Object getParent(final Object element) {
		if (element instanceof ISourceUnit) {
			if (this.level < BY_SOURCE_UNIT) {
				final IFile file= (IFile) ((ISourceUnit) element).getResource();
				return file.getParent();
			}
		}
		if (element instanceof IResource) {
			if (element instanceof IProject) {
				return null;
			}
			return ((IResource) element).getParent();
		}
		if (element instanceof LineElement) {
			return ((LineElement<?>) element).getElement();
		}
		return super.getParent(element);
	}
	
	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IResource) {
			final SortedListSet<Object> children= this.levelChildren.get(parentElement);
			return (children != null) ? children.toArray() : NO_ELEMENTS;
		}
		return super.getChildren(parentElement);
	}
	
	@Override
	public Object[] getShownMatches(final RElementMatch[] matches) {
		final List<LineElement<?>> groups= new ArrayList<LineElement<?>>();
		LineElement<?> lastGroup= null;
		for (int i= 0; i < matches.length; i++) {
			final LineElement<?> group= matches[i].getMatchGroup();
			if (group != lastGroup) {
				groups.add(group);
				lastGroup= group;
			}
		}
		return groups.toArray();
	}
	
	
	@Override
	protected void addElement(final TreeViewer viewer, final IRSourceUnit element, final int idx) {
		if (this.level < BY_SOURCE_UNIT) {
			doAdd(viewer, element);
		}
		else {
			super.addElement(viewer, element, idx);
		}
	}
	
	protected void doAdd(final TreeViewer viewer, final Object element) {
		final Object parent= getParent(element);
		SortedListSet<Object> children= this.levelChildren.get(parent);
		if (children == null) {
			if (parent != null) {
				doAdd(viewer, parent);
			}
			children= new SortedArraySet<Object>(NO_ELEMENTS, new LevelComparator());
			this.levelChildren.put(parent, children);
		}
		final int idx;
		if ((idx= children.addE(element)) >= 0) {
			if (viewer != null) {
				viewer.insert((parent != null) ? parent : TreePath.EMPTY, element, idx);
			}
		}
	}
	
	@Override
	protected void removeElement(final TreeViewer viewer, final IRSourceUnit element, final int idx) {
		if (this.level < BY_SOURCE_UNIT) {
			doRemove(viewer, element);
		}
		else {
			super.removeElement(viewer, element, idx);
		}
	}
	
	protected void doRemove(final TreeViewer viewer, final Object element) {
		final Object parent= getParent(element);
		final SortedListSet<Object> children= this.levelChildren.get(parent);
		if (children == null) {
			return;
		}
		if (children.removeE(element) >= 0) {
			if (children.isEmpty() && parent != null) {
				doRemove(viewer, parent);
			}
			else if (viewer != null) {
				viewer.remove(element);
			}
		}
	}
	
}
