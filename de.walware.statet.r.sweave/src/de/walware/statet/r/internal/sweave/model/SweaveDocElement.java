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
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;


/**
 * Model Element of a Sweave document.
 * E.g. for Rweave-LaTeX, the children are Tex partitions and RAstNodes
 */
public class SweaveDocElement implements IAstNode {
	
	
	private IAstNode fParent;
	final List<IAstNode> fChildren = new ArrayList<IAstNode>();
	
	int fStartOffset;
	int fStopOffset;
	
	
	SweaveDocElement() {
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
		if (fParent != null) {
			return fParent.getRoot();
		}
		return null;
	}
	
	@Override
	public boolean hasChildren() {
		return !fChildren.isEmpty();
	}
	
	@Override
	public int getChildCount() {
		return fChildren.size();
	}
	
	public IAstNode[] getChildren() {
		return fChildren.toArray(new IAstNode[fChildren.size()]);
	}
	
	@Override
	public IAstNode getChild(final int index) {
		return fChildren.get(index);
	}
	
	@Override
	public int getChildIndex(final IAstNode element) {
		final int n = fChildren.size();
		for (int i = 0; i < n; i++) {
			if (fChildren.get(i) == element) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		for (final IAstNode child : fChildren) {
			visitor.visit(child);
		}
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
