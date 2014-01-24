/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.walware.ecommons.ltk.IModelElement;

import de.walware.statet.r.core.model.IRLangSourceElement;


public class RSourceElements {
	
	static final List<? extends IRLangSourceElement> NO_R_SOURCE_CHILDREN = Collections.emptyList();
	
	static final List<? extends IRLangSourceElement> getChildren(final List<? extends IRLangSourceElement> children, final IModelElement.Filter filter) {
		if (filter == null) {
			return children;
		}
		else {
			final ArrayList<IRLangSourceElement> filtered = new ArrayList<IRLangSourceElement>(children.size());
			for (final IRLangSourceElement child : children) {
				if (filter.include(child)) {
					filtered.add(child);
				}
			}
			return filtered;
		}
	}
	
	static final boolean hasChildren(final List<? extends IRLangSourceElement> children, final IModelElement.Filter filter) {
		if (filter == null) {
			return (!children.isEmpty());
		}
		else {
			for (final IRLangSourceElement child : children) {
				if (filter.include(child)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
