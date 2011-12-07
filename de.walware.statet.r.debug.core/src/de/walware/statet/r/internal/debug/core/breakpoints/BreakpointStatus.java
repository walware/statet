/*******************************************************************************
 * Copyright (c) 2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core.breakpoints;

import de.walware.rj.server.dbg.Tracepoint;
import de.walware.rj.server.dbg.TracepointEvent;
import de.walware.rj.server.dbg.TracepointState;

import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpointStatus;


public class BreakpointStatus implements IRMethodBreakpointStatus {
	
	
	protected final TracepointEvent fEvent;
	
	protected String fLabel;
	protected final IRBreakpoint fBreakpoint;
	
	
	public BreakpointStatus(final TracepointEvent event, final String label,
			final IRBreakpoint breakpoint) {
		fEvent = event;
		fLabel = label;
		fBreakpoint = breakpoint;
	}
	
	
	@Override
	public int getKind() {
		switch (fEvent.getKind()) {
		case TracepointEvent.KIND_ABOUT_TO_HIT:
			return HIT;
		}
		return 0;
	}
	
	@Override
	public String getLabel() {
		return fLabel;
	}
	
	@Override
	public IRBreakpoint getBreakpoint() {
		return fBreakpoint;
	}
	
	@Override
	public boolean isEntry() {
		return (fEvent.getType() == Tracepoint.TYPE_FB
				&& (fEvent.getFlags() & TracepointState.FLAG_MB_ENTRY) != 0 );
	}
	
	@Override
	public boolean isExit() {
		return (fEvent.getType() == Tracepoint.TYPE_FB
				&& (fEvent.getFlags() & TracepointState.FLAG_MB_EXIT) != 0 );
	}
	
}
