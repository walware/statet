/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.graphics;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.actions.HandlerContributionItem;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.rj.eclient.graphics.IERGraphic;
import de.walware.rj.eclient.graphics.RGraphicCompositeActionSet;
import de.walware.rj.eclient.graphics.RGraphicPage;

import de.walware.statet.r.console.core.RProcess;


public class StatetRGraphicPage extends RGraphicPage {
	
	
	private class SaveInRHandler extends AbstractHandler {
		
		
		private final String fDevCmd;
		private final String fDevAbbr;
		
		
		public SaveInRHandler(final String cmd, final String abbr) {
			fDevCmd = cmd;
			fDevAbbr = abbr;
		}
		
		
		@Override
		public void setEnabled(final Object evaluationContext) {
			final ITool tool = getGraphic().getRHandle();
			setBaseEnabled(tool != null && !tool.isTerminated());
		}
		
		@Override
		public Object execute(final ExecutionEvent event) throws ExecutionException {
			final Object rHandle = getGraphic().getRHandle();
			if (rHandle instanceof RProcess) {
				final StatetRGraphicCopyToDevWizard wizard = new StatetRGraphicCopyToDevWizard((RProcess) rHandle, getGraphic(), fDevCmd, fDevAbbr);
				final WizardDialog dialog = new WizardDialog(UIAccess.getActiveWorkbenchShell(true), wizard);
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
			return null;
		}
		
	}
	
	
	public StatetRGraphicPage(final IERGraphic graphic) {
		super(graphic);
	}
	
	
	@Override
	protected RGraphicCompositeActionSet createActionSet() {
		return new RGraphicCompositeActionSet(getGraphicComposite()) {
			@Override
			public void contributeToActionsBars(final IServiceLocator serviceLocator,
					final IActionBars actionBars) {
				super.contributeToActionsBars(serviceLocator, actionBars);
				
				addSizeActions(serviceLocator, actionBars);
			}
		};
	}
	
	@Override
	protected void initActions(final IServiceLocator serviceLocator, final IActionBars actionBars) {
		super.initActions(serviceLocator, actionBars);
		
		final IMenuManager menu = actionBars.getMenuManager();
		
		menu.appendToGroup("save", new HandlerContributionItem(new CommandContributionItemParameter(
				getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
				null, null, null,
				"Save as PDF (using R)...", "D", null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				new SaveInRHandler("pdf", "pdf"))); //$NON-NLS-1$ //$NON-NLS-2$
		menu.appendToGroup("save", new HandlerContributionItem(new CommandContributionItemParameter(
				getSite(), null, HandlerContributionItem.NO_COMMAND_ID, null,
				null, null, null,
				"Save as EPS (using R)...", "E", null,
				HandlerContributionItem.STYLE_PUSH, null, false),
				new SaveInRHandler("postscript", "eps"))); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
}
