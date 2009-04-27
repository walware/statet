/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.IToolRetargetable;
import de.walware.statet.nico.ui.IToolRegistryListener;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.ToolSessionUIData;


public class WindowToolProvider implements IToolProvider, IToolRegistryListener {
	
	
	private final IWorkbenchPage fPage;
	
	private IToolRetargetable fListener;
	
	
	public WindowToolProvider(final IWorkbenchWindow window) {
		fPage = window.getActivePage();
		assert (fPage != null);
	}
	
	
	public void addToolRetargetable(final IToolRetargetable listener) {
		assert (fListener == null);
		fListener = listener;
		NicoUI.getToolRegistry().addListener(this, fPage);
	}
	
	public void removeToolRetargetable(final IToolRetargetable listener) {
		assert (fListener == listener || fListener == null);
		fListener = null;
		NicoUI.getToolRegistry().removeListener(this);
	}
	
	public ToolProcess getTool() {
		return NicoUI.getToolRegistry().getActiveToolSession(fPage).getProcess();
	}
	
	public void toolSessionActivated(final ToolSessionUIData informations) {
		fListener.setTool(informations.getProcess());
	}
	
	public void toolTerminated(final ToolSessionUIData sessionData) {
		fListener.toolTerminated();
	}
	
}