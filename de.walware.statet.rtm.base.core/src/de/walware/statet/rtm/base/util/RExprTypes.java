/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.util;

import java.util.List;

import de.walware.ecommons.collections.ConstArrayList;


public class RExprTypes {
	
	
	private final List<RExprType> fTypes;
	
	private final String fDefaultTypeKey;
	
	
	public RExprTypes(final RExprType type) {
		this(new ConstArrayList<RExprType>(type), type.getTypeKey());
	}
	
	public RExprTypes(final List<RExprType> types, final int defaultTypeIdx) {
		fTypes = types;
		fDefaultTypeKey = types.get(defaultTypeIdx).getTypeKey();
	}
	
	public RExprTypes(final List<RExprType> types, final String defaultType) {
		fTypes = types;
		fDefaultTypeKey = defaultType;
	}
	
	
	public List<RExprType> getTypes() {
		return fTypes;
	}
	
	public String getDefaultTypeKey() {
		return fDefaultTypeKey;
	}
	
	public boolean contains(final String typeKey) {
		for (int i = 0; i < fTypes.size(); i++) {
			if (fTypes.get(i).getTypeKey() == typeKey) {
				return true;
			}
		}
		return false;
	}
	
}
