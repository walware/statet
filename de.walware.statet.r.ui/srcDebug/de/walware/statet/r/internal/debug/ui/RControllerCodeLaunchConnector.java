/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.ToolSessionUIData;
import de.walware.statet.nico.ui.console.NIConsole;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.launching.IRCodeSubmitConnector;


/**
 * Connector for NICO consoles.
 */
public class RControllerCodeLaunchConnector implements IRCodeSubmitConnector {
	
	
	public static final String ID = "de.walware.statet.r.launching.RNewConsoleConnector"; //$NON-NLS-1$
	
	
	public static interface CommandsCreator {
		
		IStatus submitTo(final ToolController controller);
		
	}
	
	
	@Override
	public boolean submit(final List<String> lines, final boolean gotoConsole) throws CoreException {
		return submit(new CommandsCreator() {
			@Override
			public IStatus submitTo(final ToolController controller) {
				return controller.submit(lines, SubmitType.EDITOR);
			}
		}, gotoConsole);
	}
	
	public boolean submit(final CommandsCreator rCommands, final boolean gotoConsole) throws CoreException {
		final AtomicReference<ToolSessionUIData> info= new AtomicReference<>();
		UIAccess.checkedSyncExec(new UIAccess.CheckedRunnable() {
			@Override
			public void run() throws CoreException {
				final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(true);
				info.set(NicoUI.getToolRegistry().getActiveToolSession(page));
			}
		});
		final ToolController controller = NicoUITools.accessController(RConsoleTool.TYPE, info.get().getProcess());
		final IStatus status = rCommands.submitTo(controller);
		if (status.getSeverity() >= IStatus.ERROR) {
			throw new CoreException(status);
		}
		final NIConsole console = info.get().getConsole();
		if (console != null) {
			NicoUITools.showConsole(console, info.get().getPage(), gotoConsole);
		}
		return true;
	}
	
	@Override
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
				final IEditorStatusLine statusLine = (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
				if (statusLine != null) {
					statusLine.setMessage(true, RLaunchingMessages.SubmitCode_error_NoRSession_message, null);
				}
			}
			Display.getCurrent().beep();
		}
	}
	
}
