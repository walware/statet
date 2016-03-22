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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.variables.details.DetailPaneAssignValueAction;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier;
import org.eclipse.debug.ui.IDebugUIConstants;

import de.walware.statet.r.debug.core.IRVariable;


public class RVariableCellModifier extends DefaultVariableCellModifier {
	
	
	public RVariableCellModifier() {
	}
	
	
//	@Override
//	public boolean canModify(Object element, String property) {
//		return super.canModify(element, property);
//	}
	
	@Override
	public Object getValue(final Object element, final String property) {
		if (IDebugUIConstants.COLUMN_ID_VARIABLE_VALUE.equals(property)) {
			if (element instanceof IRVariable) {
				final IRVariable variable= (IRVariable) element;
				try {
					return variable.getValue().getValueString();
				}
				catch (final DebugException e) {
					DebugUIPlugin.log(e);
				}
				return null;
			}
		}
		return super.getValue(element, property);
	}
	
	@Override
	public void modify(final Object element, final String property, final Object value) {
		if (IDebugUIConstants.COLUMN_ID_VARIABLE_VALUE.equals(property)) {
			if (element instanceof IRVariable) {
				final IRVariable variable= (IRVariable) element;
				final Object oldValue= getValue(element, property);
				if (!value.equals(oldValue)) {
					if (value instanceof String) {
						DetailPaneAssignValueAction.assignValue(DebugUIPlugin.getShell(), variable,
								(String) value );
					}
				}
				return;
			}
		}
		super.modify(element, property, value);
	}
	
}
