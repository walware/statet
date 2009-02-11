/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.EnumSet;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.console.IOConsoleOutputStream;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.StatetUIServices;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences.FilterPreferences;
import de.walware.statet.nico.ui.NicoUI;


/**
 * Connects a console to the streams of a tool process/controller.
 */
public class NIConsoleColorAdapter implements IConsoleColorProvider {
	
	
	public static final String ID_INFO_STREAM = NicoUI.PLUGIN_ID+".InfoStream"; //$NON-NLS-1$
	
	private NIConsole fConsole;
	
	protected IPreferenceStore fPreferences;
	private IPropertyChangeListener fPreferenceListener;
	
	
	public NIConsoleColorAdapter() {
	}
	
	
	public void connect(final IProcess process, final IConsole console) {
		// not yet implemented
	}
	
	public void connect(final ToolProcess process, final NIConsole console) {
		fConsole = console;
		
		fPreferences = createPreferenceStore();
		fPreferenceListener = new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				final String streamId = getStreamId(event.getProperty());
				if (streamId != null) {
					UIAccess.getDisplay().asyncExec(new Runnable() {
						public void run() {
							final IOConsoleOutputStream stream = fConsole.getStream(streamId);
							if (stream != null) {
								stream.setColor(getColor(streamId));
							}
						}
					});
				}
			}
		};
		fPreferences.addPropertyChangeListener(fPreferenceListener);
		
		final FilterPreferences filter = new ConsolePreferences.FilterPreferences(PreferencesUtil.getInstancePrefs());
		final ToolController controller = process.getController();
		if (controller != null) {
			final ToolStreamProxy proxy = controller.getStreams();
			console.connect(proxy.getErrorStreamMonitor(), IDebugUIConstants.ID_STANDARD_ERROR_STREAM, 
					filter.showAllErrors() ? EnumSet.allOf(SubmitType.class) : filter.getSelectedTypes());
			console.connect(proxy.getOutputStreamMonitor(), IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM, 
					filter.getSelectedTypes());
			console.connect(proxy.getInfoStreamMonitor(), ID_INFO_STREAM, 
					filter.getSelectedTypes());
			console.connect(proxy.getInputStreamMonitor(), IDebugUIConstants.ID_STANDARD_INPUT_STREAM, 
					filter.getSelectedTypes());
		}
	}
	
	public void disconnect() {
		fPreferences.removePropertyChangeListener(fPreferenceListener);
		fPreferenceListener = null;
		fPreferences = null;
		
		fConsole = null;
	}
	
	
	public Color getColor(final String streamIdentifer) {
		final String colorId = getColorId(streamIdentifer);
		if (colorId == null) {
			return null;
		}
		final RGB rgb = PreferenceConverter.getColor(fPreferences, colorId);
		return UIAccess.getColor(StatetUIServices.getSharedColorManager(), rgb);
	}
	
	protected String getColorId(final String streamId) {
		if (streamId.equals(IDebugUIConstants.ID_STANDARD_INPUT_STREAM)) {
			return ConsolePreferences.INPUT_COLOR;
		}
		else if (streamId.equals(ID_INFO_STREAM)) {
			return ConsolePreferences.INFO_COLOR;
		}
		else if (streamId.equals(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM)) {
			return ConsolePreferences.OUTPUT_COLOR;
		}
		else if (streamId.equals(IDebugUIConstants.ID_STANDARD_ERROR_STREAM)) {
			return ConsolePreferences.ERROR_COLOR;
		}
		return null;
	}
	
	protected String getStreamId(final String colorId) {
		if (colorId.equals(ConsolePreferences.INPUT_COLOR)) {
			return IDebugUIConstants.ID_STANDARD_INPUT_STREAM;
		}
		else if (colorId.equals(ConsolePreferences.INFO_COLOR)) {
			return ID_INFO_STREAM;
		}
		else if (colorId.equals(ConsolePreferences.OUTPUT_COLOR)) {
			return IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM;
		}
		else if (colorId.equals(ConsolePreferences.ERROR_COLOR)) {
			return IDebugUIConstants.ID_STANDARD_ERROR_STREAM;
		}
		return null;
	}
	
	/**
	 * For ProcessConsole only.
	 */
	public boolean isReadOnly() {
		return true; 
	}
	
	protected IPreferenceStore createPreferenceStore() {
		return ConsolePreferences.getStore();
	}
	
}
