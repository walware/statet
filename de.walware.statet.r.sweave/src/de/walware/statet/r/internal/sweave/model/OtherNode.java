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

package de.walware.statet.r.internal.sweave.model;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * 
 */
public class OtherNode implements IAstNode {
	
	
	private IAstNode fParent;
	
	int fStartOffset;
	int fStopOffset;
	
	
	OtherNode(final IAstNode parent) {
		fParent = parent;
	}
	
	
	@Override
	public int getStatusCode() {
		return 0;
	}
	
	
	@Override
	public IAstNode getParent() {
		return fParent;
	}
	
	@Override
	public IAstNode getRoot() {
		return fParent.getRoot();
	}
	
	
	@Override
	public boolean hasChildren() {
		return true;
	}
	
	@Override
	public int getChildCount() {
		return 0;
	}
	
	public IAstNode[] getChildren() {
		return new RAstNode[] { };
	}
	
	@Override
	public IAstNode getChild(final int index) {
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public int getChildIndex(final IAstNode element) {
		return -1;
	}
	
	
	@Override
	public void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
	}
	
	
	@Override
	public int getOffset() {
		return fStartOffset;
	}
	
	@Override
	public int getStopOffset() {
		return fStopOffset;
	}
	
	@Override
	public int getLength() {
		return fStopOffset-fStartOffset;
	}
	
}
