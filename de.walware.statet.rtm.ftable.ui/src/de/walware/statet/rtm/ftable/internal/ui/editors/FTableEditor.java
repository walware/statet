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

package de.walware.statet.rtm.ftable.internal.ui.editors;

import org.eclipse.ui.PartInitException;

import de.walware.statet.rtm.base.ui.editors.RTaskEditor;
import de.walware.statet.rtm.ftable.ui.RtFTableDescriptor;


public class FTableEditor extends RTaskEditor {
	
	
	public FTableEditor() {
		super(RtFTableDescriptor.INSTANCE);
	}
	
	
	@Override
	protected void addFormPages() throws PartInitException {
		addPage(new MainPage(this));
	}
	
	
}
