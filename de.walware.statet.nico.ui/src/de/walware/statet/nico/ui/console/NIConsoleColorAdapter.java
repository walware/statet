/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import static de.walware.statet.nico.ui.console.NIConsoleOutputStream.ERROR_STREAM_ID;
import static de.walware.statet.nico.ui.console.NIConsoleOutputStream.INFO_STREAM_ID;
import static de.walware.statet.nico.ui.console.NIConsoleOutputStream.INPUT_STREAM_ID;
import static de.walware.statet.nico.ui.console.NIConsoleOutputStream.OUTPUT_STREAM_ID;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.FilterPreferences;


/**
 * Connects a console to the streams of a tool process/controller.
 */
public class NIConsoleColorAdapter {
	
	
	private static final List<String> STREAM_IDS = new ConstList<String>(
			INFO_STREAM_ID, INPUT_STREAM_ID, OUTPUT_STREAM_ID, ERROR_STREAM_ID );
	
	
	private NIConsole fConsole;
	
	
	public NIConsoleColorAdapter() {
	}
	
	
	public List<String> getStreamIds() {
		return STREAM_IDS;
	}
	
	public void connect(final ToolProcess process, final NIConsole console) {
		fConsole = console;
		
		final FilterPreferences filter = new ConsolePreferences.FilterPreferences(PreferencesUtil.getInstancePrefs());
		final ToolController controller = process.getController();
		if (controller != null) {
			final ToolStreamProxy proxy = controller.getStreams();
			console.connect(proxy.getErrorStreamMonitor(), ERROR_STREAM_ID, 
					filter.showAllErrors() ? EnumSet.allOf(SubmitType.class) : filter.getSelectedTypes());
			console.connect(proxy.getOutputStreamMonitor(), OUTPUT_STREAM_ID, 
					filter.getSelectedTypes());
			console.connect(proxy.getInfoStreamMonitor(), INFO_STREAM_ID, 
					filter.getSelectedTypes());
			console.connect(proxy.getInputStreamMonitor(), INPUT_STREAM_ID, 
					filter.getSelectedTypes());
		}
	}
	
	void updateSettings() {
		final NIConsole console = fConsole;
		if (console == null) {
			return;
		}
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				for (final String streamId : STREAM_IDS) {
					final NIConsoleOutputStream stream = console.getStream(streamId);
					if (stream != null) {
						stream.setColor(getColor(streamId));
					}
				}
			}
		});
	}
	
	public void disconnect() {
		fConsole = null;
	}
	
	
	public Color getColor(final String streamIdentifer) {
		final Preference<RGB> colorPref = getColorPref(streamIdentifer);
		if (colorPref == null) {
			return null;
		}
		
		final RGB rgb = PreferencesUtil.getInstancePrefs().getPreferenceValue(colorPref);
		return UIAccess.getColor(SharedUIResources.getColors(), rgb);
	}
	
	protected Preference<RGB> getColorPref(final String streamId) {
		if (streamId.equals(INPUT_STREAM_ID)) {
			return ConsolePreferences.PREF_COLOR_INPUT;
		}
		else if (streamId.equals(NIConsoleOutputStream.INFO_STREAM_ID)) {
			return ConsolePreferences.PREF_COLOR_INFO;
		}
		else if (streamId.equals(OUTPUT_STREAM_ID)) {
			return ConsolePreferences.PREF_COLOR_OUTPUT;
		}
		else if (streamId.equals(ERROR_STREAM_ID)) {
			return ConsolePreferences.PREF_COLOR_ERROR;
		}
		return null;
	}
	
}
