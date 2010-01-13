/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.actions.AbstractHistoryPage;


public class LoadHistoryPage extends AbstractHistoryPage {
	
	
	public LoadHistoryPage(final ToolProcess tool) {
		super("LoadHistoryPage", Messages.LoadHistoryPage_title, tool); //$NON-NLS-1$
		setDescription(NLS.bind(Messages.LoadHistoryPage_description, fTool.getToolLabel(false)));
	}
	
	
	@Override
	protected ResourceInputComposite createResourceInputComposite(final Composite composite) {
		return new ResourceInputComposite(composite, 
				ResourceInputComposite.STYLE_COMBO,
				ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_OPEN, 
				Messages.LoadSaveHistoryPage_File_label);
	}
	
}
