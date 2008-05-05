/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.base.internal.core.BaseCorePlugin;


/**
 * 
 */
public final class WorkingContext {
	
	
	private final String fKey;
	private final Map<String, ISourceUnitFactory> fFactories;
	
	
	public WorkingContext(final String key) {
		fKey = key;
		fFactories = new HashMap<String, ISourceUnitFactory>();
	}
	
	
	public String getKey() {
		return fKey;
	}
	
	public ISourceUnit getUnit(final Object from, String typeId, final boolean create) {
		synchronized (this) {
			final ISourceUnit fromUnit = (from instanceof ISourceUnit) ?
				((ISourceUnit) from) : null;
			if (typeId == null && fromUnit != null) {
				typeId = fromUnit.getTypeId();
			}
			final ISourceUnitFactory factory = getFactory(typeId);
			if (factory == null) {
				throw new UnsupportedOperationException(NLS.bind(
						"no factory for type ''{0}''", typeId)); //$NON-NLS-1$
			}
			final ISourceUnit copy = factory.getUnit(from, typeId, this, create);
			if (copy == null) {
				if (create) {
					throw new UnsupportedOperationException();
				}
				else {
					return null;
				}
			}
			copy.connect();
			if (fromUnit != null) {
				fromUnit.disconnect();
			}
			return copy;
		}
	}
	
	private ISourceUnitFactory getFactory(final String typeId) {
		ISourceUnitFactory factory = fFactories.get(typeId);
		if (factory == null) {
			try {
				final IConfigurationElement[] elements = Platform.getExtensionRegistry().
						getConfigurationElementsFor("de.walware.statet.base.workingContexts"); //$NON-NLS-1$
				IConfigurationElement matchingElement = null;
				for (final IConfigurationElement element : elements) {
					if (element.getName().equals("unitType") && element.isValid()) { //$NON-NLS-1$
						final String typeIdOfElement= element.getAttribute("typeId"); //$NON-NLS-1$
						final String contextKeyOfElement = element.getAttribute("contextKey"); //$NON-NLS-1$
						if (typeId.equals(typeIdOfElement)) {
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
					fFactories.put(typeId, factory);
				}
			}
			catch (final Exception e) {
				BaseCorePlugin.logError(-1, "Error loading working context contributions", e); //$NON-NLS-1$
			}
		}
		return factory;
	}
	
}
