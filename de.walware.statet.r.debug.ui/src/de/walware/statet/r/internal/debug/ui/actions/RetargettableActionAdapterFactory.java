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

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;


/**
 * Creates adapters for retargettable actions in debug platform.
 * Contributed via <code>org.eclipse.core.runtime.adapters</code> 
 * extension point. 
 */
public class RetargettableActionAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS = new Class[] { IToggleBreakpointsTarget.class };
	
	
	private IToggleBreakpointsTarget fBreakpointAdapter;
	
	
	public RetargettableActionAdapterFactory() {
	}
	
	
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
	
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (IToggleBreakpointsTarget.class.equals(adapterType)) {
			synchronized (this) {
				if (fBreakpointAdapter == null) {
					fBreakpointAdapter = new ToggleBreakpointAdapter();
				}
				return fBreakpointAdapter;
			}
		} 
		return null;
	}
	
}
