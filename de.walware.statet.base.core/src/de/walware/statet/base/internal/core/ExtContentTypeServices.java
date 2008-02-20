/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import de.walware.statet.base.core.IExtContentTypeManager;


public class ExtContentTypeServices implements IExtContentTypeManager {
	
	
	private static boolean matches(IContentType type, final String typeId) {
		while (type != null) {
			if (typeId.equals(type.getId())) {
				return true;
			}
			type = type.getBaseType();
		}
		return false;
	}
	
	private static boolean matches(final String[] ids, final String typeId) {
		for (int i = 0; i < ids.length; i++) {
			if (typeId.equals(ids[i])) {
				return true;
			}
		}
		return false;
	}
	
	
	private final Map<String, String[]> fPrimaryToSecondary = new HashMap<String, String[]>();
	private final Map<String, String[]> fSecondaryToPrimary = new HashMap<String, String[]>();
	private final String[] NO_TYPES = new String[0];
	
	
	public ExtContentTypeServices() {
		load();
	}
	
	private void load() {
		final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor("de.walware.statet.base.contentTypeActivation"); //$NON-NLS-1$
		
		final Map<String, Set<String>> primaryToSecondary = new HashMap<String, Set<String>>();
		final Map<String, Set<String>> secondaryToPrimary = new HashMap<String, Set<String>>();
		
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getName().equals("activation")) { //$NON-NLS-1$
				final String primary = elements[i].getAttribute("primary"); //$NON-NLS-1$
				final String secondary = elements[i].getAttribute("secondary"); //$NON-NLS-1$
				if (primary != null && secondary != null
						&& primary.length() > 0 && secondary.length() > 0) {
					add(primaryToSecondary, primary, secondary);
					add(secondaryToPrimary, secondary, primary);
				}
			}
		}
		copy(primaryToSecondary, fPrimaryToSecondary);
		copy(secondaryToPrimary, fSecondaryToPrimary);
	}
	
	private void add(final Map<String, Set<String>> map,
			final String key, final String value) {
		Set<String> set = map.get(key);
		if (set == null) {
			set = new HashSet<String>();
			map.put(key, set);
		}
		set.add(value);
	}
	
	private void copy(final Map<String, Set<String>> from, final Map<String, String[]> to) {
		for (final Map.Entry<String, Set<String>> entry : from.entrySet()) {
			final Set<String> set = entry.getValue();
			to.put(entry.getKey(), set.toArray(new String[set.size()]));
		}
	}
	
	
	public String[] getSecondaryContentTypes(final String primaryContentType) {
		final String[] types = fPrimaryToSecondary.get(primaryContentType);
		return (types != null) ? types : NO_TYPES;
	}
	
	public String[] getPrimaryContentTypes(final String secondaryContentType) {
		final String[] types = fSecondaryToPrimary.get(secondaryContentType);
		return (types != null) ? types : NO_TYPES;
	}
	
	public boolean matchesActivatedContentType(final String primaryContentTypeId, final String activatedContentTypeId,
			final boolean self) {
		final IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType primary = manager.getContentType(primaryContentTypeId);
		if (self &&
				(primaryContentTypeId.equals(activatedContentTypeId)
				|| matches(primary, activatedContentTypeId))) {
			return true;
		}
		while (primary != null) {
			final String[] types = getSecondaryContentTypes(primary.getId());
			if (types != null && matches(types, activatedContentTypeId)) {
				return true;
			}
			primary = primary.getBaseType();
		}
		return false;
	}
	
	
	public void dispose() {
		fSecondaryToPrimary.clear();
		fPrimaryToSecondary.clear();
	}
	
}
