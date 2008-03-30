/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import de.walware.eclipsecommons.ltk.IModelElement;


/**
 * 
 */
public class RLabelProvider extends BaseLabelProvider implements ILabelProvider {

	public String getText(Object element) {
		if (element instanceof IModelElement) {
			IModelElement modelElement = (IModelElement) element;
			return modelElement.getElementName();
		}
		return element.toString();
	}
	
	public Image getImage(Object element) {
		return null;
	}

}
