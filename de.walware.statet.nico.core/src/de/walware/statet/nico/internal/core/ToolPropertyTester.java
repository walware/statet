/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;

import de.walware.statet.nico.core.ITool;


public class ToolPropertyTester extends PropertyTester {
	
	
	public static final String IS_PROVIDING_FEATURE = "isProvidingFeatureSet"; //$NON-NLS-1$
	
	
	public ToolPropertyTester() {
	}
	
	
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		ITool tool = null;
		if (receiver instanceof ITool) {
			tool = (ITool) receiver;
		}
		else if (receiver instanceof IAdaptable) {
			tool = (ITool) ((IAdaptable) receiver).getAdapter(ITool.class);
		}
		if (tool == null) {
			return false;
		}
		
		if (property.equals(IS_PROVIDING_FEATURE)) {
			for (final Object obj : args) {
				if (!tool.isProvidingFeatureSet((String) obj)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
}
