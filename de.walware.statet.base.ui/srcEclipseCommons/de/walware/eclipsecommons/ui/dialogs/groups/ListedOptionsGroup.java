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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


public abstract class ListedOptionsGroup<ItemT extends Object> extends StructuredSelectionOptionsGroup<ListViewer, ItemT> {


	public ListedOptionsGroup(boolean grabSelectionHorizontal,	boolean grabVertical) {
		
		super(grabSelectionHorizontal, grabVertical);
	}

	
	@Override
	protected ListViewer createSelectionViewer(Composite parent) {
		
		ListViewer viewer = new ListViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		viewer.setLabelProvider(createLabelProvider());
		
		return viewer;
	}
	
	protected abstract ILabelProvider createLabelProvider();
	
	@Override
	public void initFields() {
		
		super.initFields();
		getStructuredViewer().getList().select(0);
		reselect();
	}
}
