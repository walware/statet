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
 * No intended to implement. It can be used to test which methods RAstVisitor are implemented.
 */
public interface IRAstVisitorTest {
	
	public void visit(SourceComponent node);
	
	public void visit(Block node);
	
	public void visit(Group node);
	
	public void visit(CIfElse node);
	
	public void visit(CForLoop node);
	
	public void visit(CRepeatLoop node);
	
	public void visit(CWhileLoop node);
	
	public void visit(CLoopCommand node);
	
	public void visit(FCall node);
	
	public void visit(FCall.Args node);
	
	public void visit(FCall.Arg node);
	
	public void visit(FDef node);
	
	public void visit(FDef.Args node);
	
	public void visit(FDef.Arg node);
	
	public void visit(Assignment node);
	
	public void visit(Model node);
	
	public void visit(Relational node);
	
	public void visit(Logical node);
	
	public void visit(Arithmetic node);
	
	public void visit(Power node);
	
	public void visit(Seq node);
	
	public void visit(Special node);
	
	public void visit(Sign node);
	
	public void visit(SubIndexed node);
	
	public void visit(SubIndexed.Args node);
	
	public void visit(SubIndexed.Arg node);
	
	public void visit(SubNamed node);
	
	public void visit(NSGet node);
	
	public void visit(StringConst node);
	
	public void visit(NumberConst node);
	
	public void visit(NullConst node);
	
	public void visit(Symbol node);
	
	public void visit(Help node);
	
	public void visit(Dummy node);
	
}
