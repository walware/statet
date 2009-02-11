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

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SubNamed;


final class SubNamedSlotSyntacticElementAccess extends SubAbstractElementAccess {
	
	
	final SubNamed fNode;
	
	
	SubNamedSlotSyntacticElementAccess(final ElementAccess root, final SubNamed node) {
		fRoot = root;
		fNode = node;
	}
	
	
	public int getType() {
		return RElementName.SUB_NAMEDSLOT;
	}
	
	public final String getSegmentName() {
		return fNode.getSubnameChild().getText();
	}
	
	public final RAstNode getNode() {
		return fNode;
	}
	
	public final RAstNode getNameNode() {
		return fNode.getSubnameChild();
	}
	
}
