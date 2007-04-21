/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.dialogs.groups;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public abstract class SelectionOptionsGroup<ItemT extends Object> implements OptionsGroup {

	private boolean fGrabSelectionHorizontal;
	private boolean fGrabVertical;
	
	private List<ItemT> fSelectionModel = new ArrayList<ItemT>();
	private Composite fComposite;
	
	
	public SelectionOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		
		fGrabSelectionHorizontal = grabSelectionHorizontal;
		fGrabVertical = grabVertical;
	}
	

	public List<ItemT> getListModel() {
		return fSelectionModel;
	}


	public void createGroup(Layouter parent) {
		
		Layouter layouter = new Layouter(new Composite(parent.composite, SWT.NONE), 2);
		fComposite = layouter.composite;
		fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, fGrabVertical, parent.fNumColumns, 1));
				
		Control selectionControl = createSelectionControl(fComposite);
		selectionControl.setLayoutData(createSelectionGridData());

		Control optionControl = createOptionsControl(fComposite);
		optionControl.setLayoutData(createOptionsGridData());
	}

	protected abstract Control createSelectionControl(Composite parent);
	
	protected GridData createSelectionGridData() {
		
		return new GridData(SWT.FILL, SWT.FILL, fGrabSelectionHorizontal, true);
	}

	protected abstract Control createOptionsControl(Composite parent);
	
	protected GridData createOptionsGridData() {
		
		return new GridData(SWT.FILL, SWT.FILL, !fGrabSelectionHorizontal, true);
	}
	
	/**
	 * Standard-Implementierung macht nichts.
	 */
	public void initFields() {

	}

}
