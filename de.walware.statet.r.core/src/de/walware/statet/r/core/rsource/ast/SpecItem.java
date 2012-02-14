/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * 
 */
abstract class SpecItem extends RAstNode {
	
	
	RAstNode fArgName;
	int fEqualsOffset = Integer.MIN_VALUE;
	final Expression fValueExpr = new Expression();
	
	
	protected SpecItem() {
	}
	
	
	@Override
	public final RTerminal getOperator(final int index) {
		return null;
	}
	
	
	@Override
	public final boolean hasChildren() {
		return (fArgName != null || fValueExpr.node != null);
	}
	
	@Override
	public final int getChildCount() {
		int count = (fArgName != null) ? 1 : 0;
		if (fValueExpr.node != null) {
			count++;
		}
		return count;
	}
	
	public boolean hasName() {
		return (fArgName != null);
	}
	
	public final RAstNode getNameChild() {
		return fArgName;
	}
	
	public boolean hasValue() {
		return (fValueExpr.node != null);
	}
	
	public final RAstNode getValueChild() {
		return fValueExpr.node;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		if (fArgName != null) {
			switch (index) {
			case 0:
				return fArgName;
			case 1:
				if (fValueExpr != null) {
					return fValueExpr.node;
				}
				//$FALL-THROUGH$
			default:
				throw new IndexOutOfBoundsException();
			}
		}
		else {
			if (index == 0 && fValueExpr.node != null) {
				return fValueExpr.node;
			}
			else {
				throw new IndexOutOfBoundsException();
			}
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		if (fArgName != null) {
			if (fValueExpr.node != null) {
				return new RAstNode[] { fArgName, fValueExpr.node };
			}
			else {
				return new RAstNode[] { fArgName };
			}
		}
		else if (fValueExpr.node != null) {
			return new RAstNode[] { fValueExpr.node };
		}
		else {
			return NO_CHILDREN;
		}
	}
	
	@Override
	public int getChildIndex(final IAstNode child) {
		if (fArgName == child) {
			return 0;
		}
		if (fValueExpr.node == child) {
			return 1;
		}
		return -1;
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		if (fArgName != null) {
			fArgName.acceptInR(visitor);
		}
		if (fValueExpr.node != null) {
			fValueExpr.node.acceptInR(visitor);
		}
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		if (fArgName != null) {
			visitor.visit(fArgName);
		}
		if (fValueExpr.node != null) {
			visitor.visit(fValueExpr.node);
		}
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fValueExpr.node == child) {
			return fValueExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return null;
	}
	
	@Override
	final Expression getRightExpr() {
		return fValueExpr;
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fValueExpr == expr) {
			return STATUS_OK;
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		if (fValueExpr.node != null) {
			fStopOffset = fValueExpr.node.fStopOffset;
		}
		else if (fEqualsOffset != Integer.MIN_VALUE) {
			fStopOffset = fEqualsOffset+1;
		}
		else if (fArgName != null) {
			fStopOffset = fArgName.fStopOffset;
		}
		else {
			fStopOffset = fStartOffset;
		}
	}
	
}
