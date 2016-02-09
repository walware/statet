/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de)
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.console;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.core.IPreferenceAccess;
import de.walware.ecommons.preferences.core.Preference.BooleanPref;
import de.walware.ecommons.preferences.ui.RGBPref;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.ui.NicoUIPreferences;


/**
 * Connects a console to the streams of a tool process/controller.
 */
public class NIConsoleColorAdapter {
	
	
	private static final List<String> STREAM_IDS= ImCollections.newList(
			NIConsoleOutputStream.INFO_STREAM_ID,
			NIConsoleOutputStream.STD_INPUT_STREAM_ID,
			NIConsoleOutputStream.STD_OUTPUT_STREAM_ID,
			NIConsoleOutputStream.STD_ERROR_STREAM_ID,
			NIConsoleOutputStream.SYSTEM_OUTPUT_STREAM_ID,
			NIConsoleOutputStream.OTHER_TASKS_INFO_STREAM_ID,
			NIConsoleOutputStream.OTHER_TASKS_STD_INPUT_STREAM_ID,
			NIConsoleOutputStream.OTHER_TASKS_STD_OUTPUT_STREAM_ID,
			NIConsoleOutputStream.OTHER_TASKS_STD_ERROR_STREAM_ID );
	
	
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
		
		final ToolController controller= process.getController();
		if (controller != null) {
			final ToolStreamProxy proxy= controller.getStreams();
			
			final IPreferenceAccess prefs= PreferencesUtil.getInstancePrefs();
			final EnumSet<SubmitType> includeSet= prefs.getPreferenceValue(
					NicoUIPreferences.OUTPUT_FILTER_SUBMITTYPES_INCLUDE_PREF );
			
			final EnumSet<SubmitType> allTypes= EnumSet.allOf(SubmitType.class);
			final EnumSet<SubmitType> otherTypes= EnumSet.of(SubmitType.OTHER);
			final EnumSet<SubmitType> defaultTypes= EnumSet.complementOf(otherTypes);
			
			console.connect(proxy.getInfoStreamMonitor(),
					NIConsoleOutputStream.INFO_STREAM_ID, defaultTypes );
			console.connect(proxy.getInfoStreamMonitor(),
					NIConsoleOutputStream.OTHER_TASKS_INFO_STREAM_ID, otherTypes );
			console.connect(proxy.getInputStreamMonitor(),
					NIConsoleOutputStream.STD_INPUT_STREAM_ID, defaultTypes );
			console.connect(proxy.getOutputStreamMonitor(),
					NIConsoleOutputStream.STD_OUTPUT_STREAM_ID, defaultTypes );
			console.connect(proxy.getErrorStreamMonitor(),
					NIConsoleOutputStream.STD_ERROR_STREAM_ID, defaultTypes );
			if (includeSet.contains(SubmitType.OTHER)) {
				console.connect(proxy.getInputStreamMonitor(),
						NIConsoleOutputStream.OTHER_TASKS_STD_INPUT_STREAM_ID, otherTypes );
				console.connect(proxy.getOutputStreamMonitor(),
						NIConsoleOutputStream.OTHER_TASKS_STD_OUTPUT_STREAM_ID, otherTypes );
				console.connect(proxy.getErrorStreamMonitor(),
						NIConsoleOutputStream.OTHER_TASKS_STD_ERROR_STREAM_ID, otherTypes );
			}
			console.connect(proxy.getSystemOutputMonitor(),
					NIConsoleOutputStream.SYSTEM_OUTPUT_STREAM_ID,
					(includeSet.contains(SubmitType.OTHER)) ? allTypes : defaultTypes );
			
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
						stream.setColor(SharedUIResources.getColors().getColor(
								getFontRGB(streamId) ));
						stream.setBackgroundColor(SharedUIResources.getColors().getColor(
								getBackgroundRGB(streamId) ));
						stream.setFontStyle(getFontStyle(streamId));
					}
				}
			}
		});
	}
	
	public void disconnect() {
		this.console= null;
	}
	
	private RGB getFontRGB(final String streamId) {
		final String rootKey= getPrefRootKey(streamId);
		if (rootKey != null) {
			return this.prefAccess.getPreferenceValue(
					new RGBPref(NicoUIPreferences.OUTPUT_QUALIFIER,
							rootKey + ITextPresentationConstants.TEXTSTYLE_COLOR_SUFFIX ));
		}
		return null;
	}
	
	private RGB getBackgroundRGB(final String streamId) {
		if (streamId.endsWith(NIConsoleOutputStream.OTHER_TASKS_STREAM_SUFFIX)) {
			return this.prefAccess.getPreferenceValue(
					NicoUIPreferences.OUTPUT_OTHER_TASKS_BACKGROUND_COLOR_PREF );
		}
		return null;
	}
	
	private int getFontStyle(final String streamId) {
		final String rootKey= getPrefRootKey(streamId);
		if (rootKey == null) {
			return 0;
		}
		int style= this.prefAccess.getPreferenceValue(new BooleanPref(
				NicoUIPreferences.OUTPUT_QUALIFIER,
				rootKey + ITextPresentationConstants.TEXTSTYLE_BOLD_SUFFIX )) ?
				SWT.BOLD : SWT.NORMAL;
		if (this.prefAccess.getPreferenceValue(new BooleanPref(
				NicoUIPreferences.OUTPUT_QUALIFIER,
				rootKey + ITextPresentationConstants.TEXTSTYLE_ITALIC_SUFFIX ))) {
			style |= SWT.ITALIC;
		}
		return style;
	}
	
	private String getPrefRootKey(final String streamId) {
		if (streamId == NIConsoleOutputStream.INFO_STREAM_ID
				|| streamId == NIConsoleOutputStream.OTHER_TASKS_INFO_STREAM_ID) {
			return NicoUIPreferences.OUTPUT_INFO_STREAM_ROOT_KEY;
		}
		if (streamId == NIConsoleOutputStream.STD_INPUT_STREAM_ID
				|| streamId == NIConsoleOutputStream.OTHER_TASKS_STD_INPUT_STREAM_ID) {
			return NicoUIPreferences.OUTPUT_STD_INPUT_STREAM_ROOT_KEY;
		}
		if (streamId == NIConsoleOutputStream.STD_OUTPUT_STREAM_ID
				|| streamId == NIConsoleOutputStream.OTHER_TASKS_STD_OUTPUT_STREAM_ID) {
			return NicoUIPreferences.OUTPUT_STD_OUTPUT_ROOT_KEY;
		}
		if (streamId == NIConsoleOutputStream.STD_ERROR_STREAM_ID
				|| streamId == NIConsoleOutputStream.OTHER_TASKS_STD_ERROR_STREAM_ID) {
			return NicoUIPreferences.OUTPUT_STD_ERROR_STREAM_ROOT_KEY;
		}
		if (streamId == NIConsoleOutputStream.SYSTEM_OUTPUT_STREAM_ID) {
			return NicoUIPreferences.OUTPUT_SYSTEM_OUTPUT_STREAM_ROOT_KEY;
		}
		return null;
	}
	
}
