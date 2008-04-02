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

import de.walware.statet.r.core.rsource.ast.RAstNode;


final class SubNamedSlotInFunElementAccess extends SubAbstractElementAccess {
	
	
	final RAstNode fSlotNameNode;
	
	
	SubNamedSlotInFunElementAccess(final ElementAccess root, final RAstNode slotNameNode) {
		fRoot = root;
		fSlotNameNode = slotNameNode;
	}
	
	
	public final int getType() {
		return SUB_NAMEDSLOT;
	}
	
	public final String getName() {
		return fSlotNameNode.getText();
	}
	
	public final RAstNode getNode() {
		return fRoot.getNode();
	}
	
	public final RAstNode getNameNode() {
		return fSlotNameNode;
	}
	
}
