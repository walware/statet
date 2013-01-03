/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


final class SubNamedSlotInFunElementAccess extends SubAbstractElementAccess {
	
	
	final RAstNode fSlotNameNode;
	
	
	SubNamedSlotInFunElementAccess(final ElementAccess root, final RAstNode slotNameNode) {
		fRoot = root;
		fSlotNameNode = slotNameNode;
	}
	
	
	@Override
	public final int getType() {
		return RElementName.SUB_NAMEDSLOT;
	}
	
	@Override
	public final String getSegmentName() {
		return fSlotNameNode.getText();
	}
	
	@Override
	public final RAstNode getNode() {
		return fRoot.getNode();
	}
	
	@Override
	public final RAstNode getNameNode() {
		return fSlotNameNode;
	}
	
}
