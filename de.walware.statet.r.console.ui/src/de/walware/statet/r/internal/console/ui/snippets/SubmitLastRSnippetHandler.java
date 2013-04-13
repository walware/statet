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

package de.walware.statet.r.internal.console.ui.snippets;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.ecommons.ui.util.MenuUtil;

import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


public class SubmitLastRSnippetHandler extends AbstractHandler implements IElementUpdater {
	
	
	private final RSnippets fSnippets;
	
	
	public SubmitLastRSnippetHandler() {
		fSnippets = RConsoleUIPlugin.getDefault().getRSnippets();
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		final TemplateStore templateStore = fSnippets.getTemplateStore();
		final String name = fSnippets.getLastSnippet();
		final Template lastTemplate = (name != null) ?
				templateStore.findTemplate(name) : null;
		if (lastTemplate != null) {
			element.setTooltip(lastTemplate.getDescription());
		}
		else {
			element.setTooltip(null);
		}
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String name = fSnippets.getLastSnippet();
		final Template template = (name != null) ?
				fSnippets.getTemplateStore().findTemplate(name) : null;
		if (template != null) {
			fSnippets.run(template, event);
			return null;
		}
		{	// Show pull down menu
			final Object trigger = event.getTrigger();
			if (trigger instanceof Event) {
				final Widget widget = ((Event) trigger).widget;
				if (widget instanceof ToolItem) {
					final ToolItem ti = (ToolItem) widget;
					
					final MenuManager menuManager = new MenuManager();
					final Menu menu = menuManager.createContextMenu(ti.getParent());
					MenuUtil.registerOneWayMenu(menuManager,
							"de.walware.statet.r.menus.RunRSnippetMain" ); //$NON-NLS-1$
					MenuUtil.setPullDownPosition(menu, ti);
					
					menu.setVisible(true);
				}
			}
		}
		
		return null;
	}
	
	
}
