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
public class RAstVisitor {
	
	
	public void visit(SourceComponent node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Block node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Group node) {
		node.acceptInChildren(this);
	}
	
	public void visit(CIfElse node) {
		node.acceptInChildren(this);
	}
	
	public void visit(CForLoop node) {
		node.acceptInChildren(this);
	}
	
	public void visit(CRepeatLoop node) {
		node.acceptInChildren(this);
	}
	
	public void visit(CWhileLoop node) {
		node.acceptInChildren(this);
	}
	
	public void visit(CLoopCommand node) {
	}
	
	public void visit(FCall node) {
		node.acceptInChildren(this);
	}
	
	public void visit(FCall.Args node) {
		node.acceptInChildren(this);
	}
	
	public void visit(FCall.Arg node) {
		node.acceptInChildren(this);
	}
	
	public void visit(FDef node) {
		node.acceptInChildren(this);
	}
	
	public void visit(FDef.Args node) {
		node.acceptInChildren(this);
	}
	
	public void visit(FDef.Arg node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Assignment node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Model node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Relational node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Logical node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Arithmetic node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Power node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Seq node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Special node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Sign node) {
		node.acceptInChildren(this);
	}
	
	public void visit(SubIndexed node) {
		node.acceptInChildren(this);
	}
	
	public void visit(SubIndexed.Sublist node) {
		node.acceptInChildren(this);
	}
	
	public void visit(SubIndexed.Arg node) {
		node.acceptInChildren(this);
	}
	
	public void visit(SubNamed node) {
		node.acceptInChildren(this);
	}
	
	public void visit(NSGet node) {
		node.acceptInChildren(this);
	}
	
	public void visit(StringConst node) {
	}
	
	public void visit(NumberConst node) {
	}
	
	public void visit(NullConst node) {
	}
	
	public void visit(Symbol node) {
	}
	
	public void visit(Help node) {
		node.acceptInChildren(this);
	}
	
	public void visit(Dummy node) {
		node.acceptInChildren(this);
	}
	
}
