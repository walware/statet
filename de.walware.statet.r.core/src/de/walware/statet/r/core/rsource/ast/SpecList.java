/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import java.util.ArrayList;
import java.util.List;

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;


/**
 * Comma separated list
 */
abstract class SpecList extends RAstNode {
	
	
	List<SpecItem> fSpecs = new ArrayList<SpecItem>(0);
	

	@Override
	public final boolean hasChildren() {
		return (!fSpecs.isEmpty());
	}
	
	@Override
	public final int getChildCount() {
		return fSpecs.size();
	}
	
	@Override
	public final RAstNode getChild(int index) {
		return fSpecs.get(index);
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return fSpecs.toArray(new RAstNode[fSpecs.size()]);
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		for (int i = fSpecs.size()-1; i >= 0; i--) {
			if (fSpecs.get(i) == child) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		acceptChildren(visitor, fSpecs);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		acceptChildren(visitor, fSpecs);
	}


	@Override
	final Expression getExpr(RAstNode child) {
		return null;
	}

	@Override
	final Expression getLeftExpr() {
		return null;
	}
	
	@Override
	final Expression getRightExpr() {
		return null;
	}
	
	@Override
	final void updateStopOffset() {
	}

	
	abstract SpecItem createItem();
	
	void appendItem(SpecItem item) {
		fSpecs.add(item);
	}
}