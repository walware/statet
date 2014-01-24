/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.emf.internal.forms;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.editor.IFormPage;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.emf.ui.forms.EFEditor;
import de.walware.ecommons.ui.util.PostSelectionProviderProxy;


public class EditorSelectionProvider extends PostSelectionProviderProxy {
	
	
	private final EFEditor fEditor;
	
	
	public EditorSelectionProvider(final EFEditor editor) {
		fEditor = editor;
	}
	
	
	public EFEditor getEditor() {
		return fEditor;
	}
	
	private ISelectionProvider getActiveSelectionProvider() {
		final IFormPage page = getEditor().getActivePageInstance();
		{	final ISelectionProvider selectionProvider = (ISelectionProvider) page.getAdapter(ISelectionProvider.class);
			if (selectionProvider != null) {
				return selectionProvider;
			}
		}
		{	final IEditorPart activeEditor = fEditor.getActiveEditor();
			if (activeEditor != null) {
				final ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
				if (selectionProvider != null) {
					return selectionProvider;
				}
			}
		}
		return null;
	}
	
	public void update() {
		setSelectionProvider(getActiveSelectionProvider());
	}
	
	@Override
	protected ISelection getSelection(final ISelection originalSelection) {
		final ISelection selection = super.getSelection(originalSelection);
		if (selection instanceof IStructuredSelection) {
			final EObject base = (EObject) getEditor().getDataBinding().getBaseObservable().getValue();
			final List list = ((IStructuredSelection) selection).toList();
			if (base != null && !list.contains(base)) {
				return new StructuredSelection(
						ConstList.<Object>concat(base, list) );
			}
		}
		return selection;
	}
	
}
