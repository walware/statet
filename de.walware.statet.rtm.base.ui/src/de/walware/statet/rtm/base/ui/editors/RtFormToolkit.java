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

package de.walware.statet.rtm.base.ui.editors;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormColors;

import de.walware.ecommons.emf.ui.forms.EFColors;
import de.walware.ecommons.emf.ui.forms.EFToolkit;
import de.walware.ecommons.emf.ui.forms.IEFColors;

import de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter;
import de.walware.statet.rtm.base.ui.rexpr.RExprWidget;
import de.walware.statet.rtm.base.util.RExprTypes;



public class RtFormToolkit extends EFToolkit {
	
	
	public RtFormToolkit(final EFColors colors) {
		super(colors);
	}
	
	
	public void adapt(final RExprWidget control) {
		final FormColors formColors = getColors();
		
		adapt(control, false, false);
		adapt(control.getText(), true, false);
		
		control.setTypeBackgroundColor(formColors.getColor(IEFColors.TW_TYPE_BACKGROUND));
		control.setTypeBorderColor(formColors.getColor(IEFColors.TB_BORDER));
		control.setTypeBorder2Color(formColors.getColor(IEFColors.TW_TYPE_BORDER2));
		control.setTypeHoverColor(formColors.getColor(IEFColors.TW_TYPE_HOVER));
	}
	
	public RExprWidget createPropRTypedExpr(final Composite parent, final int options,
			final RExprTypes types, final List<RExprTypeUIAdapter> uiAdapters) {
		final RExprWidget widget = new RExprWidget(parent, options, types, uiAdapters);
		adapt(widget);
		widget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		return widget;
	}
	
}
