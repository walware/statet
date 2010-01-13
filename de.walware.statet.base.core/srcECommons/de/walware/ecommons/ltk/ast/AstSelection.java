/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ast;

import java.lang.reflect.InvocationTargetException;


/**
 * Converts source range to a selection of AST nodes.
 */
public class AstSelection {
	
	/** Selects the node, greater than the selected range */
	public final static int MODE_COVERING_GREATER = 1;
	/** Selects the outermost node, greater or equal than the selected range */
	public final static int MODE_COVERING_SAME_FIRST = 2;
	/** Selects the innermost node, greater or equal than the selected range */
	public final static int MODE_COVERING_SAME_LAST = 3;
	
	
	private final static int SEARCH_STATE_BEFORE = -1;
	private final static int SEARCH_STATE_MATCH = 0;
	private final static int SEARCH_STATE_MATCHED = 1;
	private final static int SEARCH_STATE_BEHIND = 2;
	
	private int fStart;
	private int fStop;
	private IAstNode fLastCovering;
	private IAstNode fBeforeChild;
	private IAstNode fFirstChild;
	private IAstNode fLastChild;
	private IAstNode fAfterChild;
	
	
	private class CoveringGreaterFinder implements ICommonAstVisitor {
		
		private int fInCovering = SEARCH_STATE_BEFORE;
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (fInCovering >= SEARCH_STATE_BEHIND) {
				return;
			}
			if ((node.getOffset() < fStart && fStop <= node.getStopOffset())
					|| (node.getOffset() == fStart && fStop < node.getStopOffset())) {
				// covering
				clearChilds();
				fLastCovering = node;
				fInCovering = SEARCH_STATE_MATCH;
				node.acceptInChildren(this);
				fInCovering = (fStart == fStop && node.getStopOffset() == fStop) ? SEARCH_STATE_MATCHED : SEARCH_STATE_BEHIND;
				return;
			}
			if (fInCovering == SEARCH_STATE_MATCH) {
				checkChild(node);
				return;
			}
			if (fInCovering == SEARCH_STATE_MATCHED) {
				fInCovering = SEARCH_STATE_BEHIND;
			}
		}
		
	}
	
	private class CoveringSameFirstFinder implements ICommonAstVisitor {
		
		private int fInCovering = SEARCH_STATE_BEFORE;
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (fInCovering >= SEARCH_STATE_BEHIND) {
				return;
			}
			if (node.getOffset() <= fStart && fStop <= node.getStopOffset()) {
				// covering
				clearChilds();
				fLastCovering = node;
				if (node.getOffset() != fStart || fStop != node.getStopOffset()) {
					fInCovering = SEARCH_STATE_MATCH;
					node.acceptInChildren(this);
				}
				fInCovering = SEARCH_STATE_BEHIND;
				return;
			}
			if (fInCovering == SEARCH_STATE_MATCH) {
				checkChild(node);
				return;
			}
		}
		
	}
	
	private class CoveringSameLastFinder implements ICommonAstVisitor {
		
		private int fInCovering = SEARCH_STATE_BEFORE;
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (fInCovering >= SEARCH_STATE_BEHIND) {
				return;
			}
			if (node.getOffset() <= fStart && fStop <= node.getStopOffset()) {
				// covering
				clearChilds();
				fLastCovering = node;
				fInCovering = SEARCH_STATE_MATCH;
				node.acceptInChildren(this);
				fInCovering = SEARCH_STATE_BEHIND;
				return;
			}
			if (fInCovering == SEARCH_STATE_MATCH) {
				checkChild(node);
				return;
			}
		}
		
	}
	
	
	AstSelection() {
	}
	
	protected final void clearChilds() {
		fBeforeChild = null;
		fFirstChild = null;
		fLastChild = null;
		fAfterChild = null;
	}
	
	protected final void checkChild(final IAstNode node) {
		if (node.getStopOffset() < fStart) {
			fBeforeChild = node;
			return;
		}
		if (node.getOffset() > fStop) {
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
	
	
	public static AstSelection search(final IAstNode rootNode, final int start, final int stop, final int mode) {
		final AstSelection selection = new AstSelection();
		selection.fStart = start;
		selection.fStop = stop;
		ICommonAstVisitor finder;
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
		try {
			finder.visit(rootNode);
		} catch (final InvocationTargetException e) {
		}
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
