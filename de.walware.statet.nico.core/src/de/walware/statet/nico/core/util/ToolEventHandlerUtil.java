/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
	
	
	@SuppressWarnings("unchecked")
	public static <C> C getCheckedData(final Map<String, Object> data, final String name, final Class<C> clazz, final boolean required) {
		final Object obj = data.get(name);
		if (obj == null) {
			if (required) {
				throw new IllegalArgumentException("missing data entry: '" + name + '"');
			}
			return null;
		}
		if (!clazz.isInstance(obj)) {
			throw new IllegalArgumentException("incompatible data entry: '" + name + "' (" + obj.getClass().getName() + ")");
		}
		return (C) obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <C> C getCheckedData(final Map<String, Object> data, final String name, final C defValue) {
		final Object obj = data.get(name);
		if (obj == null) {
			return defValue;
		}
		if (!defValue.getClass().isInstance(obj)) {
			throw new IllegalArgumentException("incompatible data entry: '" + name + "' (" + obj.getClass().getName() + ")");
		}
		return (C) obj;
	}
	
	
	private ToolEventHandlerUtil() {}
	
}
