/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.SettingsChangeNotifier;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.ITool;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamMonitor;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.console.NIConsolePartitioner;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences;
import de.walware.statet.nico.ui.NicoUITools;


/**
 * A console to interact with controller using command-line-based interface.
 */
public abstract class NIConsole extends TextConsole implements IAdaptable {
	
	
	public static final String NICONSOLE_TYPE = "de.walware.statet.nico.console"; //$NON-NLS-1$
	
	
	private class SettingsListener implements SettingsChangeNotifier.ChangeListener, IPropertyChangeListener {
		
		public void settingsChanged(final Set<String> groupIds) {
			if (groupIds.contains(ConsolePreferences.GROUP_ID)) {
				updateSettings();
			}
		}
		
		public void propertyChange(final PropertyChangeEvent event) {
			if (JFaceResources.TEXT_FONT.equals(event.getProperty()) ) {
				setFont(null);
			}
		}
		
	}
	
	private final NIConsolePartitioner fPartitioner;
	private final Map<String, NIConsoleOutputStream> fStreams = new HashMap<String, NIConsoleOutputStream>();
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
		
		PreferencesUtil.getSettingsChangeNotifier().addChangeListener(fSettingsListener);
		updateWatermarks();
		
		fStreamsClosed = fProcess.isTerminated();
		fAdapter.connect(process, this);
		
		fDebugListener = new IDebugEventSetListener() {
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
		final NIConsoleColorAdapter adapter = fAdapter;
		if (adapter != null) {
			adapter.updateSettings();
		}
	}
	
	protected void updateWatermarks() {
		final boolean limitBufferSize = true;
		if (limitBufferSize) {
			int lowWater = PreferencesUtil.getInstancePrefs().getPreferenceValue(ConsolePreferences.PREF_CHARLIMIT);
			if (lowWater < 10000) {
				lowWater = 10000;
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
	
	
	public void connect(final ToolStreamMonitor streamMonitor, final String streamId, final EnumSet<SubmitType> filter) {
		synchronized (fStreams) {
			if (fStreamsClosed) {
				return;
			}
			
			NIConsoleOutputStream stream = fStreams.get(streamId);
			if (stream == null) {
				stream = new NIConsoleOutputStream(this, streamId);
				stream.setColor(fAdapter.getColor(streamId));
				fStreams.put(streamId, stream);
			}
			
			final NIConsoleOutputStream out = stream;
			streamMonitor.addListener(new IStreamListener() {
				
				private static final int BUFFER_SIZE = 9216;
				private final StringBuilder fBuffer = new StringBuilder(BUFFER_SIZE);
				
				public void streamAppended(final String text, final IStreamMonitor monitor) {
					try {
						synchronized (out) {
							// it would be better to run the check later, e.g. in partitioning job, but this is internal Eclipse
							int start = 0;
							final int n = text.length();
							for (int idx = 0; idx < n;) {
								final char c = text.charAt(idx);
								if (c <= 12) {
									switch (c) {
									case 7: // bell
										fBuffer.append(text, start, idx);
										ring();
										start = ++idx;
										continue;
									case 8: // back
										fBuffer.append(text, start, idx);
										if (fBuffer.length() > 0) {
											final char prev = fBuffer.charAt(fBuffer.length()-1);
											if (prev != '\n' && prev != '\r') {
												fBuffer.deleteCharAt(fBuffer.length()-1);
											}
										}
										start = ++idx;
										continue;
									case 11: // vertical tab
										fBuffer.append(text, start, idx);
										printVTab();
										start = ++idx;
										continue;
									case 12: // formfeed
										fBuffer.append(text, start, idx);
										printFormfeed();
										start = ++idx;
										continue;
									}
								}
								++idx;
								continue;
							}
							if (start == 0) {
								out.write(text);
							}
							else {
								fBuffer.append(text, start, n);
								out.write(fBuffer.toString());
								if (fBuffer.capacity() > BUFFER_SIZE*5) {
									fBuffer.setLength(BUFFER_SIZE);
									fBuffer.trimToSize();
								}
								fBuffer.setLength(0);
							}
// TODO
//							if (text.length() >= 7168) {
//								try {
//									Thread.sleep(10);
//								}
//								catch (final InterruptedException e) {
//									Thread.interrupted();
//								}
//							}
						}
					}
					catch (final IOException e) {
						NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when writing to console stream.", e); //$NON-NLS-1$
					}
				}
				
				private void ring() {
					final Display display = UIAccess.getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							display.beep();
						};
					});
				}
				
				private void printVTab() {
					final String br = fProcess.getWorkspaceData().getLineSeparator();
					fBuffer.append(br);
				}
				
				private void printFormfeed() {
					final String br = fProcess.getWorkspaceData().getLineSeparator();
					fBuffer.append(br+br);
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
			
			fAdapter.disconnect();
			fAdapter = null;
		}
	}
	
	public final ToolProcess getProcess() {
		return fProcess;
	}
	
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
