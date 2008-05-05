/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * update offsets in expressions
 * not for control statements or function def/calls
 */
/* package */ class RScannerPostExprVisitor extends RAstVisitor {
	
	@Override
	public void visit(final SourceComponent node) {
	}
	
	@Override
	public void visit(final Block node) {
	}
	
	@Override
	public void visit(final Group node) {
	}
	
	@Override
	public void visit(final CIfElse node) throws InvocationTargetException {
		if (node.fWithElse) {
			node.fElseExpr.node.acceptInR(this);
		}
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final CForLoop node) throws InvocationTargetException {
		node.fLoopExpr.node.acceptInR(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		node.fLoopExpr.node.acceptInR(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		node.fLoopExpr.node.acceptInR(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final FCall node) throws InvocationTargetException {
		node.getRefChild().acceptInR(this);
		node.updateStartOffset();
	}
	
	@Override
	public void visit(final FCall.Args node) {
	}
	
	@Override
	public void visit(final FCall.Arg node) {
	}
	
	@Override
	public void visit(final FDef node) throws InvocationTargetException {
		node.getContChild().acceptInR(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final FDef.Args node) {
	}
	
	@Override
	public void visit(final FDef.Arg node) {
	}
	
	@Override
	public void visit(final Assignment node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Model node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Relational node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Arithmetic node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Power node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Seq node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Special node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Sign node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final SubIndexed node) throws InvocationTargetException {
		node.fExpr.node.acceptInR(this);
		node.updateStartOffset();
	}
	
	@Override
	public void visit(final SubIndexed.Args node) {
	}
	
	@Override
	public void visit(final SubIndexed.Arg node) {
	}
	
	@Override
	public void visit(final SubNamed node) throws InvocationTargetException {
		node.fExpr.node.acceptInR(this);
		node.updateStartOffset();
	}
	
	@Override
	public void visit(final NSGet node) {
	}
	
	@Override
	public void visit(final StringConst node) {
	}
	
	@Override
	public void visit(final NumberConst node) {
	}
	
	@Override
	public void visit(final Symbol node) {
	}
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStopOffset();
	}
	
	@Override
	public void visit(final Dummy node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		node.updateStartOffset();
		node.updateStopOffset();
	}
	
}
