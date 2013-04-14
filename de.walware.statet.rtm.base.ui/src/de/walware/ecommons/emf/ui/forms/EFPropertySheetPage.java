/*******************************************************************************
 * Copyright (c) 2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;


public class EFPropertySheetPage extends TabbedPropertySheetPage {
	
	
	private final EFEditor fEditor;
	
	
	public EFPropertySheetPage(final EFEditor editor) {
		super(editor);
		
		fEditor = editor;
	}
	
	
	public EFEditor getEditor() {
		return fEditor;
	}
	
	@Override
	public void setActionBars(final IActionBars actionBars) {
		super.setActionBars(actionBars);
		fEditor.getActionBarContributor().shareGlobalActions(this, actionBars);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		fEditor.onPropertySheetDisposed(this);
	}
	
}
