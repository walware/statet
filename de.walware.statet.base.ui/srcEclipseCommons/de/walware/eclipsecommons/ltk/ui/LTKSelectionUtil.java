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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.ISourceUnitModelInfo;
import de.walware.eclipsecommons.ltk.text.ISourceStructElement;


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
	
	public static ISourceStructElement getSelectedSourceStructElement(final ISourceStructElement root, final int offset, final int endOffset) {
		ISourceStructElement ok = root;
		CHECK: while (ok != null) {
			final List<? extends ISourceStructElement> children = ok.getChildren(null);
			for (final ISourceStructElement child : children) {
				final IRegion cand = child.getSourceRange();
				if (offset >= cand.getOffset()) {
					if (offset < endOffset ? 
							endOffset < cand.getOffset()+cand.getLength() : endOffset <= cand.getOffset()+cand.getLength()) {
						ok = child;
						continue CHECK;
					}
				}
				else {
					break CHECK;
				}
			}
			break CHECK;
		}
		return ok;
	}
	
	public static ISourceStructElement[] getSelectedSourceStructElement(final ISourceUnitModelInfo suModel, final ITextSelection selection) {
		if (suModel != null) {
			final ISourceStructElement root = suModel.getSourceElement();
			final int selectionStart = selection.getOffset();
			final int selectionEnd = selectionStart + selection.getLength();
			if (selectionStart >= root.getSourceRange().getOffset() 
					&& selectionEnd <= root.getSourceRange().getOffset()+root.getSourceRange().getLength()) {
				return new ISourceStructElement[] { getSelectedSourceStructElement(root, selectionStart, selectionEnd) };
			}
		}
		return null;
	}
	
}
