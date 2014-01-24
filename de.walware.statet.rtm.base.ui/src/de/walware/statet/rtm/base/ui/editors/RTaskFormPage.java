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

package de.walware.statet.rtm.base.ui.editors;

import de.walware.ecommons.emf.ui.forms.EFFormPage;

import de.walware.statet.rtm.base.internal.ui.editors.Messages;


public abstract class RTaskFormPage extends EFFormPage {
	
	
	public RTaskFormPage(final RTaskEditor editor) {
		super(editor, RTaskEditor.MAIN_PAGE_ID, Messages.RTaskEditor_FirstPage_label);
	}
	
	public RTaskFormPage(final RTaskEditor editor, final String id, final String title) {
		super(editor, id, title);
	}
	
	
}
