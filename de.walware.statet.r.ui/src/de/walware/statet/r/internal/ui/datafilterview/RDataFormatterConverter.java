/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.core.databinding.conversion.IConverter;

import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;


public class RDataFormatterConverter implements IConverter {
	
	
	private final Class fFromType;
	
	private final RDataFormatter fFormatter;
	
	
	public RDataFormatterConverter(final Class fromType, final RDataFormatter formatter) {
		fFromType = fromType;
		fFormatter = formatter;
	}
	
	
	@Override
	public Object getFromType() {
		return fFromType;
	}

	@Override
	public Object getToType() {
		return String.class;
	}

	@Override
	public Object convert(final Object fromObject) {
		return fFormatter.modelToDisplayValue(fromObject).toString();
	}
	
}
