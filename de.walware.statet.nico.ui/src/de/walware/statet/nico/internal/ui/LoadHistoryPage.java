/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.ecommons.ui.dialogs.Layouter;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.actions.AbstractHistoryPage;


/**
 * 
 */
public class LoadHistoryPage extends AbstractHistoryPage {
	
	
	public LoadHistoryPage(final ToolProcess tool) {
		super("LoadHistoryPage", Messages.LoadHistoryPage_title, tool); //$NON-NLS-1$
		setDescription(NLS.bind(Messages.LoadHistoryPage_description, fTool.getToolLabel(false)));
	}
	
	
	@Override
	protected ChooseResourceComposite createResourceComposite(final Layouter layouter) {
		return new ChooseResourceComposite(layouter.composite, 
				ChooseResourceComposite.STYLE_COMBO,
				ChooseResourceComposite.MODE_FILE | ChooseResourceComposite.MODE_OPEN, 
				Messages.LoadSaveHistoryPage_File_label);
	}
	
}
