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

package de.walware.statet.base.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import de.walware.statet.base.core.StatetCore;


/**
 * @see de.walware.statet.core.IExtContentTypeManager
 */
public class ActivatedContentTypeTester extends PropertyTester {
	
	
	public static final String MATCH_ACTIVATEC_TYPE = "matchesActivatedContentType"; //$NON-NLS-1$
	
	
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (!(expectedValue instanceof String)) {
			return false;
		}
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
//			IPathEditorInput editorInput = (IPathEditorInput) ((IAdaptable) receiver).getAdapter(IPathEditorInput.class);
//			
//						if (editorInput != null) {
//							IPath path= editorInput.getPath();
//							File file= path.toFile();
//							if (file.exists()) {
//								try {
//									FileReader reader= new FileReader(file);
//									IContentType contentType= Platform.getContentTypeManager().getContentType((String)expectedValue);
//									IContentDescription description= contentType.getDescriptionFor(reader, IContentDescription.ALL);
//									reader.close();
//									if (description != null) {
//										return matchesContentType(description.getContentType(), (String)expectedValue);
//									}
//								} catch (FileNotFoundException e) {
//									return false;
//								} catch (IOException e) {
//									return false;
//								}
//							}
//						}
//					}
		
		if (property.equals(MATCH_ACTIVATEC_TYPE)) {
			if (file != null) {
				try {
					final IContentDescription contentDescription = file.getContentDescription();
					if (contentDescription != null) {
						final IContentType contentType = contentDescription.getContentType();
						if (contentType != null) {
							return StatetCore.getExtContentTypeManager().matchesActivatedContentType(contentType.getId(), (String) expectedValue, true);
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
