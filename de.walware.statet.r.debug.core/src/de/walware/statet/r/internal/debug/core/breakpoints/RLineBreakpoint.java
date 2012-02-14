/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.core.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.r.debug.core.RDebugModel;


public class RLineBreakpoint extends RGenericLineBreakpoint {
	
	
	public static final String R_LINE_BREAKPOINT_MARKER_TYPE = "de.walware.statet.r.debug.markers.RLineBreakpoint"; //$NON-NLS-1$
	
	
	public RLineBreakpoint(final IResource resource, final int lineNumber,
			final int charStart, final int charEnd,
			final int elementType, final String elementId, final String elementLabel, final String subLabel,
			final boolean temporary) throws CoreException {
		
		final Map<String, Object> attributes = new HashMap<String, Object>();
		addStandardLineBreakpointAttributes(attributes, true, lineNumber, charStart, charEnd,
				elementType, elementId, elementLabel, subLabel );
		
		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(R_LINE_BREAKPOINT_MARKER_TYPE));
				
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
	
	public RLineBreakpoint() {
	}
	
	
	@Override
	public String getBreakpointType() {
		return RDebugModel.R_LINE_BREAKPOINT_TYPE_ID;
	}
	
}
