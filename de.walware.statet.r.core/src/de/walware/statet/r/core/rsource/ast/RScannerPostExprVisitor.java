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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUSFLAG_ERROR_IN_CHILD;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUSFLAG_REAL_ERROR;

import java.lang.reflect.InvocationTargetException;


/**
 * update offsets in expressions
 * not for control statements or function def/calls
 */
/* package */ class RScannerPostExprVisitor extends RAstVisitor {
	
	static final int SYNTAXERROR_MASK = (STATUSFLAG_REAL_ERROR | STATUSFLAG_ERROR_IN_CHILD);
	
	
	private boolean fSyntaxError;
	
	
	public int check(final RAstNode node) {
		fSyntaxError = false;
		try {
			node.acceptInR(this);
		}
		catch (InvocationTargetException e) {
			// not used
		}
		if (fSyntaxError) {
			return STATUSFLAG_ERROR_IN_CHILD;
		}
		return 0;
	}
	
	public int checkTerminal(final RAstNode node) {
		if ((node.getStatusCode() & SYNTAXERROR_MASK) != 0) {
			return STATUSFLAG_ERROR_IN_CHILD;
		}
		return 0;
	}
	
	private void doAcceptIn(final RAstNode child) throws InvocationTargetException {
		final boolean savedSyntaxError = fSyntaxError;
		fSyntaxError = false;
		child.acceptInR(this);
		if (fSyntaxError) {
			child.fRParent.fStatus |= STATUSFLAG_ERROR_IN_CHILD;
		}
		fSyntaxError |= savedSyntaxError;
	}
	
	private void doAccecptInChildren(final RAstNode node) throws InvocationTargetException {
		final boolean savedSyntaxError = fSyntaxError;
		fSyntaxError = false;
		node.acceptInRChildren(this);
		if (fSyntaxError) {
			node.fStatus |= STATUSFLAG_ERROR_IN_CHILD;
		}
		fSyntaxError |= savedSyntaxError;
	}
	
	
	@Override
	public void visit(final SourceComponent node) {
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Block node) {
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Group node) {
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final CIfElse node) throws InvocationTargetException {
		if (node.fWithElse) {
			doAcceptIn(node.fElseExpr.node);
		}
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final CForLoop node) throws InvocationTargetException {
		doAcceptIn(node.fLoopExpr.node);
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		doAcceptIn(node.fLoopExpr.node);
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		doAcceptIn(node.fLoopExpr.node);
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final FCall node) throws InvocationTargetException {
		doAcceptIn(node.fRefExpr.node);
		node.updateStartOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final FCall.Args node) {
//		throw new IllegalStateException();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final FCall.Arg node) {
//		throw new IllegalStateException();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final FDef node) throws InvocationTargetException {
		doAcceptIn(node.fExpr.node);
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final FDef.Args node) {
//		throw new IllegalStateException();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final FDef.Arg node) {
//		throw new IllegalStateException();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Assignment node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Model node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Relational node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Arithmetic node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Power node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Seq node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Special node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Sign node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final SubIndexed node) throws InvocationTargetException {
		doAcceptIn(node.fExpr.node);
		node.updateStartOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final SubIndexed.Args node) {
//		throw new IllegalStateException();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final SubIndexed.Arg node) {
//		throw new IllegalStateException();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final SubNamed node) throws InvocationTargetException {
		doAcceptIn(node.fExpr.node);
		// name by scanner
		node.updateStartOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final NSGet node) throws InvocationTargetException {
		// children by scanner
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final StringConst node) {
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final NumberConst node) {
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Symbol node) {
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
	@Override
	public void visit(final Dummy node) throws InvocationTargetException {
		doAccecptInChildren(node);
		node.updateStartOffset();
		node.updateStopOffset();
		fSyntaxError |= ((node.getStatusCode() & SYNTAXERROR_MASK) != 0);
	}
	
/*	
	@Override
	public void visit(CLoopCommand node) throws InvocationTargetException {
	}
	
	@Override
	public void visit(NullConst node) throws InvocationTargetException {
	}
*/	
/*	
	public void visit(final Comment node) throws InvocationTargetException {
	}
	
	public void visit(final DocuComment node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final DocuTag node) throws InvocationTargetException {
	}
	
	public void visit(final DocuText node) throws InvocationTargetException {
	}
*/
	
}
