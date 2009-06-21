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
	
	
	public void visit(final SourceComponent node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Block node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Group node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final CIfElse node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final CForLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final CLoopCommand node) throws InvocationTargetException {
	}
	
	public void visit(final FCall node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final FCall.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final FCall.Arg node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final FDef node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final FDef.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final FDef.Arg node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Assignment node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Model node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Relational node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Logical node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Arithmetic node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Power node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Seq node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Special node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final Sign node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final SubIndexed node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final SubIndexed.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final SubIndexed.Arg node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final SubNamed node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final NSGet node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final StringConst node) throws InvocationTargetException {
	}
	
	public void visit(final NumberConst node) throws InvocationTargetException {
	}
	
	public void visit(final NullConst node) throws InvocationTargetException {
	}
	
	public void visit(final Symbol node) throws InvocationTargetException {
	}
	
	public void visit(final Help node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	
	public void visit(final Dummy node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	
	public void visit(final Comment node) throws InvocationTargetException {
	}
	
	public void visit(final DocuComment node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	public void visit(final DocuTag node) throws InvocationTargetException {
	}
	
	public void visit(final DocuText node) throws InvocationTargetException {
	}
	
}
