/*=============================================================================#
 # Copyright (c) 2011-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleView;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;


public class ClearOutputHandler extends AbstractHandler {
	
	
	/**
	 * Created by 
	 */
	public ClearOutputHandler() {
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ToolSessionUIData session = NicoUI.getToolRegistry().getActiveToolSession(
				UIAccess.getActiveWorkbenchPage(true) );
		final NIConsole console = session.getConsole();
		if (console == null) {
			return null;
		}
		final IConsoleView consoleView = NicoUITools.getConsoleView(console, session.getPage());
		if (consoleView == null) {
			return null;
		}
		consoleView.display(console);
		BusyIndicator.showWhile(ConsolePlugin.getStandardDisplay(), new Runnable() {
			@Override
			public void run() {
				console.clearConsole();
			}
		});
		return null;
	}
	
}
