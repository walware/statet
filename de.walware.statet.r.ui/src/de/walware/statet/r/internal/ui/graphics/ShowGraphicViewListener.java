/*******************************************************************************
 * Copyright (c) 2011-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.graphics;

import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;

import de.walware.rj.eclient.graphics.IERGraphic;
import de.walware.rj.eclient.graphics.PageBookRGraphicView;


public class ShowGraphicViewListener extends PageBookRGraphicView.ShowRequiredViewListener {
	
	
	public ShowGraphicViewListener() {
		super("de.walware.statet.r.views.RGraphic"); //$NON-NLS-1$
	}
	
	
	@Override
	protected IWorkbenchPage getBestPage(final IERGraphic graphic) {
		final ITool tool = graphic.getRHandle();
		if (tool instanceof ToolProcess) {
			return NicoUI.getToolRegistry().findWorkbenchPage((ToolProcess) tool);
		}
		return super.getBestPage(graphic);
	}
	
}
