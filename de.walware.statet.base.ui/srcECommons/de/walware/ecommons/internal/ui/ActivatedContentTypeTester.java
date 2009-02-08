/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.internal.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;


/**
 * @see de.walware.ecommons.ltk.core.IExtContentTypeManager
 */
public class ActivatedContentTypeTester extends PropertyTester {
	
	
	public static final String MATCH_ACTIVATED_TYPE = "matchesActivatedContentType"; //$NON-NLS-1$
	public static final String MATCH_TYPE = "matchesContentType"; //$NON-NLS-1$
	
	
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (!(expectedValue instanceof String)) {
			return false;
		}
		IContentType contentType = null;
		
		// Search IFile
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
		if (file != null) {
			// get content type by IFile
			try {
				final IContentDescription contentDescription = file.getContentDescription();
				if (contentDescription != null) {
					contentType = contentDescription.getContentType();
				}
			}
			catch (final CoreException e) {}
		}
		else {
			// get content type by ISourceEditor
			if (receiver instanceof IAdaptable) {
				final ISourceEditor editor = (ISourceEditor) ((IAdaptable) receiver).getAdapter(ISourceEditor.class);
				if (editor != null) {
					final ISourceUnit sourceUnit = editor.getSourceUnit();
					if (sourceUnit != null) {
						final String modelTypeId = sourceUnit.getModelTypeId();
						final String contentTypeId = ECommonsLTK.getExtContentTypeManager().getContentTypeForModelType(modelTypeId);
						if (contentTypeId != null) {
							contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
						}
					}
				}
			}
		}
		
		if (property.equals(MATCH_ACTIVATED_TYPE)) {
			final String expectedContentTypeId = (String) expectedValue;
			if (contentType != null) {
				return ECommonsLTK.getExtContentTypeManager().matchesActivatedContentType(
						contentType.getId(), expectedContentTypeId, true);
			}
			return false;
		}
		if (property.equals(MATCH_TYPE)) {
			final String expectedContentTypeId = (String) expectedValue;
			while (contentType != null) {
				if (expectedContentTypeId.equals(contentType.getId())) {
					return true;
				}
				contentType = contentType.getBaseType();
			}
			return false;
		}
		return false;
	}
	
}
