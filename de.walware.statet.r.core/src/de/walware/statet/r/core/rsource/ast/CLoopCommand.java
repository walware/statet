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

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;



/**
 * next
 * break
 */
abstract class CLoopCommand extends RAstNode {
	
	
	static class Break extends CLoopCommand {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.C_BREAK;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.C_BREAK);
		}
		
	}

	static class Next extends CLoopCommand {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.C_NEXT;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.C_NEXT);
		}
		
	}

	
	@Override
	public final boolean hasChildren() {
		return false;
	}
	
	@Override
	public final int getChildCount() {
		return 0;
	}
	
	@Override
	public final RAstNode getChild(int index) {
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return NO_CHILDREN;
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		return -1;
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
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
	}

	@Override
	final void updateStopOffset() {
	}

}
