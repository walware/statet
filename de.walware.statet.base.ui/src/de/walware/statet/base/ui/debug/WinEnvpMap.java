/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.debug;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Map for environment variables on windows
 */
public class WinEnvpMap implements Map<String, String> {
	
	
	private Map<String, String> fIdNameMap = new HashMap<String, String>();
	private Map<String, String> fNameValueMap = new HashMap<String, String>();
	
	
	public void clear() {
		fIdNameMap.clear();
		fNameValueMap.clear();
	}
	
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
	
	public void putAll(final Map<? extends String, ? extends String> t) {
		for (final Entry<? extends String, ? extends String> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
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
	
	
	public boolean isEmpty() {
		return fIdNameMap.isEmpty();
	}
	
	public int size() {
		return fIdNameMap.size();
	}
	
	public boolean containsKey(final Object key) {
		if (!(key instanceof String)) {
			return false;
		}
		final String id = ((String) key).toUpperCase();
		return fIdNameMap.containsKey(id);
	}
	
	public boolean containsValue(final Object value) {
		return fNameValueMap.containsValue(value);
	}
	
	public Set<Entry<String, String>> entrySet() {
		return fNameValueMap.entrySet();
	}
	
	public Set<String> keySet() {
		return fNameValueMap.keySet();
	}
	
	public Collection<String> values() {
		return fNameValueMap.values();
	}
	
	
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
