/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamsProxy;


/**
 * None buffered streams
 */
public class ToolStreamProxy implements IStreamsProxy {
	
	
	private final ToolStreamMonitor inputMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor infoMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor standardOutputMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor standardErrorMonitor = new ToolStreamMonitor();
	private final ToolStreamMonitor systemOutputMonitor = new ToolStreamMonitor();
	
	
	public ToolStreamProxy() {
	}
	
	
	@Override
	public void write(final String input) throws IOException {
		throw new IOException("Function is not supported."); //$NON-NLS-1$
	}
	
	public ToolStreamMonitor getInputStreamMonitor() {
		return this.inputMonitor;
	}
	
	public ToolStreamMonitor getInfoStreamMonitor() {
		return this.infoMonitor;
	}
	
	@Override
	public ToolStreamMonitor getOutputStreamMonitor() {
		return this.standardOutputMonitor;
	}
	
	@Override
	public ToolStreamMonitor getErrorStreamMonitor() {
		return this.standardErrorMonitor;
	}
	
	public ToolStreamMonitor getSystemOutputMonitor() {
		return this.systemOutputMonitor;
	}
	
	
	public void dispose() {
		inputMonitor.dispose();
		infoMonitor.dispose();
		standardOutputMonitor.dispose();
		standardErrorMonitor.dispose();
		systemOutputMonitor.dispose();
	}
	
}
