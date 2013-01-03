/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamsProxy;


/**
 * None buffered streams
 */
public class ToolStreamProxy implements IStreamsProxy {
	
	
	private final ToolStreamMonitor fInputMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor fInfoMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor fOutputMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor fErrorMonitor = new ToolStreamMonitor();
	
	
	public ToolStreamProxy() {
	}
	
	
	@Override
	public void write(final String input) throws IOException {
		throw new IOException("Function is not supported."); //$NON-NLS-1$
	}
	
	public ToolStreamMonitor getInputStreamMonitor() {
		return fInputMonitor;
	}
	
	public ToolStreamMonitor getInfoStreamMonitor() {
		return fInfoMonitor;
	}
	
	@Override
	public ToolStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}
	
	@Override
	public ToolStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}
	
	
	/**
	 * 
	 */
	public void dispose() {
		fInputMonitor.dispose();
		fInfoMonitor.dispose();
		fOutputMonitor.dispose();
		fErrorMonitor.dispose();
	}
	
}
