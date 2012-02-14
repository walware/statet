/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * Calls for all node types {@link #visitNode(RAstNode)}
 */
public class GenericVisitor extends RAstVisitor {
	
	
	public void visitNode(final RAstNode node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	
	@Override
	public void visit(final SourceComponent node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Block node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Group node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final CIfElse node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final CForLoop node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final CLoopCommand node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final FCall node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final FCall.Args node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final FCall.Arg node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final FDef node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final FDef.Args node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final FDef.Arg node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Assignment node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Model node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Relational node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Arithmetic node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Power node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Seq node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Special node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Sign node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final SubIndexed node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final SubIndexed.Args node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final SubIndexed.Arg node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final SubNamed node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final NSGet node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final StringConst node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final NumberConst node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final NullConst node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Symbol node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		visitNode(node);
	}
	
	@Override
	public void visit(final Dummy node) throws InvocationTargetException {
		visitNode(node);
	}
	
}
