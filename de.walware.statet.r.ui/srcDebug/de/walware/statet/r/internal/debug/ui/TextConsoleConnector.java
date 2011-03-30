/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.ui.console.NIConsole;

import de.walware.statet.r.launching.IRCodeLaunchConnector;
import de.walware.statet.r.ui.RUI;


/**
 * Connector for classic Eclipse console.
 */
public class TextConsoleConnector implements IRCodeLaunchConnector {
	
	
	public static final String ID = "de.walware.statet.r.rCodeLaunchConnector.EclipseTextConsole"; //$NON-NLS-1$
	
	
	public TextConsoleConnector() {
	}
	
	public boolean submit(final String[] rCommands, final boolean gotoConsole) throws CoreException {
		if (rCommands == null) {
			throw new NullPointerException();
		}
		
		UIAccess.checkedSyncExec(new UIAccess.CheckedRunnable() {
			public void run() throws CoreException {
				final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
				IWorkbenchPart activePart = page.getActivePart();
				
				try {
					final TextConsole console = getAndShowConsole();
					if (console == null || console instanceof NIConsole) {
						handleNoConsole();
					}
					
					final IDocument doc = console.getDocument();
					try {
						for (int i = 0; i < rCommands.length; i++) {
							doc.replace(doc.getLength(), 0, rCommands[i]+'\n');
						}
						if (gotoConsole) {
							activePart = null;
						}
					} catch (final BadLocationException e) {
						throw new CoreException(new Status(
								IStatus.ERROR,
								RUI.PLUGIN_ID,
								ICommonStatusConstants.LAUNCHING,
								RLaunchingMessages.TextConsoleConnector_error_Other_message,
								e));
					}
				}
				finally {
					if (activePart != null) {
						page.activate(activePart);
					}
				}
			}
		});
		return true; // otherwise, we throw exception
	}
	
	public void gotoConsole() throws CoreException {
		UIAccess.checkedSyncExec(new UIAccess.CheckedRunnable() {
			public void run() throws CoreException {
				final TextConsole console = getAndShowConsole();
				if (console == null || console instanceof NIConsole) {
					handleNoConsole();
				}
			}
		});
	}
	
	private void handleNoConsole() throws CoreException {
		throw new CoreException(new Status(
				IStatus.WARNING,
				RUI.PLUGIN_ID,
				ICommonStatusConstants.LAUNCHING,
				RLaunchingMessages.TextConsoleConnector_error_NoConsole_message,
				null));
	}
	
	private TextConsole getAndShowConsole() throws CoreException {
		final IConsoleView view = getConsoleView(true);
		final IConsole console = view.getConsole();
		if (console instanceof TextConsole) {
			return ((TextConsole) console);
		}
		return null;
	}
	
	private IConsoleView getConsoleView(final boolean activateConsoleView) throws PartInitException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		if (activateConsoleView) {
			page.activate(view);
		}
		return view;
	}
	
}
