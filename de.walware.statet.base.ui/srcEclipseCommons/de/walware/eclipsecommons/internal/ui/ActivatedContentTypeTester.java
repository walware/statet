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

package de.walware.eclipsecommons.internal.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import de.walware.statet.base.core.StatetCore;


/**
 * @see de.walware.eclipsecommons.ltk.core.IExtContentTypeManager
 */
public class ActivatedContentTypeTester extends PropertyTester {
	
	
	public static final String MATCH_ACTIVATED_TYPE = "matchesActivatedContentType"; //$NON-NLS-1$
	public static final String MATCH_TYPE = "matchesContentType"; //$NON-NLS-1$
	
	
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (!(expectedValue instanceof String)) {
			return false;
		}
		IFile file = null;
		if (receiver instanceof IFile) {
			file = (IFile) receiver;
		}
		else if (receiver instanceof IAdaptable) {
			final IAdaptable adaptableReceiver = (IAdaptable) receiver;
			file = (IFile) adaptableReceiver.getAdapter(IFile.class);
			if (file == null) {
				final IResource resource = (IResource) adaptableReceiver.getAdapter(IResource.class);
				if (resource instanceof IFile) {
					file = (IFile) resource;
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
		}
		if (file == null) {
			IEditorInput editorInput;
			if (receiver instanceof IEditorInput) {
				editorInput = (IEditorInput) receiver;
			}
			else if (receiver instanceof IEditorPart) {
				editorInput = ((IEditorPart) receiver).getEditorInput();
			}
			else {
				editorInput = null;
			}
			if (editorInput != null) {
				file = (IFile) editorInput.getAdapter(IFile.class);
			}
		}
		
		if (property.equals(MATCH_ACTIVATED_TYPE)) {
			if (file != null) {
				try {
					final IContentDescription contentDescription = file.getContentDescription();
					if (contentDescription != null) {
						final String expectedContentTypeId = (String) expectedValue;
						final IContentType contentType = contentDescription.getContentType();
						if (contentType != null) {
							return StatetCore.getExtContentTypeManager().matchesActivatedContentType(
									contentType.getId(), expectedContentTypeId, true);
						}
					}
				}
				catch (final CoreException e) {
				}
			}
			return false;
		}
		if (property.equals(MATCH_TYPE)) {
			if (file != null) {
				try {
					final IContentDescription contentDescription = file.getContentDescription();
					if (contentDescription != null) {
						final String expectedContentTypeId = (String) expectedValue;
						IContentType testType = contentDescription.getContentType();
						while (testType != null) {
							if (expectedContentTypeId.equals(testType.getId())) {
								return true;
							}
							testType = testType.getBaseType();
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
