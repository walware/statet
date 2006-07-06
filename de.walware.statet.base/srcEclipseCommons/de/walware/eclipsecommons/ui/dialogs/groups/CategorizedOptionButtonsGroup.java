/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.dialogs.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public abstract class CategorizedOptionButtonsGroup<ItemT extends CategorizedItem> 
		extends CategorizedOptionsGroup<ItemT>
		implements ButtonGroup.ButtonListener {

	
	protected ButtonGroup fButtonGroup;
	
	
	public CategorizedOptionButtonsGroup(String[] buttonLabels) {
		super(true, false);
	
		fButtonGroup = new ButtonGroup(buttonLabels, this);

	}

	@Override
	protected Control createOptionsControl(Composite parent, GridData gd) {
		
		Layouter options = new Layouter(new Composite(parent, SWT.NONE), 1);
		fButtonGroup.createGroup(options);
		
		return options.fComposite;
	}

	public abstract void handleButtonPressed(int buttonIdx);
	
}
