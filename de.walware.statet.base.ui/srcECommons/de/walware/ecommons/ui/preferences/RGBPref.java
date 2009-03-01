/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.preferences.Preference;


/**
 * Preference of a color value as RGB (like in JFace).
 */
public class RGBPref extends Preference<RGB> {
	
	
	public RGBPref(final String qualifier, final String key) {
		super(qualifier, key, Type.STRING);
	}
	
	
	@Override
	public Class<RGB> getUsageType() {
		return RGB.class;
	}
	
	@Override
	public RGB store2Usage(final Object obj) {
		if (obj != null) {
			return StringConverter.asRGB((String) obj);
		}
		return null;
	}
	
	@Override
	public Object usage2Store(final RGB value) {
		if (value != null) {
			return StringConverter.asString(value);
		}
		return null;
	}
	
}
