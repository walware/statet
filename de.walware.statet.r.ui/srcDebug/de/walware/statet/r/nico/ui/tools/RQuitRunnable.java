/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui.tools;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.r.internal.nico.ui.RNicoMessages;
import de.walware.statet.r.nico.IBasicRAdapter;


/**
 *
 */
public class RQuitRunnable implements IToolRunnable<IBasicRAdapter> {

	public String getTypeId() {
		return ToolController.QUIT_TYPE_ID;
	}

	public String getLabel() {
		return RNicoMessages.Quit_Task_label;
	}

	public SubmitType getSubmitType() {
		return SubmitType.TOOLS;
	}

	public void run(IBasicRAdapter tools, IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		String command = "q()"; //$NON-NLS-1$
		tools.submitToConsole(command, monitor);
		
		IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		NIConsole console = NicoUITools.getConsole(tools.getController().getProcess());
		NicoUITools.showConsole(console, page, true);
	}
	
}
