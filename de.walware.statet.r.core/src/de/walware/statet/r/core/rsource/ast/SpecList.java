/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * Comma separated list
 */
abstract class SpecList extends RAstNode {
	
	
	List<SpecItem> fSpecs = new ArrayList<SpecItem>(0);
	
	
	protected SpecList() {
	}
	
	
	@Override
	public final RTerminal getOperator(final int index) {
		return null;
	}
	
	
	@Override
	public final boolean hasChildren() {
		return (!fSpecs.isEmpty());
	}
	
	@Override
	public final int getChildCount() {
		return fSpecs.size();
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		return fSpecs.get(index);
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return fSpecs.toArray(new RAstNode[fSpecs.size()]);
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		for (int i = fSpecs.size()-1; i >= 0; i--) {
			if (fSpecs.get(i) == child) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		acceptChildren(visitor, fSpecs);
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		acceptChildren(visitor, fSpecs);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
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
	
	abstract SpecItem createItem();
	
	void appendItem(final SpecItem item) {
		fSpecs.add(item);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
	}
	
}
