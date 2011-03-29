/*******************************************************************************
 * Copyright (c) 2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;


/**
 * Runtime process supporting logging of the process output without attaching a console
 */
public class LogRuntimeProcess extends RuntimeProcess {
	
	
	private boolean fLogOnly;
	
	private RuntimeProcessOutput fLog;
	
	
	public LogRuntimeProcess(final ILaunch launch, final Process process, final String name,
			final Map attributes) {
		super(launch, process, name, attributes);
	}
	
	
	@Override
	protected IStreamsProxy createStreamsProxy() {
		fLogOnly = ToolRunner.captureLogOnly(getLaunch().getLaunchConfiguration());
		final IStreamsProxy proxy = super.createStreamsProxy();
		if (fLogOnly) {
			fLog = new RuntimeProcessOutput(proxy, fLogOnly);
		}
		return proxy;
	}
	
	@Override
	public IStreamsProxy getStreamsProxy() {
		if (!fLogOnly) {
			return super.getStreamsProxy();
		}
		return null;
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (ILogOutput.class.equals(required)) {
			return fLog;
		}
		return super.getAdapter(required);
	}
	
}


class RuntimeProcessOutput implements ILogOutput {
	
	
	private final class StreamListener implements IStreamListener {
		
		private final IStreamMonitor fStreamMonitor;
		
		private boolean fFlushed;
		
		private StreamListener(final IStreamMonitor streamMonitor) {
			fStreamMonitor = streamMonitor;
			fStreamMonitor.addListener(this);
		}
		
		public void streamAppended(final String text, final IStreamMonitor monitor) {
			if (!fFlushed) {
				String contents = null;
				synchronized (fStreamMonitor) {
					fFlushed = true;
					contents = fStreamMonitor.getContents();
					if (fFlush && fStreamMonitor instanceof IFlushableStreamMonitor) {
						final IFlushableStreamMonitor m = (IFlushableStreamMonitor) fStreamMonitor;
						m.flushContents();
						m.setBuffered(false);
					}
				}
				append(contents);
			}
			else {
				append(text);
			}
		}
		
		public void init() {
			streamAppended(null, fStreamMonitor);
		}
		
		public void dispose() {
			fStreamMonitor.removeListener(this);
		}
		
	}
	
	
	private final StringBuilder fStringBuilder = new StringBuilder(0x1000);
	
	private StreamListener fOutputStreamListener;
	private StreamListener fErrorStreamListener;
	
	private final boolean fFlush;
	private final int fHighWater;
	private final int fLowWater;
	
	
	public RuntimeProcessOutput(final IStreamsProxy proxy, final boolean flush) {
		fFlush = flush;
		fHighWater = 0x10000;
		fLowWater = fHighWater - 0x1000;
		
		fOutputStreamListener = new StreamListener(proxy.getOutputStreamMonitor());
		fErrorStreamListener = new StreamListener(proxy.getErrorStreamMonitor());
		fOutputStreamListener.init();
		fErrorStreamListener.init();
	}
	
	
	private synchronized void append(final String text) {
		if (text == null) {
			return;
		}
		if (fStringBuilder.length() + text.length() > fHighWater) {
			int idx = fStringBuilder.length() + text.length() - fLowWater;
			boolean found = false;
			while (idx < fStringBuilder.length()) {
				final char c = fStringBuilder.charAt(idx++);
				if (c == '\n' || c == '\r') {
					found = true;
				}
				else if (found) {
					idx--;
					break;
				}
			}
			fStringBuilder.delete(0, idx);
		}
		fStringBuilder.append(text);
	}
	
	public synchronized void dispose() {
		if (fOutputStreamListener != null) {
			fOutputStreamListener.dispose();
			fOutputStreamListener = null;
		}
		if (fErrorStreamListener != null) {
			fErrorStreamListener.dispose();
			fErrorStreamListener = null;
		}
	}
	
	public synchronized String getOutput() {
		return fStringBuilder.toString();
	}
	
}
