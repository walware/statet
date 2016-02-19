/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.ts.IToolCommandHandler;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.IConsoleService;


public abstract class AbstractConsoleCommandHandler implements IToolCommandHandler {
	
	
	@Override
	public IStatus execute(final String id, final IToolService service, final Map<String, Object> data,
			final IProgressMonitor monitor) throws CoreException {
		return execute(id, (IConsoleService) service, data, monitor);
	}
	
	protected abstract IStatus execute(String id, IConsoleService service, Map<String, Object> data,
			IProgressMonitor monitor) throws CoreException;
	
}
