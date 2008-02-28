/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.IRCodeLaunchConnector;


/**
 * Connector for NICO consoles.
 */
public class RControllerCodeLaunchConnector implements IRCodeLaunchConnector {
	
	
	public static final String ID = "de.walware.statet.r.launching.RNewConsoleConnector"; //$NON-NLS-1$
	
	
	public boolean submit(final String[] rCommands, final boolean gotoConsole) throws CoreException {
		final AtomicReference<Boolean> success = new AtomicReference<Boolean>(Boolean.FALSE);
		UIAccess.checkedSyncExec(new UIAccess.CheckedRunnable() {
			public void run() throws CoreException {
				final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
				final ToolSessionUIData info = NicoUI.getToolRegistry().getActiveToolSession(page);
				final ToolController controller = NicoUITools.accessTool("R", info.getProcess());
				final IStatus status = controller.submit(rCommands, SubmitType.EDITOR);
				if (status.getSeverity() >= IStatus.ERROR) {
					throw new CoreException(status);
				}
				final NIConsole console = info.getConsole();
				if (console != null) {
					NicoUITools.showConsole(console, page, gotoConsole);
				}
				success.set(Boolean.TRUE);
			}
		});
		return success.get().booleanValue();
	}
	
	public void gotoConsole() throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
		final ToolSessionUIData info = NicoUI.getToolRegistry().getActiveToolSession(page);
		final NIConsole console = info.getConsole();
		if (console != null) {
			NicoUITools.showConsole(console, page, true);
			return;
		}
		else {
			// TODO Move to registry actions/throw exceptions
			final IWorkbenchPart part = page.getActivePart();
			if (part != null) {
				final IEditorAdapter adapter = (IEditorAdapter) part.getAdapter(IEditorAdapter.class);
				if (adapter != null) {
					adapter.setStatusLineErrorMessage(RLaunchingMessages.Run_error_NoRSession_message);
				}
			}
			Display.getCurrent().beep();
		}
	}
	
}
