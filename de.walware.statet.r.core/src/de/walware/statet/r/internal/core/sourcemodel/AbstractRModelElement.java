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
import java.util.List;

import de.walware.statet.r.core.model.RModel;


public abstract class AbstractRModelElement implements IRLangElementWithSource {
	
	
	private List<IRLangElementWithSource> NO_CHILDREN = Arrays.asList(new IRLangElementWithSource[0]);
	
	
	List<? extends IRLangElementWithSource> fChildrenProtected = NO_CHILDREN;
	
	
	public boolean hasChildren(final Filter filter) {
		if (filter == null) {
			return (!fChildrenProtected.isEmpty());
		}
		else {
			for (int i = 0; i < fChildrenProtected.size(); i++) {
				if (filter.include(fChildrenProtected.get(i))) {
					return true;
				}
			}
			return false;
		}
	}
	
	public List<? extends IRLangElementWithSource> getChildren(final Filter filter) {
		if (filter == null) {
			return fChildrenProtected;
		}
		else {
			final ArrayList<IRLangElementWithSource> filtered = new ArrayList<IRLangElementWithSource>(fChildrenProtected.size());
			for (final IRLangElementWithSource child : fChildrenProtected) {
				if (filter.include(child)) {
					filtered.add(child);
				}
			}
			return filtered;
		}
	}
	
	
	public final String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
}
