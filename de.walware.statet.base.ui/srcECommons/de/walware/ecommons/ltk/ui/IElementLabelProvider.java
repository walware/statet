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

package de.walware.ecommons.ltk.ui;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import de.walware.ecommons.ltk.IModelElement;


/**
 * Provides labels for model elements
 */
public interface IElementLabelProvider {
	
	
	public final static StyledString.Styler TITLE_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(final TextStyle style) {
			((StyleRange) style).fontStyle = SWT.BOLD;
		};
	};
	
	
	public String getText(IModelElement element);
	
//	public void decorateText(final StringBuilder text, final IModelElement element);
	
	public StyledString getStyledText(IModelElement element);
	
//	public void decorateStyledText(StyledString text, IModelElement element);
	
	public Image getImage(IModelElement element);
	
}
