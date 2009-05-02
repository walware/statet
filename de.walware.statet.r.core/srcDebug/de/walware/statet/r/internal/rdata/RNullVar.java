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

package de.walware.statet.r.internal.rdata;

import java.util.Collections;
import java.util.List;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;

import de.walware.rj.data.RList;
import de.walware.rj.data.RStore;


public final class RNullVar extends CombinedElement {
	
	
	public RNullVar() {
	}
	
	public RNullVar(final CombinedElement parent, final IElementName name) {
		fParent = parent;
		fElementName = name;
	}
	
	
	public int getRObjectType() {
		return TYPE_NULL;
	}
	
	public String getRClassName() {
		return "NULL";
	}
	
	public int getLength() {
		return 0;
	}
	
	public RStore getData() {
		return null;
	}
	
	public RList getAttributes() {
		return null;
	}
	
	
	public int getElementType() {
		return R_GENERAL_VARIABLE;
	}
	
	public boolean hasChildren(final Filter filter) {
		return false;
	}
	
	public List<? extends IModelElement> getChildren(final Filter filter) {
		return Collections.EMPTY_LIST;
	}
	
}
