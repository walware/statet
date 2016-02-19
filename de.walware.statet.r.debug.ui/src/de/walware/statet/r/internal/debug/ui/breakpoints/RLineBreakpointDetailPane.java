/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.breakpoints;

import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.debug.ui.breakpoints.AbstractBreakpointDetailEditor;
import de.walware.ecommons.debug.ui.breakpoints.AbstractBreakpointDetailPane;

import de.walware.statet.r.internal.debug.ui.Messages;


public class RLineBreakpointDetailPane extends AbstractBreakpointDetailPane {
	
	
	public static final String ID = "de.walware.statet.r.debug.detailPane.RLineBreakpointDefault"; //$NON-NLS-1$
	
	
	public RLineBreakpointDetailPane() {
		super(ID, Messages.Breakpoint_DefaultDetailPane_name, Messages.Breakpoint_DefaultDetailPane_description);
	}
	
	
	@Override
	protected AbstractBreakpointDetailEditor createEditor(final Composite parent) {
		return new RLineBreakpointDetailEditor(false, true, getPropertyListeners());
	}
	
}
