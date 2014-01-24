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

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SubNamed;


final class SubNamedPartSyntacticElementAccess extends SubAbstractElementAccess {
	
	
	final SubNamed fNode;
	
	
	SubNamedPartSyntacticElementAccess(final ElementAccess root, final SubNamed node) {
		fRoot = root;
		fNode = node;
	}
	
	
	@Override
	public int getType() {
		return RElementName.SUB_NAMEDPART;
	}
	
	@Override
	public final String getSegmentName() {
		return fNode.getSubnameChild().getText();
	}
	
	@Override
	public final RAstNode getNode() {
		return fNode;
	}
	
	@Override
	public final RAstNode getNameNode() {
		return fNode.getSubnameChild();
	}
	
}
