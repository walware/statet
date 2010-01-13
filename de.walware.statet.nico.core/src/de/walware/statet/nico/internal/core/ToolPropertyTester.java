/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.debug.core.DebugException;

import de.walware.statet.nico.core.ITool;
import de.walware.statet.nico.core.runtime.ToolProcess;


public class ToolPropertyTester extends PropertyTester {
	
	
	public static final String IS_MAIN_TYPE = "isMainType"; //$NON-NLS-1$
	public static final String IS_PROVIDING_FEATURE = "isProvidingFeatureSet"; //$NON-NLS-1$
	public static final String IS_TERMINATED = "isTerminated"; //$NON-NLS-1$
	
	
	public ToolPropertyTester() {
	}
	
	
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		ToolProcess tool = null;
		if (receiver instanceof ITool) {
			tool = (ToolProcess) receiver;
		}
		else if (receiver instanceof IAdaptable) {
			tool = (ToolProcess) ((IAdaptable) receiver).getAdapter(ITool.class);
		}
		
		if (property.equals(IS_PROVIDING_FEATURE)) {
			return (tool != null &&
					tool.isProvidingFeatureSet((String) expectedValue));
		}
		if (property.equals(IS_MAIN_TYPE)) {
			return (tool != null &&
					tool.getMainType().equals(expectedValue));
		}
		if (property.equals(IS_TERMINATED)) {
			if (Boolean.FALSE.equals(expectedValue)) {
				return (tool != null && !tool.isTerminated());
			}
			else if (tool != null && tool.isTerminated()) {
				if (args == null || args.length == 0) {
					return true;
				}
				try {
					final int exitValue = tool.getExitValue();
					for (final Object arg : args) {
						if (arg instanceof Integer && ((Integer) arg).intValue() == exitValue) {
							return true;
						}
					}
				}
				catch (final DebugException e) {
				}
			}
			return false;
		}
		return false;
	}
	
}
