/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.part.ShowInContext;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.text.ISourceStructElement;


/**
 * Data/state of LTK based input of a view/editor.
 * 
 * E.g. used for {@link ISelectionWithElementInfoListener} or {@link ShowInContext}
 */
public class LTKInputData implements ISelection {
	
	
	protected ISourceUnit fInputElement;
	protected ISourceUnitModelInfo fInputInfo;
	
	protected ISelectionProvider fSelectionProvider;
	protected ISelection fSelection;
	protected AstSelection fAstSelection;
	protected ISourceStructElement fModelSelection;
	
	
	public LTKInputData(final ISourceUnit inputElement, final ISelection selection) {
		fInputElement = inputElement;
		fSelection = selection;
	}
	
	public LTKInputData(final ISourceUnit inputElement, final ISelectionProvider selectionProvider) {
		fInputElement = inputElement;
		fSelectionProvider = selectionProvider;
		fSelection = selectionProvider.getSelection();
	}
	
	
	public boolean update() {
		if (fSelectionProvider != null) {
			final ISelection selection = fSelectionProvider.getSelection();
			if (!selection.equals(fSelection)) {
				fAstSelection = null;
				fModelSelection = null;
			}
			return true;
		}
		return false;
	}
	
	public IModelElement getInputElement() {
		return fInputElement;
	}
	
	public ISourceUnitModelInfo getInputInfo() {
		if (fInputInfo == null) {
			fInputInfo = fInputElement.getModelInfo(null, IModelManager.NONE, new NullProgressMonitor());
		}
		return fInputInfo;
	}
	
	
	public boolean isEmpty() {
		return fSelection.isEmpty();
	}
	
	public ISelection getSelection() {
		return fSelection;
	}
	
	public AstSelection getAstSelection() {
		if (fAstSelection == null) {
			if (fSelection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) fSelection;
				fAstSelection = AstSelection.search(getInputInfo().getAst().root, textSelection.getOffset(), textSelection.getOffset()+textSelection.getLength(), AstSelection.MODE_COVERING_SAME_LAST);
			}
		}
		return fAstSelection;
	}
	
	public ISourceStructElement getModelSelection() {
		if (fModelSelection == null) {
			if (fSelection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) fSelection;
				fModelSelection = LTKSelectionUtil.getSelectedSourceStructElement(getInputInfo().getSourceElement(), textSelection.getOffset(), textSelection.getOffset()+textSelection.getLength());
			}
		}
		return fModelSelection;
	}
	
	
	public boolean isStillValid() {
		return true;
	}
	
}
