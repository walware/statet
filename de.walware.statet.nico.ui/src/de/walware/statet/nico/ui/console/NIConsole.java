/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.ITool;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamMonitor;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUITools;


/**
 * A console to interact with controller using command-line-based interface.
 */
public abstract class NIConsole extends IOConsole implements IAdaptable {
	
	
	public static final String NICONSOLE_TYPE = "de.walware.statet.nico.console"; //$NON-NLS-1$
	
	
	private final Map<String, IOConsoleOutputStream> fStreams = new HashMap<String, IOConsoleOutputStream>();
	private boolean fStreamsClosed;
	
	private final ToolProcess fProcess;
	private NIConsoleColorAdapter fAdapter;
	
	private IDebugEventSetListener fDebugListener;
	private IPropertyChangeListener fFontListener;
	
	
	/**
	 * Constructs a new console.
	 * 
	 * @param name console name
	 */
	public NIConsole(final ToolProcess process, final NIConsoleColorAdapter adapter) {
		super(process.getAttribute(IProcess.ATTR_PROCESS_LABEL),
				NICONSOLE_TYPE, null, null, true);
		fProcess = process;
		fAdapter = adapter;
		Charset.defaultCharset();
		setImageDescriptor(NicoUITools.getImageDescriptor(fProcess));
		
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
	
	@Override
	protected void init() {
		super.init();
		
		fFontListener = new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				if (JFaceResources.TEXT_FONT.equals(event.getProperty()) )
					setFont(null);
			};
		};
		JFaceResources.getFontRegistry().addListener(fFontListener);
	}
	
	
	@Override
	protected void dispose() {
		super.dispose();
		
		final DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.removeDebugEventListener(fDebugListener);
		}
		fDebugListener = null;
		
		disconnect();
		
		JFaceResources.getFontRegistry().removeListener(fFontListener);
	}
	
	@Override
	public abstract IPageBookViewPage createPage(IConsoleView view);
	
	
	public void connect(final ToolStreamMonitor streamMonitor, final String streamId, final EnumSet<SubmitType> filter) {
		synchronized (fStreams) {
			if (fStreamsClosed) {
				return;
			}
			
			IOConsoleOutputStream stream = fStreams.get(streamId);
			if (stream == null) {
				stream = newOutputStream();
				stream.setColor(fAdapter.getColor(streamId));
				fStreams.put(streamId, stream);
			}
			
			final IOConsoleOutputStream out = stream;
			streamMonitor.addListener(new IStreamListener() {
				
				private static final int BUFFER_SIZE = 4000;
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
	
	public IOConsoleOutputStream getStream(final String streamId) {
		synchronized (fStreams) {
			return fStreams.get(streamId);
		}
	}
	
	private void disconnect() {
		synchronized (fStreams) {
			if (fStreamsClosed) {
				return;
			}
			
			for (final IOConsoleOutputStream stream : fStreams.values()) {
				try {
					if (!stream.isClosed()) {
						stream.close();
					}
				} catch (final IOException e) {
					NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when closing a console stream.", e); //$NON-NLS-1$
				}
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
