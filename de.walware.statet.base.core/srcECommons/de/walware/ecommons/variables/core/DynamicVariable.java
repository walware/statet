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

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;


/**
 * Dynamic variable to provide easily variable resolving for a given string variable.
 */
public abstract class DynamicVariable extends StringVariable implements IDynamicVariable {
	
	
	public static abstract class LocationVariable extends DynamicVariable implements ILocationVariable {
		
		
		public LocationVariable(final IStringVariable variable) {
			super(variable);
		}
		
	}
	
	
	public DynamicVariable(final IStringVariable variable) {
		super(variable.getName(), variable.getDescription());
	}
	
	
	public boolean supportsArgument() {
		return false;
	}
	
}
