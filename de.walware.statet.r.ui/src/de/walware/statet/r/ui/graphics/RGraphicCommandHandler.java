/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.graphics;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.eclient.AbstractRToolCommandHandler;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.internal.ui.RUIPlugin;


public class RGraphicCommandHandler extends AbstractRToolCommandHandler {
	
	
	public RGraphicCommandHandler() {
	}
	
	
	@Override
	public IStatus execute(final String id, final IRToolService r, final Map<String, Object> data, final IProgressMonitor monitor) {
		data.put("factory", RUIPlugin.getDefault().getCommonRGraphicFactory()); //$NON-NLS-1$
		
		return Status.OK_STATUS;
	}
	
}
