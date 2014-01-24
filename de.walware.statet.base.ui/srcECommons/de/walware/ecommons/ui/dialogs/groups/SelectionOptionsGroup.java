/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.ui.dialogs.groups;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;



public abstract class SelectionOptionsGroup<ItemT extends Object> implements OptionsGroup {

	private final boolean fGrabSelectionHorizontal;
	private final boolean fGrabVertical;
	
	private final List<ItemT> fSelectionModel = new ArrayList<ItemT>();
	private Composite fComposite;
	
	
	public SelectionOptionsGroup(final boolean grabSelectionHorizontal, final boolean grabVertical) {
		fGrabSelectionHorizontal = grabSelectionHorizontal;
		fGrabVertical = grabVertical;
	}
	

	public List<ItemT> getListModel() {
		return fSelectionModel;
	}


	@Override
	public void createGroup(final Composite parent, final int hSpan) {
		final Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), 2);
		fComposite = layouter.composite;
		fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, fGrabVertical, hSpan, 1));
				
		final Control selectionControl = createSelectionControl(fComposite);
		selectionControl.setLayoutData(createSelectionGridData());

		final Control optionControl = createOptionsControl(fComposite);
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
	@Override
	public void initFields() {
	}

}
