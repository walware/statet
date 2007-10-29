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
 * update offsets in expressions
 * not for control statements or function def/calls
 */
/* package */ class RScannerPostExprVisitor extends RAstVisitor {
	
	@Override
	public void visit(SourceComponent node) {
	}
	
	@Override
	public void visit(Block node) {
	}
	
	@Override
	public void visit(CIfElse node) {
		// skip scan cond and then
		if (node.fWithElse) {
			node.getElseChild().accept(this);
		}
		node.updateStopOffset();
	}

	@Override
	public void visit(CForLoop node) {
		// skip scan cond
		node.getContChild().accept(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(CRepeatLoop node) {
		node.acceptInChildren(this);
		node.updateStopOffset();
	}

	@Override
	public void visit(CWhileLoop node) {
		// skip scan cond
		node.getContChild().accept(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(FCall node) {
		node.getRefChild().accept(this);
		node.updateStartOffset();
//		node.updateStopOffset();
	}
	
	@Override
	public void visit(FCall.Args node) {
	}
	
	@Override
	public void visit(FCall.Arg node) {
	}
	
	@Override
	public void visit(FDef node) {
		node.getContChild().accept(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(FDef.Args node) {
	}
	
	@Override
	public void visit(FDef.Arg node) {
	}
	
	@Override
	public void visit(Assignment node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Model node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}

	@Override
	public void visit(Relational node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Logical node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}

	@Override
	public void visit(Arithmetic node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Power node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Seq node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Special node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Sign node) {
		node.acceptInChildren(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(SubIndexed node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(SubIndexed.Sublist node) {
	}
	
	@Override
	public void visit(SubIndexed.Arg node) {
	}

	@Override
	public void visit(SubNamed node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(NSGet node) {
//		node.acceptInChildren(this);
//		node.updateStartOffset();
//		node.updateStopOffset();
	}
	
	@Override
	public void visit(StringConst node) {
	}
	
	@Override
	public void visit(NumberConst node) {
	}
	
	@Override
	public void visit(Symbol node) {
	}
	
	@Override
	public void visit(Help node) {
		node.acceptInChildren(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(Dummy node) {
		node.acceptInChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}

}
