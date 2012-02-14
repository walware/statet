/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.rdata;

import java.util.Collections;
import java.util.List;

import de.walware.rj.data.RList;
import de.walware.rj.data.RStore;

import de.walware.statet.r.core.model.IRLangElement;
import de.walware.statet.r.core.model.RElementName;


public final class RPromiseVar extends CombinedElement {
	
	
	public RPromiseVar() {
	}
	
	public RPromiseVar(final CombinedElement parent, final RElementName name) {
		fParent = parent;
		fElementName = name;
	}
	
	
	@Override
	public byte getRObjectType() {
		return TYPE_PROMISE;
	}
	
	@Override
	public String getRClassName() {
		return "<promise>";
	}
	
	@Override
	public int getLength() {
		return 0;
	}
	
	@Override
	public RStore getData() {
		return null;
	}
	
	@Override
	public RList getAttributes() {
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
		return Collections.EMPTY_LIST;
	}
	
}
