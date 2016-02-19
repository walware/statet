/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import de.walware.rj.renv.IRPkg;


class DetailGroup {
	
	
	public static abstract class ContentProvider implements ITreeContentProvider {
		
		
		protected final DetailGroup[] fGroups;
		
		
		public ContentProvider(final int num) {
			fGroups = new DetailGroup[num];
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(final Object inputElement) {
			return fGroups;
		}
		
		@Override
		public Object getParent(final Object element) {
			return null;
		}
		
		@Override
		public boolean hasChildren(final Object element) {
			return (element instanceof DetailGroup && !((DetailGroup) element).getList().isEmpty());
		}
		@Override
		public Object[] getChildren(final Object parentElement) {
			return ((DetailGroup) parentElement).getList().toArray();
		}
		
	}
	
	
	private final int fId;
	
	private final String fLabel;
	
	private List<? extends IRPkg> fList = Collections.emptyList();
	
	
	public DetailGroup(final int id, final String label) {
		fId = id;
		fLabel = label;
	}
	
	
	public int getId() {
		return fId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public void setList(final List<? extends IRPkg> list) {
		fList = list;
	}
	
	public void clearList() {
		fList = Collections.emptyList();
	}
	
	public List<? extends IRPkg> getList() {
		return fList;
	}
	
}
