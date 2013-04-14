/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.ui.rexpr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.emf.core.util.IContext;
import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.ui.IElementNameProvider;

import de.walware.statet.rtm.base.util.RExprType;

import de.walware.statet.r.core.model.RElementName;


public class RExprTypeUIAdapter {
	
	
	protected static final int PRIORITY_INVALID = 0;
	protected static final int PRIORITY_DEFAULT = 10;
	
	
	private final RExprType fType;
	
	private final Image fImage;
	
	
	public RExprTypeUIAdapter(final RExprType type, final Image image) {
		fType = type;
		fImage = image;
	}
	
	
	public RExprType getType() {
		return fType;
	}
	
	public Image getImage() {
		return fImage;
	}
	
	public String getLabel() {
		return fType.getLabel();
	}
	
	public RExprWidget.TypeDef createWidgetDef() {
		return new RExprWidget.TypeDef(this);
	}
	
	
	public int isValidInput(final Object input, final IContext context) {
		final List<?> elements = getElements(input);
		if (elements == null || elements.isEmpty()
				|| (!isMulti() && elements.size() != 1) ) {
			return PRIORITY_INVALID;
		}
		return isValidElements(elements);
	}
	
	public List<String> getInputExprs(final Object input, final IContext context) {
		final List<?> elements = getElements(input);
		return collectElementName(elements, (input instanceof IElementNameProvider) ?
				(IElementNameProvider) input : DefaultRExprTypeUIAdapters.DIRECT_NAME);
	}
	
	public int isMoveValid(final Object input, final IContext source, final IContext context) {
		return PRIORITY_INVALID;
	}
	
	
	protected List<?> getElements(final Object input) {
		if (input instanceof IStructuredSelection) {
			return ((IStructuredSelection) input).toList();
		}
		if (input instanceof List) {
			return (List<?>) input;
		}
		return null;
	}
	
	protected boolean isMulti() {
		return false;
	}
	
	protected int isValidElements(final List<?> elements) {
		int priority = Integer.MAX_VALUE;
		for (final Object element : elements) {
			final int elementPriority = isValidElement(element);
			if (elementPriority <= 0) {
				return PRIORITY_INVALID;
			}
			if (elementPriority < priority) {
				priority = elementPriority;
			}
		}
		return priority;
	}
	
	protected int isValidElement(final Object element) {
		return PRIORITY_INVALID;
	}
	
	protected List<String> collectElementName(final List<?> elements, final IElementNameProvider nameProvider) {
		final List<String> expressions = new ArrayList<String>(elements.size());
		for (final Object element : elements) {
			final IElementName elementName = nameProvider.getElementName(element);
			if (elementName instanceof RElementName) {
				final String name = elementName.getDisplayName();
				if (name != null) {
					expressions.add(name);
				}
			}
		}
		return expressions;
	}
	
	public String adopt(final String typeKey, final String expr) {
		return null;
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RExprTypeUIAdapter"); //$NON-NLS-1$
		sb.append(" for '").append(getType().getTypeKey()).append("'");  //$NON-NLS-1$//$NON-NLS-2$
		sb.append(" (").append(getClass().getName()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("\n\tlabel= ").append(getLabel()); //$NON-NLS-1$
		sb.append("\n\tisMulti= ").append(isMulti()); //$NON-NLS-1$
		return sb.toString();
	}
	
}
