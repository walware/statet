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

package de.walware.statet.r.internal.sweave.model;

import java.lang.reflect.InvocationTargetException;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * R Chunk
 */
public class RChunkNode implements IAstNode {
	
	
	private SweaveDocElement fParent;
	// start/stop control chunk
	SourceComponent fRSource;
	
	int fStartOffset;
	int fStopOffset;
	
	
	public RChunkNode(final SweaveDocElement parent) {
		fParent = parent;
	}
	
	
	public IAstNode getParent() {
		return fParent;
	}
	
	public IAstNode getRoot() {
		return fParent.getRoot();
	}
	
	
	public boolean hasChildren() {
		return true;
	}
	
	public int getChildCount() {
		return 1;
	}
	
	public IAstNode[] getChildren() {
		return new RAstNode[] { fRSource };
	}
	
	public IAstNode getChild(final int index) {
		if (index == 0) {
			return fRSource;
		}
		throw new IndexOutOfBoundsException();
	}
	
	public int getChildIndex(final IAstNode element) {
		if (element == fRSource) {
			return 0;
		}
		return -1;
	}
	
	
	public void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		fRSource.accept(visitor);
	}
	
	
	public int getStartOffset() {
		return fStartOffset;
	}
	
	public int getStopOffset() {
		return fStopOffset;
	}
	
	public int getLength() {
		return fStopOffset-fStartOffset;
	}
	
}
