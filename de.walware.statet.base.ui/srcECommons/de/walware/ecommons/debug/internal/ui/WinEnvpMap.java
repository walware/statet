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

package de.walware.ecommons.debug.internal.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Map for environment variables on windows
 */
public class WinEnvpMap implements Map<String, String> {
	
	
	private final Map<String, String> fIdNameMap = new HashMap<String, String>();
	private final Map<String, String> fNameValueMap = new HashMap<String, String>();
	
	
	public WinEnvpMap() {
	}
	
	
	@Override
	public void clear() {
		fIdNameMap.clear();
		fNameValueMap.clear();
	}
	
	@Override
	public String put(final String name, final String value) {
		if (name == null) {
			throw new NullPointerException();
		}
		final String id = name.toUpperCase();
		final String oldName = fIdNameMap.get(id);
		String prevValue;
		if (!name.equals(oldName)) {
			prevValue = fNameValueMap.remove(oldName);
			fIdNameMap.put(id, name);
			fNameValueMap.put(name, value);
		}
		else {
			prevValue = fNameValueMap.put(name, value);
		}
		return prevValue;
	}
	
	@Override
	public void putAll(final Map<? extends String, ? extends String> t) {
		for (final Entry<? extends String, ? extends String> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public String remove(final Object key) {
		if (!(key instanceof String)) {
			return null;
		}
		final String id = ((String) key).toUpperCase();
		final String realName = fIdNameMap.remove(id);
		if (realName == null) {
			return null;
		}
		return fNameValueMap.remove(realName);
	}
	
	
	@Override
	public boolean isEmpty() {
		return fIdNameMap.isEmpty();
	}
	
	@Override
	public int size() {
		return fIdNameMap.size();
	}
	
	@Override
	public boolean containsKey(final Object key) {
		if (!(key instanceof String)) {
			return false;
		}
		final String id = ((String) key).toUpperCase();
		return fIdNameMap.containsKey(id);
	}
	
	@Override
	public boolean containsValue(final Object value) {
		return fNameValueMap.containsValue(value);
	}
	
	@Override
	public Set<Entry<String, String>> entrySet() {
		return fNameValueMap.entrySet();
	}
	
	@Override
	public Set<String> keySet() {
		return fNameValueMap.keySet();
	}
	
	@Override
	public Collection<String> values() {
		return fNameValueMap.values();
	}
	
	
	@Override
	public String get(final Object key) {
		if (!(key instanceof String)) {
			return null;
		}
		final String id = ((String) key).toUpperCase();
		final String realName = fIdNameMap.get(id);
		if (realName == null) {
			return null;
		}
		return fNameValueMap.get(realName);
	}
	
}
