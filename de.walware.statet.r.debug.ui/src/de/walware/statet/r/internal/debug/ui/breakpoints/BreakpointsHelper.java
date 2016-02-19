/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.IDisposable;

import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.internal.debug.ui.Messages;
import de.walware.statet.r.internal.debug.ui.RDebugUIPlugin;


public class BreakpointsHelper implements IBreakpointsListener, IDisposable {
	
	
	class UpdateRunnable implements IWorkspaceRunnable {
		
		private final IBreakpoint[] breakpoints;
		
		private List<IStatus> errors;
		
		
		public UpdateRunnable(final IBreakpoint[] breakpoints) {
			this.breakpoints= breakpoints;
		}
		
		
		public void exec() {
			try {
				ResourcesPlugin.getWorkspace().run(this, null, 0, null);
				log(null);
			}
			catch (final CoreException e) {
				log(e);
			}
		}
		
		private void log(final CoreException runError) {
			IStatus status= null;
			if (this.errors != null) {
				status= new MultiStatus(RDebugUIPlugin.PLUGIN_ID, 0,
						this.errors.toArray(new IStatus[this.errors.size()]),
						"An error occurred when updating UI information of R breakpoints.",
						runError );
			}
			else if (runError != null) {
				status= new Status(IStatus.ERROR, RDebugUIPlugin.PLUGIN_ID,
						"An error occurred when updating UI information of R breakpoints.",
						runError );
			}
			if (status != null) {
				StatusManager.getManager().handle(status, StatusManager.LOG);
			}
		}
		
		private boolean exists(final IBreakpoint breakpoint) {
			try {
				final IMarker marker= breakpoint.getMarker();
				return (marker != null && marker.exists());
			}
			catch (final Exception e) {
				return false;
			}
		}
		
		@Override
		public void run(final IProgressMonitor monitor) throws CoreException {
			for (int i= 0; i < this.breakpoints.length; i++) {
				if (this.breakpoints[i] instanceof IRBreakpoint) {
					try {
						updateBreakpoint((IRBreakpoint) this.breakpoints[i]);
					}
					catch (final CoreException e) {
						if (!exists(this.breakpoints[i])) {
							continue;
						}
						if (this.errors == null) {
							this.errors= new ArrayList<>();
						}
						this.errors.add(new Status(IStatus.ERROR, RDebugUIPlugin.PLUGIN_ID,
								"An error occurred when updating breakpoint message.", e ));
					}
				}
			}
		}
		
	};
	
	
	private final IDebugModelPresentation labelProvider= DebugUITools.newDebugModelPresentation(RDebugModel.IDENTIFIER);
	
	
	public BreakpointsHelper() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	
	@Override
	public void dispose() {
		final DebugPlugin debugPlugin= DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.getBreakpointManager().removeBreakpointListener(this);
		}
	}
	
	
	@Override
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		if (containsRBreakpoint(breakpoints)) {
			new UpdateRunnable(breakpoints).exec();
		}
	}
	
	@Override
	public void breakpointsChanged(final IBreakpoint[] breakpoints, final IMarkerDelta[] deltas) {
		if (containsRBreakpoint(breakpoints)) {
			new UpdateRunnable(breakpoints).exec();
		}
	}
	
	@Override
	public void breakpointsRemoved(final IBreakpoint[] breakpoints, final IMarkerDelta[] deltas) {
	}
	
	
	private boolean containsRBreakpoint(final IBreakpoint[] breakpoints) {
		for (int i= 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof IRBreakpoint) {
				return true;
			}
		}
		return false;
	}
	
	private void updateBreakpoint(final IRBreakpoint breakpoint) throws CoreException {
		final String typeLabel;
		final String detail= this.labelProvider.getText(breakpoint);
		
		switch (breakpoint.getBreakpointType()) {
		case RDebugModel.R_LINE_BREAKPOINT_TYPE_ID:
			typeLabel= Messages.LineBreakpoint_name;
			break;
		case RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID:
			typeLabel= Messages.MethodBreakpoint_name;
			break;
		default:
			return;
		}
		
		final IMarker marker= breakpoint.getMarker();
		if (marker != null && marker.exists()) {
			marker.setAttribute(IMarker.MESSAGE, typeLabel + ": " + detail); //$NON-NLS-1$
		}
	}
	
}
