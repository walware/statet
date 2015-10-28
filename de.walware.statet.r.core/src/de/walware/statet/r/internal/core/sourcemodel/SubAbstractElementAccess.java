/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;


abstract class SubAbstractElementAccess extends RElementAccess {
	
	
	private final ElementAccess root;
	
	SubAbstractElementAccess nextSegment;
	
	
	public SubAbstractElementAccess(final ElementAccess root) {
		this.root= root;
	}
	
	
	public final ElementAccess getRoot() {
		return this.root;
	}
	
	
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
		return nextSegment;
	}
	
	@Override
	public final IRFrame getFrame() {
		return root.getFrame();
	}
	
	@Override
	public final boolean isWriteAccess() {
		return root.isWriteAccess();
	}
	
	@Override
	public boolean isCallAccess() {
		return root.isCallAccess();
	}
	
	@Override
	public boolean isFunctionAccess() {
		return root.isFunctionAccess();
	}
	
	@Override
	public ImList<? extends RElementAccess> getAllInUnit(final boolean includeSlaves) {
		final List<ElementAccess> all= root.fShared.entries;
		final List<RElementAccess> elements= new ArrayList<>();
		ITER_ACCESS: for (RElementAccess element : all) {
			RElementAccess me= root;
			while (true) {
				me= me.getNextSegment();
				if (me.getSegmentName() == null) {
					return null;
				}
				element= element.getNextSegment();
				if (element == null || me.getType() != element.getType() 
						|| !me.getSegmentName().equals(element.getSegmentName())) {
					continue ITER_ACCESS;
				}
				if (me == this) {
					if (includeSlaves || element.isMaster()) {
						elements.add(element);
					}
					continue ITER_ACCESS;
				}
			}
		}
		Collections.sort(elements, RElementAccess.NAME_POSITION_COMPARATOR);
		return ImCollections.toList(elements);
	}
	
}
