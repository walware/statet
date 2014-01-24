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

package de.walware.statet.rtm.base.ui.rexpr;

import org.eclipse.core.databinding.conversion.IConverter;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class String2RTypedExprConverter implements IConverter {
	
	
	private final String fType;
	
	
	public String2RTypedExprConverter() {
		this(RTypedExpr.R);
	}
	
	public String2RTypedExprConverter(final String type) {
		fType = type;
	}
	
	
	@Override
	public Object getFromType() {
		return String.class;
	}
	
	@Override
	public Object getToType() {
		return RTypedExpr.class;
	}
	
	@Override
	public RTypedExpr convert(final Object fromObject) {
		final String s = (String) fromObject;
		if (s == null || s.isEmpty()) {
			return null;
		}
		return new RTypedExpr(fType, (String) fromObject);
	}
	
}
