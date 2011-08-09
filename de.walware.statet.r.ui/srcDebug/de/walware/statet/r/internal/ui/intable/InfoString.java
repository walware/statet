/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.intable;

import org.eclipse.swt.graphics.Font;

import net.sourceforge.nattable.style.ConfigAttribute;


public class InfoString {
	
	
	public static final ConfigAttribute<Font> CELL_STYLE_FONT_ATTRIBUTE = new ConfigAttribute<Font>();
	
	
	private final String fText;
	
	
	public InfoString(final String text) {
		fText = text;
	}
	
	
	@Override
	public String toString() {
		return fText;
	}
	
}
