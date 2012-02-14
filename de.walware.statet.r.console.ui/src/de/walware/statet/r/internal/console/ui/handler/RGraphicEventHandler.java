/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.handler;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.IToolEventHandler;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class RGraphicEventHandler implements IToolEventHandler {
	
	
	public RGraphicEventHandler() {
	}
	
	
	@Override
	public IStatus handle(final String id, final IConsoleService tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		data.put("factory", RUIPlugin.getDefault().getCommonRGraphicFactory()); //$NON-NLS-1$
		
		return Status.OK_STATUS;
	}
	
}
