/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.objectbrowser;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.statet.nico.ui.actions.ToolRetargetableHandler;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;


class ToggleAutoRefreshHandler extends ToolRetargetableHandler implements IElementUpdater {
	
	
	private final ObjectBrowserView view;
	
	private boolean currentState;
	
	
	public ToggleAutoRefreshHandler(final ObjectBrowserView view) {
		super(view, view.getSite());
		
		this.view = view;
		init();
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		this.currentState = false;
		final RProcess tool = this.view.getTool();
		if (tool != null) {
			final RWorkspace workspace = tool.getWorkspaceData();
			if (workspace != null) {
				this.currentState = workspace.isAutoRefreshEnabled();
			}
		}
		element.setChecked(this.currentState);
	}
	
	@Override
	protected Object doExecute(final ExecutionEvent event) {
		final RWorkspace workspace = (RWorkspace) getCheckedTool().getWorkspaceData();
		if (workspace != null) {
			this.currentState = !this.currentState;
			workspace.setAutoRefresh(this.currentState);
		}
		return null;
	}
	
}
