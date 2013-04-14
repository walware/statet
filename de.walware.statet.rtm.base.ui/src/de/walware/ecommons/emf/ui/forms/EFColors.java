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

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.themes.ColorUtil;


public class EFColors extends FormColors {
	
	
	public EFColors(final Display display) {
		super(display);
	}
	
	protected void initializeTypedWidgetColors() {
		if (colorRegistry.containsKey(IEFColors.TW_TYPE_BACKGROUND)) {
			return;
		}
		
		final Color border = getColor(IEFColors.TB_BORDER);
		final Color typeBackground = createColor(IEFColors.TW_TYPE_BACKGROUND,
				ColorUtil.blend(getColor(IEFColors.TB_BG).getRGB(), getBackground().getRGB(),
				40 ));
		final Color border2Color = createColor(IEFColors.TW_TYPE_BORDER2,
				ColorUtil.blend(typeBackground.getRGB(), border.getRGB(),
				60 ));
		final Color hoverColor = createColor(IEFColors.TW_TYPE_HOVER,
				ColorUtil.blend(getColor(IEFColors.TB_TOGGLE_HOVER).getRGB(), border.getRGB(),
				60 ));
	}
	
	@Override
	public Color getColor(final String key) {
		if (key.startsWith(IEFColors.TW_PREFIX)) {
			initializeTypedWidgetColors();
		}
		return super.getColor(key);
	}
	
}
