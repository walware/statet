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

package de.walware.statet.r.internal.debug.core.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRElementVariable;
import de.walware.statet.r.debug.core.IRThread;
import de.walware.statet.r.debug.core.IRVariable;


@NonNullByDefault
public class RElementVariableProxy extends RVariableProxy implements IRElementVariable {
	
	
	private volatile @Nullable RElementName fqName;
	
	
	public RElementVariableProxy(final IRElementVariable variable, final IRVariable parent) {
		super(variable, parent);
	}
	
	
	@Override
	public final IRThread getThread() {
		return ((IRElementVariable) this.variable).getThread();
	}
	
	@Override
	public final ICombinedRElement getElement() {
		return ((IRElementVariable) this.variable).getElement();
	}
	
	@Override
	public @Nullable RElementName getFQElementName() {
		RElementName name= this.fqName;
		if (name == null) {
			name= this.fqName= RElementVariable.createFQElementName(this);
		}
		return name;
	}
	
}
