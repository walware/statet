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

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.ui.action.RedoAction;
import org.eclipse.emf.edit.ui.action.UndoAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;


public class EFEditorActionBarContributor extends MultiPageEditorActionBarContributor 
		implements IPropertyListener {
	
	
	private IEditorPart fActiveEditor;
	
	private UndoAction fUndoAction;
	private RedoAction fRedoAction;
	
	
	public EFEditorActionBarContributor() {
	}
	
	@Override
	public void init(final IActionBars bars, final IWorkbenchPage page) {
		super.init(bars, page);
		
		final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		fUndoAction = new UndoAction();
		fUndoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), fUndoAction);
		
		fRedoAction = new RedoAction();
		fRedoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), fRedoAction);
	}
	
	
	IEditorPart getActiveEditor() {
		return fActiveEditor;
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		if (!(part instanceof IEditingDomainProvider)) {
			part = null;
		}
		if (fActiveEditor != null) {
			fActiveEditor.removePropertyListener(this);
		}
		fActiveEditor = part;
		if (part != null) {
			part.addPropertyListener(this);
		}
		
		fUndoAction.setActiveWorkbenchPart(part);
		fRedoAction.setActiveWorkbenchPart(part);
		
		update();
	}
	
	@Override
	public void setActivePage(final IEditorPart activeEditor) {
	}
	
	@Override
	public void propertyChanged(final Object source, final int propId) {
		update();
	}
	
	public void update() {
		fUndoAction.update();
		fRedoAction.update();
	}
	
	
	public void shareGlobalActions(final IPage page, final IActionBars actionBars) {
	}
	
}
