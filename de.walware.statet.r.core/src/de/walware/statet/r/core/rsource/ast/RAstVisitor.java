/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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


/**
 * 
 */
public class RAstVisitor {
	
	
	public void visit(SourceComponent node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Block node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Group node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(CIfElse node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(CForLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(CRepeatLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(CWhileLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(CLoopCommand node) throws InvocationTargetException {
	}
	
	public void visit(FCall node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(FCall.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(FCall.Arg node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(FDef node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(FDef.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(FDef.Arg node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Assignment node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Model node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Relational node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Logical node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Arithmetic node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Power node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Seq node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Special node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Sign node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(SubIndexed node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(SubIndexed.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(SubIndexed.Arg node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(SubNamed node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(NSGet node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(StringConst node) throws InvocationTargetException {
	}
	
	public void visit(NumberConst node) throws InvocationTargetException {
	}
	
	public void visit(NullConst node) throws InvocationTargetException {
	}
	
	public void visit(Symbol node) throws InvocationTargetException {
	}
	
	public void visit(Help node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(Dummy node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
}
