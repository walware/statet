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
import java.util.Iterator;
import java.util.List;

import de.walware.statet.r.core.model.IElementAccess;
import de.walware.statet.r.core.model.IEnvirInSource;
import de.walware.statet.r.core.model.RElementName;


abstract class SubAbstractElementAccess extends RElementName implements IElementAccess {
	
	
	SubAbstractElementAccess fNextSub;
	ElementAccess fRoot;
	
	
	@Override
	public String getDisplayName() {
		return RElementName.createDisplayName(this);
	}
	
	public final IElementAccess getNextSegment() {
		return fNextSub;
	}
	
	public final IEnvirInSource getFrame() {
		return fRoot.getFrame();
	}
	
	public final boolean isWriteAccess() {
		return fRoot.isWriteAccess();
	}
	
	public IElementAccess[] getAllInUnit() {
		final List<ElementAccess> all = fRoot.fShared.entries;
		final List<IElementAccess> elements = new ArrayList<IElementAccess>();
		final Iterator<ElementAccess> iter = all.iterator();
		ITER_ACCESS: while (iter.hasNext()) {
			IElementAccess other = iter.next();
			IElementAccess me = fRoot;
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
		return elements.toArray(new IElementAccess[elements.size()]);
	}
	
}
