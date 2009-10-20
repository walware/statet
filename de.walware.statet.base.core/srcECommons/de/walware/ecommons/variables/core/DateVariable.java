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

package de.walware.ecommons.variables.core;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;


/**
 * Dynamic variable providing the formatted date. 
 * The variable supports a formatting pattern as optional argument.
 * 
 * It is recommended to overwrite {@link #getTimestamp()} to provide a 
 * constant or special timestamp.
 * 
 * @see SimpleDateFormat
 */
public abstract class DateVariable extends StringVariable implements IDynamicVariable {
	
	
	public DateVariable(final String name, final String description) {
		super(name, description);
	}
	
	public DateVariable(final IStringVariable variable) {
		super(variable.getName(), variable.getDescription());
	}
	
	
	protected long getTimestamp() {
		return System.currentTimeMillis();
	}
	
	public boolean supportsArgument() {
		return true;
	}
	
	public String getValue(final String argument) throws CoreException {
		if (argument == null) {
			return DateFormat.getDateInstance().format(getTimestamp());
		}
		return new SimpleDateFormat(argument).format(getTimestamp());
	}
	
}