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
import de.walware.statet.r.core.rsource.ast.SubNamed;


final class SubNamedPartSyntacticElementAccess extends SubAbstractElementAccess {
	
	
	final SubNamed fNode;
	
	
	SubNamedPartSyntacticElementAccess(final ElementAccess root, final SubNamed node) {
		fRoot = root;
		fNode = node;
	}
	
	
	public int getType() {
		return SUB_NAMEDPART;
	}
	
	public final String getName() {
		return fNode.getSubnameChild().getText();
	}
	
	public final RAstNode getNode() {
		return fNode;
	}
	
	public final RAstNode getNameNode() {
		return fNode.getSubnameChild();
	}
	
}
