/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.ui.editors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import de.walware.statet.base.ui.IStatetUIMenuIds;
import de.walware.statet.rtm.base.internal.ui.editors.Messages;
import de.walware.statet.rtm.base.ui.RtModelUIPlugin;

import de.walware.statet.r.internal.ui.editors.REditor;


public class RCodePage extends REditor implements IFormPage {
	
	
	public static final String ID = "RCode"; //$NON-NLS-1$
	
	
	private RTaskEditor fEditor;
	private int fIndex;
	
	private Control fControl;
	
	
	public RCodePage(final RTaskEditor editor) {
		initialize(editor);
		setPartName(Messages.RTaskEditor_RCodePage_label);
	}
	
	
	@Override
	public void initialize(final FormEditor editor) {
		fEditor = (RTaskEditor) editor;
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
//		setHelpContextId();
		setEditorContextMenuId("de.walware.statet.rtm.menus.RCodePageContextMenu"); //$NON-NLS-1$
	}
	
	@Override
	public FormEditor getEditor() {
		return fEditor;
	}
	
	@Override
	public IManagedForm getManagedForm() {
		return null;
	}
	
	@Override
	public void setActive(final boolean active) {
	}
	
	@Override
	public boolean isActive() {
		return (this == fEditor.getActivePageInstance());
	}
	
	@Override
	public boolean canLeaveThePage() {
		return true;
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		fControl = (Control) getAdapter(Control.class);
	}
	
	@Override
	public Control getPartControl() {
		return fControl;
	}
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public int getIndex() {
		return fIndex;
	}
	
	@Override
	public void setIndex(final int index) {
		fIndex = index;
	}
	
	@Override
	public boolean isEditor() {
		return true;
	}
	
	@Override
	public boolean selectReveal(final Object object) {
		return false;
	}
	
	@Override
	protected void createUndoRedoActions() {
	}
	
	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager m) {
		super.editorContextMenuAboutToShow(m);
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_SUBMIT_MENU_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RtModelUIPlugin.RUN_R_TASK_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
	}
	
}
