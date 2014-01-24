/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.launching.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public class EnvironmentTabForR extends EnvironmentTab {
	
	
	private static final String VAR_R_KEEP_PKG_SOURCE = "R_KEEP_PKG_SOURCE"; //$NON-NLS-1$
	
	
	public EnvironmentTabForR() {
	}
	
	
	@Override
	protected void handleTableSelectionChanged(final SelectionChangedEvent event) {
		super.handleTableSelectionChanged(event);
		final IStructuredSelection selection = (IStructuredSelection) environmentTable.getSelection();
		if (selection.size() > 0) {
			for (final Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
				final Object variable = iter.next();
				if (variable != null && VAR_R_KEEP_PKG_SOURCE.equals(variable.toString())) {
					envRemoveButton.setEnabled(false);
				}
			}
		}
	}
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		final Map<String, String> map = new HashMap<String, String>();
		map.put(VAR_R_KEEP_PKG_SOURCE, "yes"); //$NON-NLS-1$
		configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
	}
	
}
