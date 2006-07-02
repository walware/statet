/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.internal;

import org.eclipse.osgi.util.NLS;

import de.walware.eclipsecommon.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommon.ui.dialogs.Layouter;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.internal.actions.AbstractHistoryPage;


/**
 *
 */
public class LoadHistoryPage extends AbstractHistoryPage {

	
	public LoadHistoryPage(ToolProcess tool) {
		
		super("LoadHistoryPage", Messages.LoadHistoryPage_title, tool); //$NON-NLS-1$
		setDescription(NLS.bind(Messages.LoadHistoryPage_description, fTool.getToolLabel(false)));
	}
	
	protected ChooseResourceComposite createResourceComposite(Layouter layouter) {
		
		return new ChooseResourceComposite(layouter.fComposite, ChooseResourceComposite.MODE_NEW_ERROR, 
				Messages.LoadHistoryPage_FileTask_label);
	}
	
}
