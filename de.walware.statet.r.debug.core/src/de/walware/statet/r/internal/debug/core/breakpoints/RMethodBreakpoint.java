/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpoint;


public class RMethodBreakpoint extends RLineBreakpoint implements IRMethodBreakpoint {
	
	
	public static final String R_METHOD_BREAKPOINT_MARKER_TYPE = "de.walware.statet.r.debug.markers.RMethodBreakpoint"; //$NON-NLS-1$
	
	public static final String ENTRY_MARKER_ATTR = "de.walware.statet.r.debug.markers.EntryBreakpointAttribute"; //$NON-NLS-1$	
	
	public static final String EXIT_MARKER_ATTR = "de.walware.statet.r.debug.markers.ExitBreakpointAttribute"; //$NON-NLS-1$	
	
	
	public RMethodBreakpoint(final IResource resource, final int lineNumber,
			final int charStart, final int charEnd,
			final int elementType, final String elementId, final String elementLabel, final String subLabel,
			final boolean temporary) throws CoreException {
		
		final Map<String, Object> attributes = new HashMap<String, Object>();
		addStandardLineBreakpointAttributes(attributes, true, lineNumber, charStart, charEnd,
				elementType, elementId, elementLabel, subLabel );
		attributes.put(ENTRY_MARKER_ATTR, Boolean.TRUE);
		
		final IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(R_METHOD_BREAKPOINT_MARKER_TYPE));
				
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
	
	public RMethodBreakpoint() {
	}
	
	
	@Override
	public String getBreakpointType() {
		return RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID;
	}
	
	
	@Override
	public void setEnabled(final boolean enabled) throws CoreException {
		if (isEnabled() != enabled) {
			if (enabled && !(isEntry() || isExit())) {
				setAttributes(new String[] { ENABLED, ENTRY_MARKER_ATTR },
						new Object[] { Boolean.TRUE, Boolean.TRUE });
			}
			else {
				setAttribute(ENABLED, enabled);
			}
		}
	}
	
	@Override
	public boolean isEntry() throws CoreException {
		return ensureMarker().getAttribute(ENTRY_MARKER_ATTR, true);
	}
	
	@Override
	public boolean isExit() throws CoreException {
		return ensureMarker().getAttribute(EXIT_MARKER_ATTR, false);
	}
	
	@Override
	public void setEntry(final boolean enabled) throws CoreException {
		if (isEntry() != enabled) {
			if (!isEnabled() && enabled) {
				setAttributes(new String[] { ENABLED, ENTRY_MARKER_ATTR },
						new Object[] { Boolean.TRUE, Boolean.TRUE });
			}
			else if (!(enabled || isExit())){
				setAttributes(new String[] { ENABLED, ENTRY_MARKER_ATTR },
						new Object[] { Boolean.FALSE, Boolean.FALSE });
			}
			else {
				setAttribute(ENTRY_MARKER_ATTR, enabled);
			}
			update();
		}
	}
	
	@Override
	public void setExit(final boolean enabled) throws CoreException {
		if (isExit() != enabled) {
			if (!isEnabled() && enabled) {
				setAttributes(new String[] { ENABLED, EXIT_MARKER_ATTR },
						new Object[] { Boolean.TRUE, Boolean.TRUE });
			}
			else if (!(enabled || isEntry())){
				setAttributes(new String[] { ENABLED, EXIT_MARKER_ATTR },
						new Object[] { Boolean.FALSE, Boolean.FALSE });
			}
			else {
				setAttribute(EXIT_MARKER_ATTR, enabled);
			}
			update();
		}
	}
	
}
