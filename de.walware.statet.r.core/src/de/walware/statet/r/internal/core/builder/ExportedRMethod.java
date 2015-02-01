/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.builder;

import java.io.Serializable;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.IRMethod;


public class ExportedRMethod extends ExportedRElement implements IRMethod, Serializable {
	
	
	private static final long serialVersionUID = -5410258006288951401L;
	
	
	private ArgsDefinition fArgs;
	
	
	public ExportedRMethod(final IRLangElement parent, final IRMethod sourceElement) {
		super(parent, sourceElement);
		fArgs = sourceElement.getArgsDefinition();
	}
	
	public ExportedRMethod() {
	}
	
	
	@Override
	public ArgsDefinition getArgsDefinition() {
		return fArgs;
	}
	
}
