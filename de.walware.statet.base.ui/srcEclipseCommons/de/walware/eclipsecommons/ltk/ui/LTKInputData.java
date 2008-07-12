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

package de.walware.eclipsecommons.ltk.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.part.ShowInContext;

import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.IModelManager;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ISourceUnitModelInfo;
import de.walware.eclipsecommons.ltk.ast.AstSelection;
import de.walware.eclipsecommons.ltk.text.ISourceStructElement;


/**
 * Data/state of LTK based input of a view/editor.
 * 
 * E.g. used for {@link ISelectionWithElementInfoListener} or {@link ShowInContext}
 */
public class LTKInputData implements ISelection {
	
	
	protected ISourceUnit fInputElement;
	protected ISourceUnitModelInfo fInputInfo;
	
	protected ISelection fSelection;
	protected AstSelection fAstSelection;
	protected ISourceStructElement fModelSelection;
	
	
	public LTKInputData(final ISourceUnit inputElement, final ISelection selection) {
		fInputElement = inputElement;
		fSelection = selection;
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
