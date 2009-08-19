/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.core;

import org.eclipse.core.variables.IStringVariable;


/**
 * Simple string value e.g. as additional entry in variable selection dialogs.
 */
public class StringVariable implements IStringVariable {
	
	
	private final String fName;
	private final String fDescription;
	
	
	public StringVariable(final String name, final String description) {
		fName = name;
		fDescription = description;
	}
	
	
	public String getName() {
		return fName;
	}
	
	public String getDescription() {
		return fDescription;
	}
	
}
