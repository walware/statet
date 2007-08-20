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
public class AstSelection {
	
	
	public final static int MODE_COVERING_GREATER = 1;
	public final static int MODE_COVERING_SAME_FIRST = 2;
	public final static int MODE_COVERING_SAME_LAST = 3;
	
	
	private int fStart;
	private int fStop;
	private IAstNode fLastCovering;
	private IAstNode fBeforeChild;
	private IAstNode fFirstChild;
	private IAstNode fLastChild;
	private IAstNode fAfterChild;
	

	private class CoveringGreaterFinder extends CommonAstVisitor {
		
		private int fInCovering = -1;
		
		@Override
		public void visit(IAstNode node) {
			if (fInCovering > 0) {
				return;
			}
			if ((node.getStartOffset() < fStart && fStop <= node.getStopOffset())
					|| (node.getStartOffset() == fStart && fStop < node.getStopOffset())) {
				// covering
				clearChilds();
				fLastCovering = node;
				fInCovering = 0;
				node.acceptInChildren(this);
				fInCovering = 1;
				return;
			}
			if (fInCovering == 0) {
				checkChild(node);
				return;
			}
		}
		
	}
	
	private class CoveringSameFirstFinder extends CommonAstVisitor {
		
		private int fInCovering = -1;
		
		@Override
		public void visit(IAstNode node) {
			if (fInCovering > 0) {
				return;
			}
			if (node.getStartOffset() <= fStart && fStop <= node.getStopOffset()) {
				// covering
				clearChilds();
				fLastCovering = node;
				if (node.getStartOffset() != fStart || fStop != node.getStopOffset()) {
					fInCovering = 0;
					node.acceptInChildren(this);
				}
				fInCovering = 1;
				return;
			}
			if (fInCovering == 0) {
				checkChild(node);
				return;
			}
		}
		
	}

	private class CoveringSameLastFinder extends CommonAstVisitor {
		
		private int fInCovering = -1;
		
		@Override
		public void visit(IAstNode node) {
			if (fInCovering > 0) {
				return;
			}
			if (node.getStartOffset() <= fStart && fStop <= node.getStopOffset()) {
				// covering
				clearChilds();
				fLastCovering = node;
				fInCovering = 0;
				node.acceptInChildren(this);
				fInCovering = 1;
				return;
			}
			if (fInCovering == 0) {
				checkChild(node);
				return;
			}
		}
		
	}
	
	
	protected final void clearChilds() {
		fBeforeChild = null;
		fFirstChild = null;
		fLastChild = null;
		fAfterChild = null;
	}
	
	protected final void checkChild(IAstNode node) {
		if (node.getStopOffset() < fStart) {
			fBeforeChild = node;
			return;
		}
		if (node.getStartOffset() > fStop) {
			if (fAfterChild == null) {
				fAfterChild = node;
			}
			return;
		}
		// touching
		if (fFirstChild == null) {
			fFirstChild = node;
		}
		fLastChild = node;
	}
	

	public static AstSelection search(IAstNode rootNode, int start, int stop, int mode) {
		AstSelection selection = new AstSelection();
		selection.fStart = start;
		selection.fStop = stop;
		CommonAstVisitor finder;
		switch (mode) {
		case MODE_COVERING_GREATER:
			finder = selection.new CoveringGreaterFinder();
			break;
		case MODE_COVERING_SAME_FIRST:
			finder = selection.new CoveringSameFirstFinder();
			break;
		case MODE_COVERING_SAME_LAST:
			finder = selection.new CoveringSameLastFinder();
			break;
		default:
			throw new IllegalArgumentException("Wrong search mode"); //$NON-NLS-1$
		}
		finder.visit(rootNode);
		return selection;
	}

	
	public int getStartOffset() {
		return fStart;
	}

	public int getStopOffset() {
		return fStop;
	}

	public final IAstNode getCovering() {
		return fLastCovering;
	}

	public final IAstNode getChildBefore() {
		return fBeforeChild;
	}
	
	public final IAstNode getChildFirstTouching() {
		return fFirstChild;
	}

	public final IAstNode getChildLastTouching() {
		return fLastChild;
	}
	
	public final IAstNode getChildAfter() {
		return fAfterChild;
	}
	
}
