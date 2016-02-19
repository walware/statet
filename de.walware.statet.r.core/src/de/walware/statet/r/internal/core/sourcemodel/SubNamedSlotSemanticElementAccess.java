/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class SubNamedSlotSemanticElementAccess extends SubAbstractElementAccess {
	
	
	private final RAstNode nameNode;
	
	
	SubNamedSlotSemanticElementAccess(final ElementAccess root, final RAstNode nameNode) {
		super(root);
		this.nameNode= nameNode;
	}
	
	
	@Override
	public final int getType() {
		return RElementName.SUB_NAMEDSLOT;
	}
	
	@Override
	public final String getSegmentName() {
		return this.nameNode.getText();
	}
	
	@Override
	public final RAstNode getNode() {
		return getRoot().getNode(); // ?
	}
	
	@Override
	public final RAstNode getNameNode() {
		return this.nameNode;
	}
	
}
