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
public class GenericVisitor extends RAstVisitor {
	
	
	public void visitNode(RAstNode node) {
		node.acceptInChildren(this);
	}
	
	
	@Override
	public void visit(SourceComponent node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Block node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Group node) {
		visitNode(node);
	}
	
	@Override
	public void visit(CIfElse node) {
		visitNode(node);
	}
	
	@Override
	public void visit(CForLoop node) {
		visitNode(node);
	}
	
	@Override
	public void visit(CRepeatLoop node) {
		visitNode(node);
	}
	
	@Override
	public void visit(CWhileLoop node) {
		visitNode(node);
	}
	
	@Override
	public void visit(CLoopCommand node) {
		visitNode(node);
	}
	
	@Override
	public void visit(FCall node) {
		visitNode(node);
	}
	
	@Override
	public void visit(FCall.Args node) {
		visitNode(node);
	}
	
	@Override
	public void visit(FCall.Arg node) {
		visitNode(node);
	}
	
	@Override
	public void visit(FDef node) {
		visitNode(node);
	}
	
	@Override
	public void visit(FDef.Args node) {
		visitNode(node);
	}
	
	@Override
	public void visit(FDef.Arg node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Assignment node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Model node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Relational node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Logical node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Arithmetic node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Power node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Seq node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Special node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Sign node) {
		visitNode(node);
	}
	
	@Override
	public void visit(SubIndexed node) {
		visitNode(node);
	}
	
	@Override
	public void visit(SubIndexed.Sublist node) {
		visitNode(node);
	}
	
	@Override
	public void visit(SubIndexed.Arg node) {
		visitNode(node);
	}
	
	@Override
	public void visit(SubNamed node) {
		visitNode(node);
	}
	
	@Override
	public void visit(NSGet node) {
		visitNode(node);
	}
	
	@Override
	public void visit(StringConst node) {
		visitNode(node);
	}
	
	@Override
	public void visit(NumberConst node) {
		visitNode(node);
	}
	
	@Override
	public void visit(NullConst node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Symbol node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Help node) {
		visitNode(node);
	}
	
	@Override
	public void visit(Dummy node) {
		visitNode(node);
	}
	
}
