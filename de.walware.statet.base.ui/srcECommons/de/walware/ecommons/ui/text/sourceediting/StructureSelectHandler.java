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

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;


public abstract class StructureSelectHandler extends AbstractHandler {
	
	
	public static class Enclosing extends StructureSelectHandler {
		
		public Enclosing(final ISourceEditor editor, final StructureSelectionHistory history) {
			super(editor, history);
		}
		
		@Override
		IRegion concreteNewSelectionRange(final AstSelection selection) {
			final IAstNode covering = selection.getCovering();
			return createRegion(covering.getOffset(), covering.getStopOffset());
		}
		
	}
	
	public static class Next extends StructureSelectHandler {
		
		public Next(final ISourceEditor editor, final StructureSelectionHistory history) {
			super(editor, history);
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
			return createRegion(covering.getOffset(), covering.getStopOffset());
		}
		
	}
	
	public static class Previous extends StructureSelectHandler {
		
		public Previous(final ISourceEditor editor, final StructureSelectionHistory history) {
			super(editor, history);
		}
		
		@Override
		IRegion concreteNewSelectionRange(final AstSelection selection) {
			final IAstNode covering = selection.getCovering();
			IAstNode child = selection.getChildFirstTouching();
			if (child == null || selection.getStartOffset() <= child.getOffset()) {
				child = selection.getChildBefore();
			}
			if (child != null) {
				return createRegion(selection.getStopOffset(), child.getOffset());
			}
			return createRegion(covering.getStopOffset(), covering.getOffset());
		}
		
	}
	
	
	private ISourceEditor fSourceEditor;
	private StructureSelectionHistory fSelectionHistory;
	
	
	protected StructureSelectHandler(final ISourceEditor editor, final StructureSelectionHistory history) {
		super();
		assert (editor != null);
		assert (history != null);
		fSourceEditor = editor;
		fSelectionHistory = history;
	}
	
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISourceUnit inputElement = fSourceEditor.getSourceUnit();
		if (inputElement == null) {
			return null;
		}
		final AstInfo<? extends IAstNode> astInfo = inputElement.getAstInfo(null, true, new NullProgressMonitor());
		if (astInfo == null) {
			return null;
		}
		
		final ITextSelection selection = getTextSelection();
		final IRegion newRange = getNewSelectionRange(selection.getOffset(), selection.getOffset()+selection.getLength(), astInfo);
		if (newRange != null) {
			fSelectionHistory.remember(new Region(selection.getOffset(), selection.getLength()));
			try {
				fSelectionHistory.ignoreSelectionChanges();
				fSourceEditor.selectAndReveal(newRange.getOffset(), newRange.getLength());
			}
			finally {
				fSelectionHistory.listenToSelectionChanges();
			}
		}
		return null;
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
		return (ITextSelection) fSourceEditor.getViewer().getSelectionProvider().getSelection();
	}
	
	protected final IRegion createRegion(final int start, final int stop) {
		return new Region(start, stop-start);
	}
	
}
