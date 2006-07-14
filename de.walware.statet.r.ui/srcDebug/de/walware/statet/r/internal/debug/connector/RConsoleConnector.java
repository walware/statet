/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.connector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.r.internal.debug.launchconfigs.IRConsoleConstants;
import de.walware.statet.r.launching.IRCodeLaunchConnector;
import de.walware.statet.r.ui.RUI;


public class RConsoleConnector implements IRCodeLaunchConnector {

	
	public static final String ID = "de.walware.statet.r.launching.RConsoleConnector"; //$NON-NLS-1$


	public RConsoleConnector() {
	}

	public void submit(String[] rCommands) throws CoreException {
		
		IOConsole console = getConsole();
		if (console == null)
			throw new CoreException(new Status(
					IStatus.WARNING,
					RUI.PLUGIN_ID,
					IStatetStatusConstants.LAUNCHING_ERROR,
					"No R-Console available.",
					null));
		
		IDocument doc = console.getDocument();
		try {
			for (int i = 0; i < rCommands.length; i++) {
				doc.replace(doc.getLength(), 0, rCommands[i]+'\n');
			}
		} catch (BadLocationException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					RUI.PLUGIN_ID,
					IStatetStatusConstants.LAUNCHING_ERROR,
					"Error when running R-Console-Connector",
					e));
		}
	}

	public void gotoConsole() throws CoreException {

		IOConsole console = getConsole();
		if (console == null) {
			IWorkbenchWindow window = UIAccess.getActiveWorkbenchWindow(true);
			((ApplicationWindow) window).setStatus("No R-Console available.");
		}
	}
	
	
	private ILaunch[] getProcesses() {

		ArrayList<ILaunch> list = new ArrayList<ILaunch>();
		
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType programType = manager.getLaunchConfigurationType(IRConsoleConstants.ID_RCMD_LAUNCHCONFIG);
		if (programType == null) {
			return new ILaunch[] {};
		}
		ILaunch launches[] = manager.getLaunches();
		ILaunchConfigurationType configType;
		ILaunchConfiguration config;
		for (int i = 0; i < launches.length; i++) {
			try {
				config = launches[i].getLaunchConfiguration();
				if (config == null) {
					continue;
				}
				configType = config.getType();
			} catch (CoreException e) {
				continue;
			}
			if (configType.equals(programType)) {
				if (!launches[i].isTerminated()) {
					list.add(launches[i]);
				}
			}
		}
		
		return list.toArray(new ILaunch[list.size()]);
	}
	
	private IOConsole getConsole() throws CoreException {
		
		List<IOConsole> consoles = getAvailableConsoles(); 
		IConsoleView view = getConsoleView(true);
		if (consoles.isEmpty())
			return null;
		int idx = consoles.indexOf(view.getConsole());
		if (idx != -1)
			return consoles.get(idx);
			
		IOConsole console = consoles.get(0);

		view.display(console);
		return console;
	}
	
	private List<IOConsole> getAvailableConsoles() {

		ILaunch[] launches = getProcesses();
		if (launches.length == 0)
			return new ArrayList<IOConsole>(0);
		
		List<IOConsole> consoles = new ArrayList<IOConsole>(launches.length);
		for (ILaunch launch : launches) {
			IProcess[] processes = launch.getProcesses();
			if (processes == null || processes.length == 0)
				continue;
			IOConsole console = (IOConsole) DebugUITools.getConsole(processes[0]);
			if (console != null)
				consoles.add(console);
		}
		
		return consoles;
	}
	
	private IConsoleView getConsoleView(boolean activateConsoleView) throws PartInitException {
		
		IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		if (activateConsoleView)
			page.activate(view);
		return view; 
	}
	
	
}
