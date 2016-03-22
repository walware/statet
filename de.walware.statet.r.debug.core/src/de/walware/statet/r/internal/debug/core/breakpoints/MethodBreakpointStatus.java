/*=============================================================================#
 # Copyright (c) 2011-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.breakpoints;

import de.walware.rj.server.dbg.Tracepoint;
import de.walware.rj.server.dbg.TracepointEvent;
import de.walware.rj.server.dbg.TracepointState;

import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpointStatus;


public class MethodBreakpointStatus extends BreakpointStatus implements IRMethodBreakpointStatus {
	
	
	public MethodBreakpointStatus(final TracepointEvent event, final String label,
			final IRBreakpoint breakpoint) {
		super(event, label, breakpoint);
	}
	
	
	@Override
	public boolean isEntry() {
		return (this.event.getType() == Tracepoint.TYPE_FB
				&& (this.event.getFlags() & TracepointState.FLAG_MB_ENTRY) != 0 );
	}
	
	@Override
	public boolean isExit() {
		return (this.event.getType() == Tracepoint.TYPE_FB
				&& (this.event.getFlags() & TracepointState.FLAG_MB_EXIT) != 0 );
	}
	
}
