/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.graphics;

import java.util.List;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.CoreException;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;

import de.walware.rj.eclient.graphics.IERGraphic;
import de.walware.rj.eclient.graphics.IERGraphicsManager;
import de.walware.rj.eclient.graphics.PageBookRGraphicView;
import de.walware.rj.eclient.graphics.RGraphicPage;

import de.walware.statet.r.console.core.RTool;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class StatetRGraphicView extends PageBookRGraphicView {
	
	
	public StatetRGraphicView() {
	}
	
	
	@Override
	protected IERGraphicsManager loadManager() {
		return RUIPlugin.getDefault().getCommonRGraphicFactory();
	}
	
	@Override
	protected RGraphicPage doCreatePage(final RGraphicSession session) {
		return new StatetRGraphicPage(session.getGraphic());
	}
	
	@Override
	protected IHandler2 createNewPageHandler() {
		return new NewDevHandler() {
			@Override
			protected ToolProcess getTool() throws CoreException {
				final ToolProcess process = NicoUI.getToolRegistry().getActiveToolSession(
						UIAccess.getActiveWorkbenchPage(false)).getProcess();
				NicoUITools.accessTool(RTool.TYPE, process);
				return process;
			}
		};
	}
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		pageIds.add("de.walware.statet.r.preferencePages.RGraphicsPage"); //$NON-NLS-1$
	}
	
	@Override
	public int canShowGraphic(final IERGraphic graphic) {
		int canShow = super.canShowGraphic(graphic);
		if (canShow > 0) {
			final ITool tool = graphic.getRHandle();
			if (tool != null) {
				final ToolSessionUIData data = NicoUI.getToolRegistry().getActiveToolSession(getViewSite().getPage());
				if (data.getProcess() == tool) {
					canShow += 1;
				}
			}
		}
		return canShow;
	}
	
}
