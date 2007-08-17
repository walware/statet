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



/**
 *
 */
abstract class SpecItem extends RAstNode {


	RAstNode fArgName;
	int fEqualsOffset = Integer.MIN_VALUE;
	final Expression fValueExpr = new Expression();
	
	
	@Override
	public final boolean hasChildren() {
		return (fArgName != null || fValueExpr.node != null);
	}
	
	@Override
	public final int getChildCount() {
		int count = (fArgName != null) ? 1 : 0;
		if (fValueExpr != null) {
			count++;
		}
		return count;
	}
	
	@Override
	public final RAstNode getChild(int index) {
		if (fArgName != null) {
			switch (index) {
			case 0:
				return fArgName;
			case 1:
				if (fValueExpr != null) {
					return fValueExpr.node;
				}
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
	public int getIndex(RAstNode element) {
		if (fArgName == element) {
			return 0;
		}
		if (fValueExpr.node == element) {
			return 1;
		}
		return -1;
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		if (fArgName != null) {
			fArgName.accept(visitor);
		}
		if (fValueExpr.node != null) {
			fValueExpr.node.accept(visitor);
		}
	}
	

	@Override
	final Expression getExpr(RAstNode child) {
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
	final void updateStopOffset() {
		if (fValueExpr.node != null) {
			fStopOffset = fValueExpr.node.fStopOffset;
		}
		else if (fEqualsOffset >= 0) {
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
