/*******************************************************************************
 * Copyright (c) 2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.databinding;

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;


/**
 * Converter for color preferences.
 */
public class ConvertString2RGB extends Converter {
	
	public ConvertString2RGB() {
		
		super(String.class, RGB.class);
	}
	
	public Object convert(Object fromObject) {
		
		return StringConverter.asRGB((String) fromObject);
	}
	
}
