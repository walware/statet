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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

import de.walware.statet.r.debug.core.IRVariable;


public class RVariableLabelProvider extends VariableLabelProvider {
	
	
	public RVariableLabelProvider() {
	}
	
	
	@Override
	protected String getValueText(final IVariable variable, final IValue value,
			final IPresentationContext context) throws CoreException {
		if (variable instanceof IRVariable) {
			return value.getValueString();
		}
		return super.getValueText(variable, value, context);
	}
	
}
