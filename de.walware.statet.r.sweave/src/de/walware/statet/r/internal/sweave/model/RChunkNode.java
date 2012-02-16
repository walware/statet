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

package de.walware.statet.r.internal.sweave.model;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * R Chunk
 */
public class RChunkNode implements IAstNode {
	
	
	private final IAstNode fParent;
	// start/stop control chunk
	Args fWeaveArgs;
	SourceComponent[] fRSources;
	
	int fStartOffset;
	int fStopOffset;
	
	
	RChunkNode(final IAstNode parent) {
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
		return fRSources.length+1;
	}
	
	@Override
	public IAstNode getChild(final int index) {
		if (index == 0) {
			return fWeaveArgs;
		}
		return fRSources[index-1];
	}
	
	@Override
	public int getChildIndex(final IAstNode element) {
		if (fWeaveArgs == element) {
			return 0;
		}
		for (int i = 0; i < fRSources.length; i++) {
			if (fRSources[i] == element) {
				return i+1;
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
		visitor.visit(fWeaveArgs);
		for (final SourceComponent node : fRSources) {
			visitor.visit(node);
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
