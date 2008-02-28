/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ast.AstSelection;
import de.walware.eclipsecommons.ltk.ast.IAstNode;

import de.walware.statet.base.ui.IStatetUICommandIds;


public abstract class StructureSelectAction extends Action {
	
	
	public static class Enclosing extends StructureSelectAction {
		
		public Enclosing(final StatextEditor1<?> editor, final SelectionHistory history) {
			super(editor, history);
			setId("SelectEnclosingElement"); //$NON-NLS-1$
			setActionDefinitionId(IStatetUICommandIds.SELECT_ENCLOSING);
		}
		
		@Override
		IRegion concreteNewSelectionRange(final AstSelection selection) {
			final IAstNode covering = selection.getCovering();
			return createRegion(covering.getStartOffset(), covering.getStopOffset());
		}
		
	}
	
	public static class Next extends StructureSelectAction {
		
		public Next(final StatextEditor1<?> editor, final SelectionHistory history) {
			super(editor, history);
			setId("SelectNextElement"); //$NON-NLS-1$
			setActionDefinitionId(IStatetUICommandIds.SELECT_NEXT);
		}
		
		@Override
		IRegion concreteNewSelectionRange(final AstSelection selection) {
			final IAstNode covering = selection.getCovering();
			IAstNode child = selection.getChildLastTouching();
			if (child == null || selection.getStopOffset() >= child.getStopOffset()) {
				child = selection.getChildAfter();
			}
			if (child != null) {
				return createRegion(selection.getStartOffset(), child.getStopOffset());
			}
			return createRegion(covering.getStartOffset(), covering.getStopOffset());
		}
		
	}
	
	public static class Previous extends StructureSelectAction {
		
		public Previous(final StatextEditor1<?> editor, final SelectionHistory history) {
			super(editor, history);
			setId("RestoreLastSelection"); //$NON-NLS-1$
			setActionDefinitionId(IStatetUICommandIds.SELECT_PREVIOUS);
		}
		
		@Override
		IRegion concreteNewSelectionRange(final AstSelection selection) {
			final IAstNode covering = selection.getCovering();
			IAstNode child = selection.getChildFirstTouching();
			if (child == null || selection.getStartOffset() <= child.getStartOffset()) {
				child = selection.getChildBefore();
			}
			if (child != null) {
				return createRegion(selection.getStopOffset(), child.getStartOffset());
			}
			return createRegion(covering.getStopOffset(), covering.getStartOffset());
		}
		
	}
	
	
	private StatextEditor1<?> fEditor;
	private SelectionHistory fSelectionHistory;
	
	protected StructureSelectAction(final StatextEditor1<?> editor, final SelectionHistory history) {
		super();
		assert (editor != null);
		assert (history != null);
		fEditor = editor;
		fSelectionHistory = history;
	}
	
	@Override
	public final  void run() {
		final ISourceUnit inputElement = fEditor.getSourceUnit();
		if (inputElement == null) {
			return;
		}
		final AstInfo<? extends IAstNode> astInfo = inputElement.getAstInfo(null, true, new NullProgressMonitor());
		if (astInfo == null) {
			return;
		}
		
		final ITextSelection selection = getTextSelection();
		final IRegion newRange = getNewSelectionRange(selection.getOffset(), selection.getOffset()+selection.getLength(), astInfo);
		if (newRange == null) {
			return;
		}
		fSelectionHistory.remember(new Region(selection.getOffset(), selection.getLength()));
		try {
			fSelectionHistory.ignoreSelectionChanges();
			fEditor.selectAndReveal(newRange.getOffset(), newRange.getLength());
		} finally {
			fSelectionHistory.listenToSelectionChanges();
		}
	}
	
	public final IRegion getNewSelectionRange(final int oldStart, final int oldStop, final AstInfo<? extends IAstNode> ast) {
//		try {
			final AstSelection selection = AstSelection.search(ast.root, oldStart, oldStop, AstSelection.MODE_COVERING_GREATER);
			if (selection.getCovering() == null) {
				return null;
			}
			return concreteNewSelectionRange(selection);
//		}
//		catch {
//		}
//		return null;
	}
	
	/**
	 * Subclasses determine the actual new selection.
	 */
	abstract IRegion concreteNewSelectionRange(AstSelection selection);
	
	protected final ITextSelection getTextSelection() {
		return (ITextSelection)fEditor.getSelectionProvider().getSelection();
	}
	
	protected final IRegion createRegion(final int start, final int stop) {
		return new Region(start, stop-start);
	}
	
}
