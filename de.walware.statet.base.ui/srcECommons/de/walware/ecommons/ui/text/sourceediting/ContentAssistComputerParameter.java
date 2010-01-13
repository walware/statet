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

package de.walware.ecommons.ui.text.sourceediting;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.walware.ecommons.ui.util.MessageUtil;


/**
 * Map of parameters for the specific content assist command.
 */
public abstract class ContentAssistComputerParameter implements IParameterValues {
	
	
	private final String fPluginId;
	private final String[] fExtensionsPointNames;
	
	
	protected ContentAssistComputerParameter(final String pluginId, final String[] extensionPointNames) {
		fPluginId = pluginId;
		fExtensionsPointNames = extensionPointNames;
	}
	
	public Map<String, String> getParameterValues() {
		final Map<String, String> map = new HashMap<String, String>();
		
		final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		for (int i = 0; i < fExtensionsPointNames.length; i++) {
			final IConfigurationElement[] contributions = extensionRegistry.getConfigurationElementsFor(fPluginId, fExtensionsPointNames[i]);
			for (final IConfigurationElement config : contributions) {
				if (config.getName().equals(ContentAssistComputerRegistry.CONFIG_CATEGORY_ELEMENT_NAME)) {
					try {
						final String id = ContentAssistComputerRegistry.getCheckedString(config, ContentAssistComputerRegistry.CONFIG_ID_ATTRIBUTE_NAME);
						final String name = ContentAssistComputerRegistry.getCheckedString(config, ContentAssistComputerRegistry.CONFIG_NAME_ATTRIBUTE_NAME);
						map.put(MessageUtil.removeMnemonics(name), id);
					}
					catch (final CoreException e) {
					}
				}
			}
		}
		
		return map;
	}
	
}
