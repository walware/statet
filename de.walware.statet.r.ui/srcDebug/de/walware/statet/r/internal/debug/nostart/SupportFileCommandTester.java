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

package de.walware.statet.r.internal.debug.nostart;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.statushandlers.StatusManager;


/**
 * 
 */
public class SupportFileCommandTester extends PropertyTester {
	
	
	public static final String SUPPORTS_FILECOMMAND = "supportsFileCommand"; //$NON-NLS-1$
	
	// Local copy of constants to avoid plug-in activation
	private static final String PLUGIN_ID = "de.walware.statet.r.ui"; //$NON-NLS-1$
	private static final String R_CONTENT = "de.walware.statet.r.contentTypes.R"; //$NON-NLS-1$
	
	private static final String CONTENTHANDLER_EXTENSION_POINT = "rCodeLaunchContentHandler"; //$NON-NLS-1$
	private static final String CONTENTHANDLER_ELEMENT = "contentHandler"; //$NON-NLS-1$
	private static final String CONTENT_FILECOMMAND_ELEMENT = "fileCommand"; //$NON-NLS-1$
	private static final String ATT_CONTENT_TYPE = "contentTypeId"; //$NON-NLS-1$
	
	
	private final Set<String> fSupportedContentTypeIds = new HashSet<String>();
	
	
	public SupportFileCommandTester() {
		updateSettings();
	}
	
	private void updateSettings() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(PLUGIN_ID, CONTENTHANDLER_EXTENSION_POINT);
		
		synchronized (this) {
			fSupportedContentTypeIds.clear();
			fSupportedContentTypeIds.add(R_CONTENT);
			for (int i = 0; i < elements.length; i++) {
				try {
					if (elements[i].getName().equals(CONTENTHANDLER_ELEMENT)) {
						if (elements[i].getChildren(CONTENT_FILECOMMAND_ELEMENT).length > 0) {
							fSupportedContentTypeIds.add(elements[i].getAttribute(ATT_CONTENT_TYPE));
						}
					}
				}
				catch (final Exception e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, PLUGIN_ID, -1,
							"An error occurred when loading supported content types for file command", e)); //$NON-NLS-1$
				}
			}
		}
	}
	
	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		IFile file = null;
		if (receiver instanceof IFile) {
			file = (IFile) receiver;
		}
		else if (receiver instanceof IAdaptable) {
			file = (IFile) ((IAdaptable) receiver).getAdapter(IFile.class);
			if (file == null) {
				final IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
				if (resource instanceof IFile) {
					file = (IFile) resource;
				}
			}
		}
		
		if (property.equals(SUPPORTS_FILECOMMAND)) {
			if (file != null) {
				try {
					final IContentDescription contentDescription = file.getContentDescription();
					if (contentDescription != null) {
						final IContentType contentType = contentDescription.getContentType();
						if (contentType != null) {
							return fSupportedContentTypeIds.contains(contentType.getId());
						}
					}
				}
				catch (final CoreException e) {
				}
			}
			return false;
		}
		return false;
	}
	
}
