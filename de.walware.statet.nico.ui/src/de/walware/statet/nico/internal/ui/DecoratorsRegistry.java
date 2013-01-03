/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.walware.ecommons.ts.ui.IToolRunnableDecorator;
import de.walware.ecommons.ui.util.UIAccess;



public class DecoratorsRegistry {
	
	
	private static final String DECORATORS_EXTENSION_POINT = "de.walware.statet.nico.uiDecorators"; //$NON-NLS-1$
	
	private static final String RUNNABLE_ELEMENT_NAME = "runnable"; //$NON-NLS-1$
	
	private static final String RUNNABLE_ID_ATTRIBUTE_NAME = "typeId"; //$NON-NLS-1$
	private static final String ICON_ATTRIBUTE_NAME = "icon"; //$NON-NLS-1$
	
	
	private class RunnableDecorator implements IToolRunnableDecorator {
		
		ImageDescriptor iconDescriptor;
		
		Image icon;
		
		
		@Override
		public Image getImage() {
			if (icon == null && iconDescriptor != null) {
				icon = fManager.createImageWithDefault(iconDescriptor);
			}
			return icon;
		}
		
	}
	
	private final HashMap<String, RunnableDecorator> fRunnableIconMap = new HashMap<String, RunnableDecorator>();
	
	private final Display fDisplay;
	private ResourceManager fManager;
	
	
	public DecoratorsRegistry() {
		fDisplay = UIAccess.getDisplay();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				fManager = JFaceResources.getResources(fDisplay);
				fDisplay.disposeExec(new Runnable() {
					@Override
					public void run() {
						DecoratorsRegistry.this.dispose();
					}
				});
				DecoratorsRegistry.this.load();
			}
		};
		if (fDisplay.getThread() == Thread.currentThread()) {
			runnable.run();
		}
		else {
			fDisplay.syncExec(runnable);
		}
	}
	
	
	private void load() {
		final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(DECORATORS_EXTENSION_POINT);
		for (final IConfigurationElement configuration : configurationElements) {
			if (configuration.getName().equals(RUNNABLE_ELEMENT_NAME)) {
				final String typeId = configuration.getAttribute(RUNNABLE_ID_ATTRIBUTE_NAME);
				if (typeId == null || typeId.isEmpty()) {
					// TODO log
				}
				else {
					final RunnableDecorator decorators = new RunnableDecorator();
					final String icon = configuration.getAttribute(ICON_ATTRIBUTE_NAME);
					final String namespaceId = configuration.getNamespaceIdentifier();
					if (icon != null && icon.length() > 0) {
						decorators.iconDescriptor = AbstractUIPlugin
								.imageDescriptorFromPlugin(namespaceId, icon);
					}
					fRunnableIconMap.put(typeId, decorators);
				}
			}
		}
	}
	
	
	public IToolRunnableDecorator getDecoratorForRunnable(final String typeId) {
		return fRunnableIconMap.get(typeId);
	}
	
	private void dispose() {
		for (final RunnableDecorator decorator : fRunnableIconMap.values()) {
			if (decorator.icon != null) {
				fManager.destroyImage(decorator.iconDescriptor);
			}
		}
		fRunnableIconMap.clear();
	}
	
}
