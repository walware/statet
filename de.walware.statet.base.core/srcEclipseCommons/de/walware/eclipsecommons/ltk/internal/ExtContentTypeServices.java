/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import de.walware.eclipsecommons.ltk.IExtContentTypeManager;


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
	
	private static void add(final Map<String, Set<String>> map, final String key, final String value) {
		Set<String> set = map.get(key);
		if (set == null) {
			set = new HashSet<String>();
			map.put(key, set);
		}
		set.add(value);
	}
	
	private static Map<String, String[]> copy(final Map<String, Set<String>> from, final Map<String, String[]> to) {
		for (final Map.Entry<String, Set<String>> entry : from.entrySet()) {
			final Set<String> set = entry.getValue();
			to.put(entry.getKey(), set.toArray(new String[set.size()]));
		}
		return to;
	}
	
	
	private static final String CONFIG_CONTENTTYPEACTIVATION_EXTENSIONPOINT_ID = "de.walware.eclipsecommons.ltk.contentTypeActivation"; //$NON-NLS-1$
	private static final String CONFIG_CONTENTTYPE_ELEMENT_NAME = "contentType"; //$NON-NLS-1$
	private static final String CONFIG_MODELTYPE_ELEMENT_NAME = "modelType"; //$NON-NLS-1$
	private static final String CONFIG_ID_ATTRIBUTE_NAME = "id"; //$NON-NLS-1$
	private static final String CONFIG_CONTENTTYPE_ID_ATTRIBUTE_NAME = "contentTypeId"; //$NON-NLS-1$
	private static final String CONFIG_SECONDARY_ID_ATTRIBUTE_NAME = "secondaryId"; //$NON-NLS-1$
	
	
	private Map<String, String[]> fPrimaryToSecondary;
	private Map<String, String[]> fSecondaryToPrimary;
	private Map<String, String> fModelToPrimary;
	private final String[] NO_TYPES = new String[0];
	
	
	public ExtContentTypeServices() {
		load();
	}
	
	
	private void load() {
		final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor(CONFIG_CONTENTTYPEACTIVATION_EXTENSIONPOINT_ID); 
		
		final Map<String, Set<String>> primaryToSecondary = new HashMap<String, Set<String>>();
		final Map<String, Set<String>> secondaryToPrimary = new HashMap<String, Set<String>>();
		final Map<String, String> modelToPrimary = new HashMap<String, String>();
		
		for (final IConfigurationElement element : elements) {
			if (element.getName().equals(CONFIG_CONTENTTYPE_ELEMENT_NAME)) { 
				final String primary = element.getAttribute(CONFIG_ID_ATTRIBUTE_NAME); 
				final String secondary = element.getAttribute(CONFIG_SECONDARY_ID_ATTRIBUTE_NAME); 
				if (primary != null && secondary != null
						&& primary.length() > 0 && secondary.length() > 0) {
					add(primaryToSecondary, primary, secondary);
					add(secondaryToPrimary, secondary, primary);
				}
			}
			if (element.getName().equals(CONFIG_MODELTYPE_ELEMENT_NAME)) {
				final String modelTypeId = element.getAttribute(CONFIG_ID_ATTRIBUTE_NAME); 
				final String contentTypeId = element.getAttribute(CONFIG_CONTENTTYPE_ID_ATTRIBUTE_NAME); 
				if (modelTypeId != null && contentTypeId != null
						&& modelTypeId.length() > 0 && contentTypeId.length() > 0) {
					modelToPrimary.put(modelTypeId, contentTypeId);
				}
			}
		}
		fPrimaryToSecondary = copy(primaryToSecondary, new HashMap<String, String[]>());
		fSecondaryToPrimary = copy(secondaryToPrimary, new HashMap<String, String[]>());
		fModelToPrimary = modelToPrimary;
	}
	
	
	public String[] getSecondaryContentTypes(final String primaryContentType) {
		final String[] types = fPrimaryToSecondary.get(primaryContentType);
		return (types != null) ? types : NO_TYPES;
	}
	
	public String[] getPrimaryContentTypes(final String secondaryContentType) {
		final String[] types = fSecondaryToPrimary.get(secondaryContentType);
		return (types != null) ? types : NO_TYPES;
	}
	
	public boolean matchesActivatedContentType(final String primaryContentTypeId, final String activatedContentTypeId, final boolean self) {
		final IContentTypeManager manager = Platform.getContentTypeManager();
		final IContentType primaryContentType = manager.getContentType(primaryContentTypeId);
		IContentType primary = primaryContentType;
		if (self &&
				(primary.getId().equals(activatedContentTypeId)
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
	
	public String getContentTypeForModelType(final String modelTypeId) {
		return fModelToPrimary.get(modelTypeId);
	}
	
	
	public void dispose() {
		fSecondaryToPrimary.clear();
		fPrimaryToSecondary.clear();
		fModelToPrimary.clear();
	}
	
}
