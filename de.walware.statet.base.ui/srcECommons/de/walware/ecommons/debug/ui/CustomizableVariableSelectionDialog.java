/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * {@link StringVariableSelectionDialog} with option to exclude and add variables.
 */
public class CustomizableVariableSelectionDialog extends StringVariableSelectionDialog {
	
	
	private List<VariableFilter> fFilters = new ArrayList<VariableFilter>();
	private List<IStringVariable> fAdditionals = new ArrayList<IStringVariable>();
	
	private boolean fInitialized;
	private Object[] fElements;
	
	
	public CustomizableVariableSelectionDialog(final Shell parent) {
		super(parent);
		
		fInitialized = true;
	}
	
	
	@Override
	public void setElements(final Object[] elements) {
		fElements = elements;
		if (fInitialized) {
			initElements();
		}
	}
	
	private void initElements() {
		final IStringVariable[] orginals = (IStringVariable[]) fElements;
		final List<IStringVariable> filteredList = new ArrayList<IStringVariable>(fElements.length);
		filteredList.addAll(fAdditionals);
		
		ITER_VAR: for (final IStringVariable variable : orginals) {
			if (variable instanceof IDynamicVariable) {
				for (final VariableFilter filter : fFilters) {
					if (filter.exclude(variable)) {
						continue ITER_VAR;
					}
				}
			}
			filteredList.add(variable);
		}
		super.setElements(filteredList.toArray(new IStringVariable[filteredList.size()]));
	}
	
	@Override
	public int open() {
		initElements();
		return super.open();
	}
	
	
	public void addAdditional(final IStringVariable variable) {
		fAdditionals.add(variable);
	}
	
	public void addAdditionals(final List<IStringVariable> variables) {
		fAdditionals.addAll(variables);
	}
	
	public void addFilter(final VariableFilter filter) {
		fFilters.add(filter);
	}
	
	public void addFilters(final List<VariableFilter> filters) {
		fFilters.addAll(filters);
	}
	
}
