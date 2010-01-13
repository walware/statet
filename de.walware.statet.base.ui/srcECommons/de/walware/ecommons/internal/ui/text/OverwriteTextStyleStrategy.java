/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.internal.ui.text;

import java.util.Set;

import org.eclipse.jface.text.source.AnnotationPainter.ITextStyleStrategy;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.IDisposable;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.base.ui.sourceeditors.ContentAssistPreference;


public class OverwriteTextStyleStrategy implements ITextStyleStrategy, IDisposable, SettingsChangeNotifier.ChangeListener {
	
	
	private Color fColor;
	
	
	public OverwriteTextStyleStrategy() {
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(this);
	}
	
	
	public void applyTextStyle(final StyleRange styleRange, final Color annotationColor) {
		Color color = fColor;
		if (fColor == null) {
			fColor = color = initColor(annotationColor);
		}
		styleRange.strikeout = true;
		styleRange.strikeoutColor = color;
		styleRange.foreground = color;
	}
	
	
	public Color initColor(final Color fallback) {
		final RGB rgb = PreferencesUtil.getInstancePrefs().getPreferenceValue(ContentAssistPreference.REPLACEMENT_FOREGROUND);
		if (rgb != null) {
			return StatetUIServices.getSharedColorManager().getColor(rgb);
		}
		return fallback;
	}
	
	public void settingsChanged(final Set<String> groupIds) {
		if (groupIds.contains(ContentAssistPreference.GROUP_ID)) {
			fColor = null;
		}
	}
	
	public void dispose() {
		PreferencesUtil.getSettingsChangeNotifier().removeChangeListener(this);
	}
	
}
