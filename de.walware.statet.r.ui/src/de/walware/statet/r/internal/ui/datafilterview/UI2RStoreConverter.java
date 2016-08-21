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

import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider;


public class UI2RStoreConverter implements IConverter {
	
	
	public static final IConverter INSTANCE = new UI2RStoreConverter();
	
	
	private UI2RStoreConverter() {
	}
	
	
	@Override
	public Object getFromType() {
		return Object.class;
	}
	
	@Override
	public Object getToType() {
		return Object.class;
	}
	
	@Override
	public Object convert(final Object fromObject) {
		if (fromObject == AbstractRDataProvider.NA) {
			return null;
		}
		return fromObject;
	}
	
}
