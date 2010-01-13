/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Util methods for dialogs
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
	
	/**
	 * Combines existing items and new item of a history setting
	 * and saves it to the dialog settings section
	 * 
	 * @param settings settings section
	 * @param key settings key
	 * @param newItem optional new item
	 */
	public static void saveHistorySettings(final IDialogSettings settings, final String key,
			final String newItem) {
		final String[] items = combineHistoryItems(settings.getArray(key), newItem);
		settings.put(key, items);
	}
	
	/**
	 * Combines existing items and new item of a history setting
	 * 
	 * @param existingItems optional array of existing items
	 * @param newItem optional new item
	 */
	public static String[] combineHistoryItems(final String[] existingItems, final String newItem) {
		final LinkedHashSet<String> history = new LinkedHashSet<String>(HISTORY_MAX);
		if (newItem != null) {
			history.add(newItem);
		}
		if (existingItems != null) {
			for (int i = 0; i < existingItems.length && history.size() < HISTORY_MAX; i++) {
				history.add(existingItems[i]);
			}
		}
		return history.toArray(new String[history.size()]);
	}
	
	
	/**
	 * Recursively enables/disables all controls and their children.
	 * {@link Control#setEnabled(boolean)}
	 * 
	 * See {@link org.eclipse.jface.dialogs.ControlEnableState ControlEnableState}
	 * if saving state is required.
	 * 
	 * @param control a control
	 * @param exceptions
	 * @param enable
	 */
	public static void setEnabled(final Control control, final List exceptions, final boolean enable) {
		setEnabled(new Control[] { control }, exceptions, enable);
	}
	
	/**
	 * Recursively enables/disables all controls and their children.
	 * {@link Control#setEnabled(boolean)}
	 * 
	 * See {@link org.eclipse.jface.dialogs.ControlEnableState ControlEnableState}
	 * if saving state is required.
	 * 
	 * @param control array of controls
	 * @param exceptions
	 * @param enable
	 */
	public static void setEnabled(final Control[] controls, final List exceptions, final boolean enable) {
		for (final Control control : controls) {
			if ((exceptions != null && exceptions.contains(control))) {
				continue;
			}
			control.setEnabled(enable);
			if (control instanceof Composite) {
				final Composite c = (Composite) control;
				final Control[] children = c.getChildren();
				if (children.length > 0) {
					setEnabled(children, exceptions, enable);
				}
			}
		}
	}
	
	/**
	 * Recursively sets visible/invisible to the control and its children.
	 * {@link Control#setVisible(boolean)}
	 * 
	 * @param control a control
	 * @param exceptions
	 * @param enable
	 */
	public static void setVisible(final Control control, final List exceptions, final boolean enable) {
		setVisible(new Control[] { control }, exceptions, enable);
	}
	
	/**
	 * Recursively sets visible/invisible to all controls and their children.
	 * {@link Control#setVisible(boolean)}
	 * 
	 * @param control array of controls
	 * @param exceptions
	 * @param enable
	 */
	public static void setVisible(final Control[] controls, final List exceptions, final boolean enable) {
		for (final Control control : controls) {
			if ((exceptions != null && exceptions.contains(control))) {
				continue;
			}
			control.setVisible(enable);
			if (control instanceof Composite) {
				final Composite c = (Composite) control;
				final Control[] children = c.getChildren();
				if (children.length > 0) {
					setVisible(children, exceptions, enable);
				}
			}
		}
	}
	
	
	private DialogUtil() {}
	
}
