/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRExceptionBreakpoint;


public class AddExceptionBreakpointHandler extends AbstractHandler {
	
	
	public AddExceptionBreakpointHandler() {
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String typeName= "*";
		
		try {
			IRExceptionBreakpoint breakpoint= RDebugModel.getExpressionBreakpoint(typeName);
			if (breakpoint == null) {
				breakpoint= RDebugModel.createExceptionBreakpoint(typeName, false);
			}
			else {
				breakpoint.setEnabled(true);
			}
		}
		catch (final CoreException e) {
		}
		return null;
	}
	
}
