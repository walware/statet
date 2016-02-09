/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de)
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.console;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamMonitor;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.console.NIConsolePartitioner;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences;
import de.walware.statet.nico.ui.NicoUIPreferences;
import de.walware.statet.nico.ui.NicoUITools;


/**
 * A console to interact with controller using command-line-based interface.
 */
public abstract class NIConsole extends TextConsole implements IAdaptable {
	
	
	public static final String NICONSOLE_TYPE = "de.walware.statet.nico.console"; //$NON-NLS-1$
	
	public static final String ADJUST_OUTPUT_WIDTH_COMMAND_ID = "de.walware.statet.nico.commands.AdjustOutputWidth"; //$NON-NLS-1$
	
	
	private static boolean gFontInitialized;
	
	
	private class SettingsListener implements SettingsChangeNotifier.ChangeListener, IPropertyChangeListener {
		
		@Override
		public void settingsChanged(final Set<String> groupIds) {
			if (groupIds.contains(ConsolePreferences.GROUP_ID)) {
				updateSettings();
			}
			if (groupIds.contains(ConsolePreferences.OUTPUT_TEXTSTYLE_GROUP_ID)) {
				final NIConsoleColorAdapter adapter = fAdapter;
				if (adapter != null) {
					adapter.updateSettings();
				}
			}
		}
		
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (getSymbolicFontName().equals(event.getProperty()) ) {
				setFont(null);
			}
		}
		
	}
	
	private final NIConsolePartitioner fPartitioner;
	private final Map<String, NIConsoleOutputStream> fStreams= new HashMap<>();
	private boolean fStreamsClosed;
	
	private final ToolProcess fProcess;
	private NIConsoleColorAdapter fAdapter;
	
	private IDebugEventSetListener fDebugListener;
	private final SettingsListener fSettingsListener = new SettingsListener();
	private int fCurrentWatermark;
	
	
	/**
	 * Constructs a new console.
	 * 
	 * @param name console name
	 */
	public NIConsole(final ToolProcess process, final NIConsoleColorAdapter adapter) {
		super(process.getAttribute(IProcess.ATTR_PROCESS_LABEL),
				NICONSOLE_TYPE,
				NicoUITools.getImageDescriptor(process),
				true);
		fProcess = process;
		fAdapter = adapter;
		
		fPartitioner = new NIConsolePartitioner(this, fAdapter.getStreamIds());
		fPartitioner.connect(getDocument());
		
		if (!gFontInitialized) {
			UIAccess.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					setFont(null);
					gFontInitialized = true;
				}
			});
		}
		else {
			setFont(null);
		}
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(fSettingsListener);
		updateWatermarks();
		
		fStreamsClosed = fProcess.isTerminated();
		fAdapter.connect(process, this);
		
		fDebugListener = new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(final DebugEvent[] events) {
				EVENTS: for (final DebugEvent event : events) {
					if (event.getSource() == fProcess) {
						switch (event.getKind()) {
						case DebugEvent.CHANGE:
							final Object obj = event.getData();
							if (obj != null && obj instanceof String[]) {
								final String[] attrChange = (String[]) obj;
								if (attrChange.length == 3 && IProcess.ATTR_PROCESS_LABEL.equals(attrChange[0])) {
									runSetName(attrChange[2]);
								}
							}
							continue EVENTS;
						case DebugEvent.TERMINATE:
							disconnect();
							continue EVENTS;
						}
					}
				}
			}
			
			private void runSetName(final String name) {
				UIAccess.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						setName(name);
//						ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(NIConsole.this);
						ConsolePlugin.getDefault().getConsoleManager().refresh(NIConsole.this);
					}
				});
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(fDebugListener);
	}
	
	protected void updateSettings() {
		updateWatermarks();
	}
	
	protected void updateWatermarks() {
		final boolean limitBufferSize = true;
		if (limitBufferSize) {
			int lowWater = PreferencesUtil.getInstancePrefs().getPreferenceValue(NicoUIPreferences.OUTPUT_CHARLIMIT_PREF);
			if (lowWater < 100000) {
				lowWater = 100000;
			}
			if (lowWater == fCurrentWatermark) {
				return;
			}
			final int highWater = lowWater + 10000;
			fPartitioner.setWaterMarks(lowWater, highWater);
		}
		else {
			fPartitioner.setWaterMarks(-1, -1);
		}
	}
	
	protected String getSymbolicFontName() {
		return JFaceResources.TEXT_FONT;
	}
	
	@Override
	public void setFont(Font newFont) {
		if (newFont == null) {
			JFaceResources.getFont(getSymbolicFontName()).getFontData()[0].getName();
			newFont = JFaceResources.getFont(getSymbolicFontName());
		}
		super.setFont(newFont);
	}
	
	@Override
	protected void init() {
		super.init();
		
		JFaceResources.getFontRegistry().addListener(fSettingsListener);
	}
	
	@Override
	public void clearConsole() {
		if (fPartitioner != null) {
			fPartitioner.clearBuffer();
		}
	}
	
	
	@Override
	protected void dispose() {
		super.dispose();
		
		final DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.removeDebugEventListener(fDebugListener);
		}
		fDebugListener = null;
		
		final SettingsChangeNotifier changeNotifier = PreferencesUtil.getSettingsChangeNotifier();
		if (changeNotifier != null) {
			changeNotifier.removeChangeListener(fSettingsListener);
		}
		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (fontRegistry != null) {
			fontRegistry.removeListener(fSettingsListener);
		}
		
		disconnect();
	}
	
	@Override
	public abstract IPageBookViewPage createPage(IConsoleView view);
	
	
	@Override
	protected NIConsolePartitioner getPartitioner() {
		return fPartitioner;
	}
	
	
	public void connect(final ToolStreamMonitor streamMonitor, final String streamId,
			final EnumSet<SubmitType> filter) {
		synchronized (fStreams) {
			if (fStreamsClosed) {
				return;
			}
			
			NIConsoleOutputStream stream = fStreams.get(streamId);
			if (stream == null) {
				stream = new NIConsoleOutputStream(this, streamId);
				fStreams.put(streamId, stream);
			}
			
			final NIConsoleOutputStream out = stream;
			streamMonitor.addListener(new IStreamListener() {
				@Override
				public void streamAppended(final String text, final IStreamMonitor monitor) {
					try {
						out.write(text);
					}
					catch (final IOException e) {
						NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when writing to console stream.", e); //$NON-NLS-1$
					}
				}
			}, filter);
		}
	}
	
	public NIConsoleOutputStream getStream(final String streamId) {
		synchronized (fStreams) {
			return fStreams.get(streamId);
		}
	}
	
	private void disconnect() {
		synchronized (fStreams) {
			if (fStreamsClosed) {
				return;
			}
			
			for (final NIConsoleOutputStream stream : fStreams.values()) {
				stream.close();
			}
			fStreamsClosed = true;
			fPartitioner.finish();
			
			fAdapter.disconnect();
			fAdapter = null;
		}
	}
	
	public final ToolProcess getProcess() {
		return fProcess;
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (ITool.class.equals(required)) {
			return fProcess;
		}
		if(ILaunchConfiguration.class.equals(required)) {
			final ILaunch launch = getProcess().getLaunch();
			if(launch != null) {
				return launch.getLaunchConfiguration();
			}
			return null;
		}
		return null;
	}
	
}
