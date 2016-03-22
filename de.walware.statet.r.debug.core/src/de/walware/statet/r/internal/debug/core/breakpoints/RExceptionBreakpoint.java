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

package de.walware.statet.r.internal.debug.core.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRExceptionBreakpoint;


public class RExceptionBreakpoint extends RBreakpoint implements IRExceptionBreakpoint {
	
	
	public static final String R_EXCEPTION_BREAKPOINT_MARKER_TYPE = "de.walware.statet.r.debug.markers.RExceptionBreakpoint"; //$NON-NLS-1$
	
	public static final String EXCEPTION_ID_MARKER_ATTR = "de.walware.statet.r.debug.markers.ExceptionIdAttribute"; //$NON-NLS-1$
	
	
	public RExceptionBreakpoint(final IResource resource, final String exceptionId,
			final boolean temporary) throws DebugException {
		final Map<String, Object> attributes= new HashMap<>();
		attributes.put(IBreakpoint.ID, getModelIdentifier());
		attributes.put(IBreakpoint.ENABLED, Boolean.TRUE);
		attributes.put(EXCEPTION_ID_MARKER_ATTR, exceptionId);
		
		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(R_EXCEPTION_BREAKPOINT_MARKER_TYPE));
				
				// update attributes
				ensureMarker().setAttributes(attributes);
				register(!temporary);
				if (temporary) {
					setPersisted(false);
				}
			}
		};
		run(ResourcesPlugin.getWorkspace().getRuleFactory().markerRule(resource), wr);
	}
	
	public RExceptionBreakpoint() {
	}
	
	
	@Override
	public String getBreakpointType() {
		return RDebugModel.R_EXCEPTION_BREAKPOINT_TYPE_ID;
	}
	
	
	@Override
	public String getExceptionId() throws DebugException {
		return ensureMarker().getAttribute(EXCEPTION_ID_MARKER_ATTR, null);
	}
	
}
