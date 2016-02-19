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

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.walware.rj.data.RStore;

import de.walware.statet.r.internal.ui.intable.InfoString;


public class RStoreContentProvider implements IStructuredContentProvider {
	
	
	private RStore fStore;
	
	
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		fStore = (RStore) newInput;
	}
	
	@Override
	public Object[] getElements(final Object inputElement) {
		final Object[] array = new Object[(int) fStore.getLength()];
		for (int idx = 0; idx < array.length; idx++) {
			if (fStore.isNA(idx)) {
				array[idx] = InfoString.NA;
			}
			else {
				array[idx] = fStore.get(idx);
			}
		}
		return array;
	}
	
	@Override
	public void dispose() {
		fStore = null;
	}
	
}
