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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;


abstract class SubAbstractElementAccess extends RElementAccess {
	
	
	SubAbstractElementAccess fNextSub;
	ElementAccess fRoot;
	
	
	@Override
	public String getDisplayName() {
		return RElementName.createDisplayName(this, 0);
	}
	
	@Override
	public RElementName getNamespace() {
		return null;
	}
	
	@Override
	public final RElementAccess getNextSegment() {
		return fNextSub;
	}
	
	@Override
	public final IRFrame getFrame() {
		return fRoot.getFrame();
	}
	
	@Override
	public final boolean isWriteAccess() {
		return fRoot.isWriteAccess();
	}
	
	@Override
	public boolean isMethodAccess() {
		return fRoot.isMethodAccess();
	}
	
	@Override
	public RElementAccess[] getAllInUnit() {
		final List<ElementAccess> all = fRoot.fShared.entries;
		final List<RElementAccess> elements = new ArrayList<RElementAccess>();
		final Iterator<ElementAccess> iter = all.iterator();
		ITER_ACCESS: while (iter.hasNext()) {
			RElementAccess other = iter.next();
			RElementAccess me = fRoot;
			while (true) {
				me = me.getNextSegment();
				if (me == null || me.getSegmentName() == null) {
					return null;
				}
				other = other.getNextSegment();
				if (other == null || me.getType() != other.getType() 
						|| !me.getSegmentName().equals(other.getSegmentName())) {
					continue ITER_ACCESS;
				}
				if (me == this) {
					elements.add(other);
					continue ITER_ACCESS;
				}
			}
		}
		return elements.toArray(new RElementAccess[elements.size()]);
	}
	
}
