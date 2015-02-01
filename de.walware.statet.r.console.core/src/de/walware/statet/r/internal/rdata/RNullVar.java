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

package de.walware.statet.r.internal.rdata;

import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RStore;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class RNullVar extends CombinedElement {
	
	
	public RNullVar(final CombinedElement parent, final RElementName name) {
		super(parent, name);
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_NULL;
	}
	
	@Override
	public String getRClassName() {
		return CLASSNAME_NULL;
	}
	
	@Override
	public long getLength() {
		return 0;
	}
	
	@Override
	public RStore getData() {
		return null;
	}
	
	
	@Override
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IRLangElement> getModelChildren(final Filter filter) {
		return Collections.emptyList();
	}
	
	
	@Override
	public String toString() {
		return "RObject type=NULL";
	}
	
}
