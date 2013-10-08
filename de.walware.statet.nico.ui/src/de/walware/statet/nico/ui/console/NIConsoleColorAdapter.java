/*******************************************************************************
 * Copyright (c) 2006-2013 Stephan Wahlbrink (www.walware.de/goto/opensource)
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
import static de.walware.statet.nico.ui.console.NIConsoleOutputStream.SYSTEM_OUTPUT_STREAM_ID;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.ui.RGBPref;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.FilterPreferences;
import de.walware.statet.nico.ui.NicoUIPreferenceNodes;


/**
 * Connects a console to the streams of a tool process/controller.
 */
public class NIConsoleColorAdapter {
	
	
	private static final List<String> STREAM_IDS= new ConstList<String>(
			INFO_STREAM_ID, INPUT_STREAM_ID, OUTPUT_STREAM_ID, ERROR_STREAM_ID,
			SYSTEM_OUTPUT_STREAM_ID );
	
	
	private NIConsole console;
	
	private final IPreferenceAccess prefAccess;
	
	
	public NIConsoleColorAdapter() {
		this.prefAccess= PreferencesUtil.getInstancePrefs();
	}
	
	
	public List<String> getStreamIds() {
		return STREAM_IDS;
	}
	
	void connect(final ToolProcess process, final NIConsole console) {
		this.console= console;
		
		final FilterPreferences filter= new ConsolePreferences.FilterPreferences(PreferencesUtil.getInstancePrefs());
		final ToolController controller= process.getController();
		if (controller != null) {
			final ToolStreamProxy proxy= controller.getStreams();
			console.connect(proxy.getErrorStreamMonitor(), ERROR_STREAM_ID, 
					filter.showAllErrors() ? EnumSet.allOf(SubmitType.class) : filter.getSelectedTypes());
			console.connect(proxy.getOutputStreamMonitor(), OUTPUT_STREAM_ID, 
					filter.getSelectedTypes());
			console.connect(proxy.getInfoStreamMonitor(), INFO_STREAM_ID, 
					filter.getSelectedTypes());
			console.connect(proxy.getInputStreamMonitor(), INPUT_STREAM_ID, 
					filter.getSelectedTypes());
			console.connect(proxy.getSystemOutputMonitor(), SYSTEM_OUTPUT_STREAM_ID, 
					filter.getSelectedTypes());
			
			updateSettings();
		}
	}
	
	void updateSettings() {
		final NIConsole console= this.console;
		if (console == null) {
			return;
		}
		UIAccess.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				for (final String streamId : STREAM_IDS) {
					final NIConsoleOutputStream stream= console.getStream(streamId);
					if (stream != null) {
						final RGB rgb= getRGB(streamId);
						stream.setColor(SharedUIResources.getColors().getColor(rgb));
						stream.setFontStyle(getFontStyle(streamId));
					}
				}
			}
		});
	}
	
	public void disconnect() {
		this.console= null;
	}
	
	private RGB getRGB(final String streamId) {
		final String rootKey= getPrefRootKey(streamId);
		if (rootKey == null) {
			return null;
		}
		return this.prefAccess.getPreferenceValue(new RGBPref(NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER,
				rootKey + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX ));
	}
	
	private int getFontStyle(final String streamId) {
		final String rootKey= getPrefRootKey(streamId);
		if (rootKey == null) {
			return 0;
		}
		int style= this.prefAccess.getPreferenceValue(new BooleanPref(
				NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER,
				rootKey + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX )) ?
				SWT.BOLD : SWT.NORMAL;
		if (this.prefAccess.getPreferenceValue(new BooleanPref(
				NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER,
				rootKey + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX ))) {
			style |= SWT.ITALIC;
		}
		return style;
	}
	
	private String getPrefRootKey(final String streamId) {
		if (streamId == INPUT_STREAM_ID) {
			return ConsolePreferences.OUTPUT_INPUT_ROOT_KEY;
		}
		else if (streamId == INFO_STREAM_ID) {
			return ConsolePreferences.OUTPUT_INFO_ROOT_KEY;
		}
		else if (streamId == OUTPUT_STREAM_ID) {
			return ConsolePreferences.OUTPUT_STANDARD_OUTPUT_ROOT_KEY;
		}
		else if (streamId == ERROR_STREAM_ID) {
			return ConsolePreferences.OUTPUT_STANDARD_ERROR_ROOT_KEY;
		}
		else if (streamId == SYSTEM_OUTPUT_STREAM_ID) {
			return ConsolePreferences.OUTPUT_SYSTEM_OUTPUT_ROOT_KEY;
		}
		return null;
	}
	
}
