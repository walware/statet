/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.variables;

import org.eclipse.debug.internal.ui.model.elements.VariableEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Composite;


public class RVariableEditor extends VariableEditor {
	
	
	public RVariableEditor() {
	}
	
	
	@Override
	public CellEditor getCellEditor(final IPresentationContext context, final String columnId,
			final Object element, final Composite parent) {
		return super.getCellEditor(context, columnId, element, parent);
	}
	
	@Override
	public ICellModifier getCellModifier(final IPresentationContext context,
			final Object element) {
		return new RVariableCellModifier();
	}
	
}
