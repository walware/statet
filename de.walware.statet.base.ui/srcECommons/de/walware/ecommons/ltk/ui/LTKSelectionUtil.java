/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.LTKUtil;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;


public class LTKSelectionUtil {
	
	
	public static IModelElement[] getSelectedElements(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			return getSelectedElements((IStructuredSelection) selection);
		}
		return null;
	}
	
	public static IModelElement[] getSelectedElements(final IStructuredSelection selection) {
		final IModelElement[] elements = new IModelElement[selection.size()];
		final Iterator iter = selection.iterator();
		for (int i = 0; i < elements.length; i++) {
			final Object next = iter.next();
			if (next instanceof IModelElement) {
				elements[i] = (IModelElement) next;
			}
			else {
				return null;
			}
		}
		return elements;
	}
	
	public static ISourceUnit getSingleSourceUnit(final IWorkbenchPart part) {
		final ISourceEditor editor = (ISourceEditor) part.getAdapter(ISourceEditor.class);
		if (editor == null) {
			return null;
		}
		return editor.getSourceUnit();
	}
	
	public static IAstNode getSelectedAstNode(final ISourceUnit su, final String type, final ISelection selection, final IProgressMonitor monitor) {
		if (selection instanceof ITextSelection) {
			final ITextSelection textSelection = (ITextSelection) selection;
			final ISourceUnitModelInfo modelInfo = su.getModelInfo(type, IModelManager.MODEL_FILE, monitor);
			if (modelInfo == null) {
				return null;
			}
			final AstInfo<? extends IAstNode> info = modelInfo.getAst();
			if (info == null || info.root == null) {
				return null;
			}
			final AstSelection astSelection = AstSelection.search(info.root, textSelection.getOffset(), textSelection.getOffset()+textSelection.getLength(), AstSelection.MODE_COVERING_SAME_LAST);
			return astSelection.getCovering();
		}
		return null;
	}
	
	public static ISourceStructElement[] getSelectedSourceStructElements(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			return getSelectedSourceStructElements((IStructuredSelection) selection);
		}
		return null;
	}
	
	public static ISourceStructElement[] getSelectedSourceStructElements(final IStructuredSelection selection) {
		final ISourceStructElement[] elements = new ISourceStructElement[selection.size()];
		final Iterator iter = selection.iterator();
		for (int i = 0; i < elements.length; i++) {
			final Object next = iter.next();
			if (next instanceof ISourceStructElement) {
				elements[i] = (ISourceStructElement) next;
				continue;
			}
			return null;
		}
		return elements;
	}
	
	public static IFile[] getSelectedFiles(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			return getSelectedFiles((IStructuredSelection) selection);
		}
		return null;
	}
	
	public static IFile[] getSelectedFiles(final IStructuredSelection selection) {
		final IFile[] elements = new IFile[selection.size()];
		final Iterator iter = selection.iterator();
		for (int i = 0; i < elements.length; i++) {
			final Object next = iter.next();
			if (next instanceof IFile) {
				elements[i] = (IFile) next;
				continue;
			}
			if (next instanceof IAdaptable) {
				elements[i] = (IFile) ((IAdaptable) next).getAdapter(IFile.class);
				if (elements[i] != null) {
					continue;
				}
			}
			return null;
		}
		return elements;
	}
	
	public static ISourceStructElement[] getSelectedSourceStructElement(final ISourceUnitModelInfo suModel, final ITextSelection selection) {
		if (suModel != null) {
			final ISourceStructElement root = suModel.getSourceElement();
			final int selectionStart = selection.getOffset();
			final int selectionEnd = selectionStart + selection.getLength();
			if (selectionStart >= root.getSourceRange().getOffset() 
					&& selectionEnd <= root.getSourceRange().getOffset()+root.getSourceRange().getLength()) {
				return new ISourceStructElement[] { LTKUtil.getCoveringSourceElement(root, selectionStart, selectionEnd) };
			}
		}
		return null;
	}
	
}
