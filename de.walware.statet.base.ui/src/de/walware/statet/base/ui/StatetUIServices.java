/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui;

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * 
 */
public class StatetUIServices {
	
	
	public static IPreferenceStore getBaseUIPreferenceStore() {
		return StatetUIPlugin.getDefault().getPreferenceStore();
	}
	
}
