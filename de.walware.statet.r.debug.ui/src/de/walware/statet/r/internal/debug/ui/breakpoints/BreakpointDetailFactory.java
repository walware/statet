/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.statet.r.debug.core.RDebugModel;
import de.walware.statet.r.debug.core.breakpoints.IRBreakpoint;
import de.walware.statet.r.internal.debug.ui.Messages;


public class BreakpointDetailFactory implements IDetailPaneFactory {
	
	
	public BreakpointDetailFactory() {
	}
	
	
	@Override
	public Set getDetailPaneTypes(final IStructuredSelection selection) {
		final Set<String> types = new HashSet<String>();
		if (selection.size() == 1 && selection.getFirstElement() instanceof IRBreakpoint) {
			final String breakpointType = ((IRBreakpoint) selection.getFirstElement()).getBreakpointType();
			if (breakpointType == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
				types.add(RLineBreakpointDetailPane.ID);
			}
			else if (breakpointType == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
				types.add(RMethodBreakpointDetailPane.ID);
			}
		}
		return types;
	}
	
	@Override
	public String getDefaultDetailPane(final IStructuredSelection selection) {
		if (selection.size() == 1 && selection.getFirstElement() instanceof IRBreakpoint) {
			final String breakpointType = ((IRBreakpoint) selection.getFirstElement()).getBreakpointType();
			if (breakpointType == RDebugModel.R_LINE_BREAKPOINT_TYPE_ID) {
				return RLineBreakpointDetailPane.ID;
			}
			else if (breakpointType == RDebugModel.R_METHOD_BREAKPOINT_TYPE_ID) {
				return RMethodBreakpointDetailPane.ID;
			}
		}
		return null;
	}
	
	@Override
	public String getDetailPaneName(final String paneID) {
		if (paneID.equals(RLineBreakpointDetailPane.ID)
				|| paneID.equals(RMethodBreakpointDetailPane.ID) ) { 
			return Messages.Breakpoint_DefaultDetailPane_name;
		}
		return null;
	}
	
	@Override
	public String getDetailPaneDescription(final String paneID) {
		if (paneID.equals(RLineBreakpointDetailPane.ID)
				|| paneID.equals(RMethodBreakpointDetailPane.ID) ) { 
			return Messages.Breakpoint_DefaultDetailPane_description;
		}
		return null;
	}
	
	@Override
	public IDetailPane createDetailPane(final String paneID) {
		if (paneID.equals(RLineBreakpointDetailPane.ID)) {
			return new RLineBreakpointDetailPane();
		}
		else if (paneID.equals(RMethodBreakpointDetailPane.ID)) {
			return new RMethodBreakpointDetailPane();
		}
		return null;
	}
	
}
