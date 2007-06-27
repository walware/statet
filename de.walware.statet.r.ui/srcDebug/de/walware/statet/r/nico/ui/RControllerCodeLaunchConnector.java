/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.r.launching.IRCodeLaunchConnector;


/**
 * Connector for NICO consoles.
 */
public class RControllerCodeLaunchConnector implements IRCodeLaunchConnector {
	
	public boolean submit(String[] rCommands, boolean gotoConsole) throws CoreException {
		IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		ToolSessionUIData info = NicoUI.getToolRegistry().getActiveToolSession(page);
		ToolProcess process = info.getProcess();
		if (process != null) {
			ToolController controller = process.getController();
			if (controller != null) {
				controller.submit(rCommands, SubmitType.EDITOR);
				NIConsole console = info.getConsole();
				if (console != null) {
					NicoUITools.showConsole(console, page, gotoConsole);
				}
				return true;
			}
		}
		
		UIAccess.getDisplay().beep();
		return false;
	}
	
	public void gotoConsole() throws CoreException {
		IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
		ToolSessionUIData info = NicoUI.getToolRegistry().getActiveToolSession(page);
		NIConsole console = info.getConsole();
		if (console != null) {
			NicoUITools.showConsole(console, page, true);
		}
		else {
			IWorkbenchPart part = page.getActivePart();
			if (part != null) {
				IEditorAdapter adapter = (IEditorAdapter) part.getAdapter(IEditorAdapter.class);
				if (adapter != null) {
					adapter.setStatusLineErrorMessage("No Console found");
				}
			}
			Display.getCurrent().beep();
		}
	}

}
