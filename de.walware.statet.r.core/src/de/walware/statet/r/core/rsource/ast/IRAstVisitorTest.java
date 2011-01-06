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

import java.lang.reflect.InvocationTargetException;


/**
 * No intended to implement. It can be used to test which methods RAstVisitor are implemented.
 */
public interface IRAstVisitorTest {
	
	public void visit(SourceComponent node) throws InvocationTargetException;
	
	public void visit(Block node) throws InvocationTargetException;
	
	public void visit(Group node) throws InvocationTargetException;
	
	public void visit(CIfElse node) throws InvocationTargetException;
	
	public void visit(CForLoop node) throws InvocationTargetException;
	
	public void visit(CRepeatLoop node) throws InvocationTargetException;
	
	public void visit(CWhileLoop node) throws InvocationTargetException;
	
	public void visit(CLoopCommand node) throws InvocationTargetException;
	
	public void visit(FCall node) throws InvocationTargetException;
	
	public void visit(FCall.Args node) throws InvocationTargetException;
	
	public void visit(FCall.Arg node) throws InvocationTargetException;
	
	public void visit(FDef node) throws InvocationTargetException;
	
	public void visit(FDef.Args node) throws InvocationTargetException;
	
	public void visit(FDef.Arg node) throws InvocationTargetException;
	
	public void visit(Assignment node) throws InvocationTargetException;
	
	public void visit(Model node) throws InvocationTargetException;
	
	public void visit(Relational node) throws InvocationTargetException;
	
	public void visit(Logical node) throws InvocationTargetException;
	
	public void visit(Arithmetic node) throws InvocationTargetException;
	
	public void visit(Power node) throws InvocationTargetException;
	
	public void visit(Seq node) throws InvocationTargetException;
	
	public void visit(Special node) throws InvocationTargetException;
	
	public void visit(Sign node) throws InvocationTargetException;
	
	public void visit(SubIndexed node) throws InvocationTargetException;
	
	public void visit(SubIndexed.Args node) throws InvocationTargetException;
	
	public void visit(SubIndexed.Arg node) throws InvocationTargetException;
	
	public void visit(SubNamed node) throws InvocationTargetException;
	
	public void visit(NSGet node) throws InvocationTargetException;
	
	public void visit(StringConst node) throws InvocationTargetException;
	
	public void visit(NumberConst node) throws InvocationTargetException;
	
	public void visit(NullConst node) throws InvocationTargetException;
	
	public void visit(Symbol node) throws InvocationTargetException;
	
	public void visit(Help node) throws InvocationTargetException;
	
	public void visit(Dummy node) throws InvocationTargetException;
	
}
