/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.util;

import java.util.Map;

import de.walware.statet.nico.core.runtime.IToolEventHandler;


/**
 * Util for {@link IToolEventHandler} implementations
 */
public class ToolEventHandlerUtil {
	
	
	public static <C> C getCheckedData(final Map<String, Object> data, final String name, final Class<C> clazz, final boolean required) {
		final Object obj = data.get(name);
		if (required && obj == null) {
			throw new IllegalArgumentException("missing data entry: '" + name + '"');
		}
		try {
			return (C) obj;
		}
		catch (final ClassCastException e) {
			throw new IllegalArgumentException("incompatible data entry: '" + name + '"', e);
		}
	}
	
	public static <C> C getCheckedData(final Map<String, Object> data, final String name, final C defValue) {
		final Object obj = data.get(name);
		if (obj == null) {
			return defValue;
		}
		try {
			return (C) obj;
		}
		catch (final ClassCastException e) {
			throw new IllegalArgumentException("incompatible data entry: '" + name + '"', e);
		}
	}
	
	
	private ToolEventHandlerUtil() {}
	
}
