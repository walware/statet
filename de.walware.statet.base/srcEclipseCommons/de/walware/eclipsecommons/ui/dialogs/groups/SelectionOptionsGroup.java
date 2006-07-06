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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public abstract class SelectionOptionsGroup<ItemT extends SelectionItem> implements OptionsGroup {

	private boolean fGrabSelectionHorizontal;
	private boolean fGrabVertical;
	
	public List<ItemT> fSelectionModel = new ArrayList<ItemT>();
	public Composite fComposite;
	
	public SelectionOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		
		fGrabSelectionHorizontal = grabSelectionHorizontal;
		fGrabVertical = grabVertical;
	}
	
	public SelectionOptionsGroup() {
		
		this(false, false);
	}
	

	public void createGroup(Layouter parent) {
		
		Layouter layouter = new Layouter(new Composite(parent.fComposite, SWT.NONE), 2);
		fComposite = layouter.fComposite;
		fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, fGrabVertical, parent.fNumColumns, 1));
				
		GridData gd = new GridData(SWT.FILL, SWT.FILL, fGrabSelectionHorizontal, true);
		Control selectionControl = createSelectionControl(fComposite, gd);
		selectionControl.setLayoutData(gd);

		gd = new GridData(SWT.FILL, SWT.FILL, !fGrabSelectionHorizontal, true);
		Control optionControl = createOptionsControl(fComposite, gd);
		optionControl.setLayoutData(gd);
		
	}

	protected abstract Control createSelectionControl(Composite parent, GridData gd);

	protected abstract Control createOptionsControl(Composite parent, GridData gd);
	
	/**
	 * Standard-Implementierung macht nichts.
	 */
	public void initFields() {

	}

	/**
	 * Selection-change in List
	 * <p>
	 * Default-Implementierung macht nichts.
	 */
	public void handleListSelection() {
		
	}

}
