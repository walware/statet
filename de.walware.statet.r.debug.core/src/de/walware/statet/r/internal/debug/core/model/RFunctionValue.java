/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.annotation.NonNullByDefault;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;
import de.walware.statet.r.core.model.IRMethod;


@NonNullByDefault
public class RFunctionValue extends RElementVariableValue<ICombinedRElement> {
	
	
	public RFunctionValue(final RElementVariable variable) {
		super(variable);
	}
	
	
	@Override
	public String getValueString() throws DebugException {
		if (this.element instanceof IRMethod) {
			final IRMethod lang= (IRMethod) this.element;
			final StringBuilder sb= new StringBuilder();
			final ArgsDefinition args= lang.getArgsDefinition();
			sb.append("function("); //$NON-NLS-1$
			if (args == null) {
				sb.append("<unknown>"); //$NON-NLS-1$
			}
			else if (args.size() > 0) {
				{	final Arg arg= args.get(0);
					if (arg.name != null) {
						sb.append(arg.name);
					}
				}
				{	for (int i= 1; i < args.size(); i++) {
						sb.append(", "); //$NON-NLS-1$
						final Arg arg= args.get(i);
						if (arg.name != null) {
							sb.append(arg.name);
						}
					}
				}
			}
			sb.append(")"); //$NON-NLS-1$
			return sb.toString();
		}
		return super.getValueString();
	}
	
}
