/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.r.launching.RCodeLaunchRegistry;


public class GotoRConsole implements IHandler {
	
	
	public GotoRConsole() {
	}
	
	
	public void dispose() {
	}
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			RCodeLaunchRegistry.gotoRConsole();
		}
		catch (final CoreException e) {
			throw new ExecutionException("Error occured when Goto R Console", e);
		}
		return null;
	}
	
	
	public boolean isEnabled() {
		return true;
	}
	
	public boolean isHandled() {
		return true;
	}
	
	public void addHandlerListener(final IHandlerListener handlerListener) {
	}
	
	public void removeHandlerListener(final IHandlerListener handlerListener) {
	}
	
}
