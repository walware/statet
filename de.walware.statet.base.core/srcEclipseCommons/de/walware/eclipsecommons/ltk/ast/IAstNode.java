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

package de.walware.eclipsecommons.ltk.ast;




/**
 *
 */
public interface IAstNode {
	
	public int getStartOffset();
	public int getStopOffset();
	
	public void accept(CommonAstVisitor visitor);
	public void acceptInChildren(CommonAstVisitor visitor);
	
	public IAstNode getParent();
	public IAstNode getRoot();
	public boolean hasChildren();
	public int getChildCount();
	public IAstNode getChild(int index);
	public IAstNode[] getChildren();
	public int getChildIndex(IAstNode element);
	
}
