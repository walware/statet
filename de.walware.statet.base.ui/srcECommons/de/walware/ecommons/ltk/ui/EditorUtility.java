/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiEditorInput;


/**
 * A number of routines for working with JavaElements in editors.
 *
 * Use 'isOpenInEditor' to test if an element is already open in a editor
 * Use 'openInEditor' to force opening an element in a editor
 * With 'getWorkingCopy' you get the working copy (element in the editor) of an element
 */
public class EditorUtility {
	
	
	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * @param skipNonResourceEditors if <code>true</code>, editors whose inputs do not adapt to {@link IResource}
	 * are not saved
	 *
	 * @return an array of dirty editor parts
	 * @since 3.4
	 */
	public static List<IEditorPart> getDirtyEditors(final boolean skipNonResourceEditors) {
		final Set inputs = new HashSet();
		final List<IEditorPart> result = new ArrayList<IEditorPart>(0);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			final IWorkbenchPage[] pages= windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				final IEditorPart[] editors= pages[x].getDirtyEditors();
				for (int z = 0; z < editors.length; z++) {
					final IEditorPart ep = editors[z];
					final IEditorInput input = ep.getEditorInput();
					if (inputs.add(input)) {
						if (!skipNonResourceEditors || isResourceEditorInput(input)) {
							result.add(ep);
						}
					}
				}
			}
		}
		return result;
	}
	
	private static boolean isResourceEditorInput(final IEditorInput input) {
		if (input instanceof MultiEditorInput) {
			final IEditorInput[] inputs= ((MultiEditorInput) input).getInput();
			for (int i= 0; i < inputs.length; i++) {
				if (inputs[i].getAdapter(IResource.class) != null) {
					return true;
				}
			}
		} 
		else if (input.getAdapter(IResource.class) != null) {
			return true;
		}
		return false;
	}
	
}
