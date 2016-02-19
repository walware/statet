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


public class Text2NumConverter implements IConverter {
	
	
	public Text2NumConverter() {
	}
	
	
	@Override
	public Object getFromType() {
		return String.class;
	}
	
	@Override
	public Object getToType() {
		return Double.TYPE;
	}
	
	@Override
	public Object convert(final Object fromObject) {
		String s = (String) fromObject;
		if (s == null || (s = s.trim()).length() == 0) {
			throw new IllegalArgumentException();
		}
		if (s.charAt(s.length()-1) == 'f') {
			if (s.endsWith("Inf")) { //$NON-NLS-1$
				s = s.substring(0, s.length()-3) + "Infinity"; //$NON-NLS-1$
			}
			else {
				s = s.substring(0, s.length()-1);
			}
		}
		return Double.parseDouble(s);
	}
	
}
