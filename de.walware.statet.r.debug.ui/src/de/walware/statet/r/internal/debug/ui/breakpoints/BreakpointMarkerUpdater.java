/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.eclipse.ui.texteditor.MarkerUtilities;

import de.walware.ecommons.ltk.ISourceUnitManager;
import de.walware.ecommons.ltk.LTK;

import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.debug.core.breakpoints.RLineBreakpointValidator;
import de.walware.statet.r.ui.RUI;


public class BreakpointMarkerUpdater implements IMarkerUpdater {
	
	
	private final static String[] ATTRIBUTES = new String[] {
		IMarker.LINE_NUMBER,
		IMarker.CHAR_START,
		IMarker.CHAR_END,
	};
	
	
	@Override
	public String getMarkerType() {
		return IBreakpoint.BREAKPOINT_MARKER;
	}
	
	@Override
	public String[] getAttribute() {
		return ATTRIBUTES;
	}
	
	@Override
	public boolean updateMarker(final IMarker marker, final IDocument document, final Position position) {
		if (position == null) {
			return true;
		}
		if (position.isDeleted()) {
			return false;
		}
		
		final IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager()
				.getBreakpoint(marker);
		if (breakpoint == null) {
			return false;
		}
		if (breakpoint instanceof IRLineBreakpoint) {
			return update((IRLineBreakpoint) breakpoint, marker, document, position);
		}
		return updateBasic(marker, document, position);
	}
	
	private boolean update(final IRLineBreakpoint breakpoint, final IMarker marker,
			final IDocument document, final Position position) {
		final IProgressMonitor monitor = new NullProgressMonitor();
		final ISourceUnitManager suManager = LTK.getSourceUnitManager();
		IRWorkspaceSourceUnit su = (IRWorkspaceSourceUnit) suManager.getSourceUnit(RModel.TYPE_ID,
				LTK.PERSISTENCE_CONTEXT, marker.getResource(), true, monitor);
		if (su == null) {
			return false;
		}
		try {
			su = (IRWorkspaceSourceUnit) suManager.getSourceUnit(RModel.TYPE_ID,
					LTK.EDITOR_CONTEXT, su, true, monitor);
			assert (su.getDocument(null) == document);
			
			final RLineBreakpointValidator validator = new RLineBreakpointValidator(su,
					breakpoint.getBreakpointType(), position.getOffset(), monitor );
			if (validator.getType() == null) {
//				// TODO search method ?
//				if (breakpoint.getElementType() != IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE) {
//				}
				return false;
			}
			
			validator.updateBreakpoint(breakpoint);
			return true;
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
					NLS.bind("An error occurred when updating an R line breakpoint in ''{0}''.",
							su.getElementName().getDisplayName() ), e ));
			return false;
		}
		finally {
			su.disconnect(monitor);
		}
	}
	
	private boolean updateBasic(final IMarker marker,
			final IDocument document, final Position position) {
		boolean offsetsInitialized = false;
		boolean offsetsChanged = false;
		final int markerStart = MarkerUtilities.getCharStart(marker);
		final int markerEnd = MarkerUtilities.getCharEnd(marker);
		if (markerStart != -1 && markerEnd != -1) {
			offsetsInitialized = true;
			
			int offset= position.getOffset();
			if (markerStart != offset) {
				MarkerUtilities.setCharStart(marker, offset);
				offsetsChanged = true;
			}
			
			offset += position.getLength();
			if (markerEnd != offset) {
				MarkerUtilities.setCharEnd(marker, offset);
			}
		}
		
		if (!offsetsInitialized || (offsetsChanged && MarkerUtilities.getLineNumber(marker) != -1)) {
			try {
				// marker line numbers are 1-based
				MarkerUtilities.setLineNumber(marker, document.getLineOfOffset(position.getOffset()) + 1);
			} catch (final BadLocationException x) {}
		}
		
		return true;
	}
	
}
