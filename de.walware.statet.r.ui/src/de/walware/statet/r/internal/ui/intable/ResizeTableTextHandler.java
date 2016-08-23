/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.intable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;

import de.walware.workbench.ui.IWaThemeConstants;


public abstract class ResizeTableTextHandler extends AbstractHandler {
	
	
	public static class ZoomIn extends ResizeTableTextHandler {
		
		@Override
		protected int getDirection() {
			return 1;
		}
		
	}
	
	public static class ZoomOut extends ResizeTableTextHandler {
		
		@Override
		protected int getDirection() {
			return -1;
		}
		
	}
	
	
	protected ResizeTableTextHandler() {
	}
	
	protected abstract int getDirection();
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IThemeManager themeManager= PlatformUI.getWorkbench().getThemeManager();
		final FontRegistry fontRegistry= themeManager.getCurrentTheme().getFontRegistry();
		
		final Font font= fontRegistry.get(IWaThemeConstants.TABLE_FONT);
		if (font != null) {
			final FontDescriptor fontDescriptor= createFontDescriptor(font.getFontData());
			if (fontDescriptor != null) {
				fontRegistry.put(IWaThemeConstants.TABLE_FONT, fontDescriptor.getFontData());
			}
		}
		
		return null;
	}
	
	
	private FontDescriptor createFontDescriptor(final FontData[] currentFontData) {
		int fontSize= currentFontData[0].getHeight();
		fontSize= (getDirection() > 0) ?
				Math.max(fontSize + 1, fontSize * 9 / 16 * 2) :
				Math.min(fontSize - 1, (fontSize + 2) * 8 / 18 * 2);
		if (fontSize <= 0) {
			return null;
		}
		return FontDescriptor.createFrom(currentFontData).setHeight(fontSize);
	}
	
}
