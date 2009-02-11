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

package de.walware.ecommons.ui.util;

import java.util.LinkedHashSet;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * 
 */
public class DialogUtil {
	
	
	private static final int HISTORY_MAX = 25;
	
	
	public static IDialogSettings getDialogSettings(final AbstractUIPlugin plugin, final String dialogId) {
		final String sectionName = dialogId;
		final IDialogSettings settings = plugin.getDialogSettings();
		IDialogSettings section = settings.getSection(sectionName);
		if (section == null) {
			section = settings.addNewSection(sectionName);
		}
		return section;
	}
	
	public static void saveHistorySettings(final IDialogSettings settings, final String key,
			final String newValue) {
		final LinkedHashSet<String> history = new LinkedHashSet<String>(HISTORY_MAX);
		history.add(newValue);
		final String[] oldHistory = settings.getArray(key);
		if (oldHistory != null) {
			for (int i = 0; i < oldHistory.length && history.size() < HISTORY_MAX; i++) {
				history.add(oldHistory[i]);
			}
		}
		settings.put(key, history.toArray(new String[history.size()]));
	}
	
	
	private DialogUtil() {}
	
}
