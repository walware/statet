/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.variables.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;


/**
 * Wraps a dynamic variable.
 * 
 * Can be used to overwrite a single method (usually {@link #getValue(String)})
 * for a given instance of a dynamic variable.
 */
public class WrappedDynamicVariable implements IDynamicVariable {
	
	
	private IDynamicVariable fVariable;
	
	
	public WrappedDynamicVariable(final IDynamicVariable variable) {
		fVariable = variable;
	}
	
	
	public String getName() {
		return fVariable.getName();
	}
	
	public String getDescription() {
		return fVariable.getDescription();
	}
	
	public boolean supportsArgument() {
		return fVariable.supportsArgument();
	}
	
	public String getValue(final String argument) throws CoreException {
		return fVariable.getValue(argument);
	}
	
	
	@Override
	public int hashCode() {
		return fVariable.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof WrappedDynamicVariable) {
			return fVariable.equals(((WrappedDynamicVariable) obj).fVariable);
		}
		return fVariable.equals(obj);
	}
	
}
