/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.preferences;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

import de.walware.eclipsecommons.preferences.Preference;


/**
 * Preference of a color value as RGB (like in JFace).
 */
public class RGBPref extends Preference<RGB> {
	
	public RGBPref(String qualifier, String key) {
		
		super(qualifier, key, Type.STRING);
	}

	@Override
	public boolean isUsageType(Object obj) {
		
		return (obj instanceof RGB);
	}
	
	@Override
	public RGB store2Usage(Object obj) {
		
		if (obj != null) {
			return StringConverter.asRGB((String) obj);
		}
		return null;
	}

	@Override
	public Object usage2Store(RGB obj) {
		
		if (obj != null && obj instanceof RGB) {
			return StringConverter.asString(obj);
		}
		return null;
	}
}
