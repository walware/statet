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

package de.walware.eclipsecommons.ltk;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.base.internal.core.BaseCorePlugin;


/**
 * 
 */
public final class WorkingContext {
	
	
	private static final String CONFIG_MODELTYPE_ID_ATTRIBUTE_NAME = "modelTypeId"; //$NON-NLS-1$
	private static final String CONFIG_CONTEXT_KEY_ATTRIBUTE_NAME = "contextKey"; //$NON-NLS-1$
	
	
	private final String fKey;
	private final Map<String, ISourceUnitFactory> fFactories;
	
	
	public WorkingContext(final String key) {
		fKey = key;
		fFactories = new HashMap<String, ISourceUnitFactory>();
	}
	
	
	public String getKey() {
		return fKey;
	}
	
	public ISourceUnit getUnit(final Object from, String modelTypeId, final boolean create, final IProgressMonitor monitor) {
		synchronized (this) {
			final ISourceUnit fromUnit = (from instanceof ISourceUnit) ?
				((ISourceUnit) from) : null;
			if (modelTypeId == null && fromUnit != null) {
				modelTypeId = fromUnit.getModelTypeId();
			}
			final ISourceUnitFactory factory = getFactory(modelTypeId);
			if (factory == null) {
				throw new UnsupportedOperationException(NLS.bind(
						"no factory for type ''{0}''", modelTypeId)); //$NON-NLS-1$
			}
			final ISourceUnit copy = factory.getUnit(from, modelTypeId, this, create);
			if (copy == null) {
				if (create) {
					throw new UnsupportedOperationException();
				}
				else {
					return null;
				}
			}
			copy.connect(monitor);
			if (fromUnit != null) {
				fromUnit.disconnect(null);
			}
			return copy;
		}
	}
	
	private ISourceUnitFactory getFactory(final String modelTypeId) {
		ISourceUnitFactory factory = fFactories.get(modelTypeId);
		if (factory == null) {
			try {
				final IConfigurationElement[] elements = Platform.getExtensionRegistry().
						getConfigurationElementsFor("de.walware.eclipsecommons.ltk.workingContexts"); //$NON-NLS-1$
				IConfigurationElement matchingElement = null;
				for (final IConfigurationElement element : elements) {
					if (element.getName().equals("unitType") && element.isValid()) { //$NON-NLS-1$
						final String typeIdOfElement = element.getAttribute(CONFIG_MODELTYPE_ID_ATTRIBUTE_NAME);
						final String contextKeyOfElement = element.getAttribute(CONFIG_CONTEXT_KEY_ATTRIBUTE_NAME);
						if (modelTypeId.equals(typeIdOfElement)) {
							if ((contextKeyOfElement == null) || (contextKeyOfElement.length() == 0)) {
								matchingElement = element;
								continue;
							}
							if (contextKeyOfElement.equals(fKey)) {
								matchingElement = element;
								break;
							}
						}
					}
				}
				if (matchingElement != null) {
					factory = (ISourceUnitFactory) matchingElement.createExecutableExtension("unitFactory"); //$NON-NLS-1$
					fFactories.put(modelTypeId, factory);
				}
			}
			catch (final Exception e) {
				BaseCorePlugin.logError(-1, "Error loading working context contributions", e); //$NON-NLS-1$
			}
		}
		return factory;
	}
	
}
