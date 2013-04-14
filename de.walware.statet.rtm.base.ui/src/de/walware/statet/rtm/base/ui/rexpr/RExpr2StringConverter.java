/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.ui.rexpr;

import org.eclipse.core.databinding.conversion.IConverter;

import de.walware.statet.rtm.rtdata.types.RExpr;


public class RExpr2StringConverter implements IConverter {
	
	
	public RExpr2StringConverter() {
	}
	
	
	@Override
	public Object getFromType() {
		return RExpr.class;
	}
	
	@Override
	public Object getToType() {
		return String.class;
	}
	
	@Override
	public String convert(final Object fromObject) {
		if (fromObject == null) {
			return ""; //$NON-NLS-1$
		}
		return ((RExpr) fromObject).getExpr();
	}
	
}
