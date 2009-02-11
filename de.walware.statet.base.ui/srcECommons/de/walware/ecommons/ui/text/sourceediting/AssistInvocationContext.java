/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Point;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ast.AstSelection;


public class AssistInvocationContext implements IQuickAssistInvocationContext {
	
	
	private final ISourceEditor fEditor;
	private final SourceViewer fSourceViewer;
	
	private final ISourceUnit fSourceUnit;
	private AstInfo fAstInfo;
	private ISourceUnitModelInfo fModelInfo;
	private AstSelection fAstSelection;
	
	private final int fInvocationOffset;
	private final int fSelectionOffset;
	private final int fSelectionLength;
	
	private String fPrefix;
	
	
	public AssistInvocationContext(final ISourceEditor editor, final int offset) {
		fEditor = editor;
		
		fSourceViewer = editor.getViewer();
		fSourceUnit = editor.getSourceUnit();
		
		fInvocationOffset = offset;
		final Point selectedRange = fSourceViewer.getSelectedRange();
		fSelectionOffset = selectedRange.x;
		fSelectionLength = selectedRange.y;
		
		if (fSourceUnit != null) {
			final NullProgressMonitor monitor = new NullProgressMonitor();
			final String type = null;
			// TODO check if/how we can reduce model requirement in content assistant
			fModelInfo = fSourceUnit.getModelInfo(type, IModelManager.MODEL_FILE, monitor);
			fAstInfo = fModelInfo != null ? fModelInfo.getAst() : fSourceUnit.getAstInfo(type, true, monitor);
			if (fAstInfo != null && fAstInfo.root != null) {
				fAstSelection = AstSelection.search(fAstInfo.root, getOffset(), getOffset(), AstSelection.MODE_COVERING_SAME_LAST);
			}
		}
	}
	
	
	/**
	 * Returns the invocation (cursor) offset.
	 * 
	 * @return the invocation offset
	 */
	public final int getInvocationOffset() {
		return fInvocationOffset;
	}
	
	public ISourceEditor getEditor() {
		return fEditor;
	}
	
	public SourceViewer getSourceViewer() {
		return fSourceViewer;
	}
	
	/**
	 * Returns the text selection offset.
	 * 
	 * @return offset of selection
	 */
	public int getOffset() {
		return fSelectionOffset;
	}
	
	/**
	 * Returns the text selection length
	 * 
	 * @return length of selection (>= 0)
	 */
	public int getLength() {
		return fSelectionLength;
	}
	
	
	public ISourceUnit getSourceUnit() {
		return fSourceUnit;
	}
	
	public AstInfo getAstInfo() {
		return fAstInfo;
	}
	
	public ISourceUnitModelInfo getModelInfo() {
		return fModelInfo;
	}
	
	public AstSelection getAstSelection() {
		return fAstSelection;
	}
	
	public String getIdentifierPrefix() {
		if (fPrefix == null) {
			fPrefix = computeIdentifierPrefix();
			if (fPrefix == null) {
				fPrefix = ""; // prevent recomputing //$NON-NLS-1$
			}
		}
		return fPrefix;
	}
	/**
	 * Computes the identifier (as specified by {@link Character#isJavaIdentifierPart(char)}) that
	 * immediately precedes the invocation offset.
	 * 
	 * @return the prefix preceding the content assist invocation offset, <code>null</code> if
	 *     there is no document
	 */
	protected String computeIdentifierPrefix() {
		return null;
	}
	
}
