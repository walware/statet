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

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;


public class ElementNameComparator extends ViewerComparator implements Comparator<IModelElement> {
	
	
	public ElementNameComparator(final Comparator<IElementName> nameComparator) {
		super(nameComparator);
	}
	
	
	@Override
	public int category(final Object element) {
		return category((IModelElement) element);
	}
	
	public int category(final IModelElement element) {
		switch (element.getElementType() & IModelElement.MASK_C1) {
		case IModelElement.C1_IMPORT:
			return 1;
		case IModelElement.C1_CLASS:
			return 2;
		}
		return 100;
	}
	
	public int compare(final IModelElement e1, final IModelElement e2) {
		final int c1 = category(e1);
		final int c2 = category(e2);
		if (c1 != c2) {
			return c1 - c2;
		}
		final int result = getComparator().compare(e1.getElementName(), e2.getElementName());
		if (result != 0) {
			return result;
		}
		if (e1 instanceof ISourceStructElement && e2 instanceof ISourceStructElement) {
			return ((ISourceStructElement) e1).getSourceRange().getOffset() 
					- ((ISourceStructElement) e2).getSourceRange().getOffset();
		}
		return 0;
	}
	
	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if (e1 instanceof IModelElement) {
			if (e2 instanceof IModelElement) {
				return compare((IModelElement) e1, (IModelElement) e2);
			}
			else {
				return Integer.MIN_VALUE;
			}
		}
		else {
			if (e2 instanceof IModelElement) {
				return Integer.MAX_VALUE;
			}
			else {
				return 0;
			}
		}
	}
	
}
