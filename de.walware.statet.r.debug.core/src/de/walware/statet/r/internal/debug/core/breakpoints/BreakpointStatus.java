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

import de.walware.rj.server.dbg.TracepointEvent;

import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpointStatus;


public class BreakpointStatus implements IRBreakpointStatus {
	
	
	protected final TracepointEvent event;
	
	protected final String label;
	protected final IRBreakpoint breakpoint;
	
	
	public BreakpointStatus(final TracepointEvent event, final String label,
			final IRBreakpoint breakpoint) {
		this.event= event;
		this.label= label;
		this.breakpoint= breakpoint;
	}
	
	
	@Override
	public int getKind() {
		switch (this.event.getKind()) {
		case TracepointEvent.KIND_ABOUT_TO_HIT:
			return HIT;
		}
		return 0;
	}
	
	@Override
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public IRBreakpoint getBreakpoint() {
		return this.breakpoint;
	}
	
}
