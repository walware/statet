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
import java.util.Iterator;
import java.util.List;

import de.walware.statet.r.core.model.IElementAccess;
import de.walware.statet.r.core.model.IScope;


abstract class SubAbstractElementAccess implements IElementAccess {
	
	
	SubAbstractElementAccess fNextSub;
	ElementAccess fRoot;
	
	
	public final IElementAccess getSubElementAccess() {
		return fNextSub;
	}
	
	
	public final IScope getScope() {
		return fRoot.getScope();
	}
	
	public final boolean isWriteAccess() {
		return fRoot.isWriteAccess();
	}
	
	public IElementAccess[] getAllInUnit() {
		final ArrayList<ElementAccess> all = fRoot.fShared.entries;
		final List<IElementAccess> elements = new ArrayList<IElementAccess>();
		final Iterator<ElementAccess> iter = all.iterator();
		ITER_ACCESS: while (iter.hasNext()) {
			IElementAccess other = iter.next();
			IElementAccess me = fRoot;
			while (true) {
				me = me.getSubElementAccess();
				if (me == null || me.getName() == null) {
					return null;
				}
				other = other.getSubElementAccess();
				if (other == null || me.getType() != other.getType() 
						|| !me.getName().equals(other.getName())) {
					continue ITER_ACCESS;
				}
				if (me == this) {
					elements.add(other);
					continue ITER_ACCESS;
				}
			}
		}
		return elements.toArray(new IElementAccess[elements.size()]);
	}
	
}
