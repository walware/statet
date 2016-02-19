/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.intable;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.swt.graphics.Font;


public class InfoString {
	
	
	public static final ConfigAttribute<Font> CELL_STYLE_FONT_ATTRIBUTE= new ConfigAttribute<>();
	
	public static final InfoString NA = new InfoString("NA"); //$NON-NLS-1$
	public static final InfoString DUMMY = new InfoString(""); //$NON-NLS-1$
	
	
	private final String fText;
	
	
	public InfoString(final String text) {
		fText = text;
	}
	
	
	@Override
	public String toString() {
		return fText;
	}
	
	
	@Override
	public int hashCode() {
		return fText.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof InfoString
				&& fText.equals(((InfoString) obj).fText));
	}
	
}
