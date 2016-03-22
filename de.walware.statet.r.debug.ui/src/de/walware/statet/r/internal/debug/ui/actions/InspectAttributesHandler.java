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

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;


@NonNullByDefault
public class InspectAttributesHandler extends InspectHandler {
	
	
	public InspectAttributesHandler() {
	}
	
	
	@Override
	protected String getCommandId() {
		return "de.walware.statet.r.commands.InspectAttributes"; //$NON-NLS-1$
	}
	
	@Override
	protected String toCommandExpression(final String expression) {
		return "attributes(" + expression + ')'; //$NON-NLS-1$
	}
	
}
