/*=============================================================================#
 # Copyright (c) 2000-2016 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 *
 # Contributors:
 #     IBM Corporation - initial API and implementation
 #     Stephan Wahlbrink - extended for StatET
 #=============================================================================*/

package de.walware.statet.nico.ui.console;

import java.io.IOException;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;

import de.walware.statet.nico.internal.ui.console.NIConsolePartitioner;
import de.walware.statet.nico.ui.NicoUI;


/**
 * OutputStream used to write to an NIConsole.
 * <p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class NIConsoleOutputStream {
	
	
	public static final String INFO_STREAM_ID= NicoUI.PLUGIN_ID+".InfoStream"; //$NON-NLS-1$
	public static final String INPUT_STREAM_ID= IDebugUIConstants.ID_STANDARD_INPUT_STREAM;
	public static final String OUTPUT_STREAM_ID= IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM;
	public static final String ERROR_STREAM_ID= IDebugUIConstants.ID_STANDARD_ERROR_STREAM;
	public static final String SYSTEM_OUTPUT_STREAM_ID= NicoUI.PLUGIN_ID+".SystemOutputStream"; //$NON-NLS-1$
	
	
	/**
	 * The console this stream is attached to.
	 */
	private final NIConsole console;
	
	/**
	 * The console's document partitioner.
	 */
	private NIConsolePartitioner partitioner;
	
	/**
	 * The id of the stream
	 */
	private final String id;
	
	/**
	 * Flag indicating whether this stream has been closed.
	 */
	private boolean closed= false;
	
	/**
	 * The color used to decorate data written to this stream.
	 */
	private Color color;
	
	/**
	 * The font style used to decorate data written to this stream.
	 */
	private int fontStyle;
	
	
	private boolean prependCR;
	
	
	/**
	 * Constructs a new output stream on the given console.
	 * 
	 * @param console the console
	 * @param streamId 
	 */
	NIConsoleOutputStream(final NIConsole console, final String streamId) {
		this.console= console;
		this.partitioner= console.getPartitioner();
		this.id= streamId;
	}
	
	
	public String getId() {
		return this.id;
	}
	
	/**
	 * Returns the font style used to decorate data written to this stream.
	 * 
	 * @return the font style used to decorate data written to this stream
	 */
	public int getFontStyle() {
		return this.fontStyle;
	}
	
	/**
	 * Sets the font style to be used to decorate data written to this stream.
	 * 
	 * @param newFontStyle the font style to be used to decorate data written to this stream
	 */
	public void setFontStyle(final int newFontStyle) {
		if (newFontStyle != this.fontStyle) {
			final int old= this.fontStyle;
			this.fontStyle= newFontStyle;
			this.console.firePropertyChange(this, IConsoleConstants.P_FONT_STYLE, new Integer(old), new Integer(this.fontStyle));
		}
	}
	
	/**
	 * Sets the color of this stream. Use <code>null</code> to indicate
	 * the default color.
	 * 
	 * @param newColor color of this stream, or <code>null</code>
	 */
	public void setColor(final Color newColor) {
		final Color old= this.color;
		if (old == null || !old.equals(newColor)) {
			this.color= newColor;
			this.console.firePropertyChange(this, IConsoleConstants.P_STREAM_COLOR, old, newColor);
		}
	}
	
	/**
	 * Returns the color of this stream, or <code>null</code>
	 * if default.
	 * 
	 * @return the color of this stream, or <code>null</code>
	 */
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * Returns true if the stream has been closed
	 * 
	 * @return true is the stream has been closed, false otherwise.
	 */
	public synchronized boolean isClosed() {
		return this.closed;
	}
	
	synchronized void close() {
		if (this.prependCR) { // force writing of last /r
			this.prependCR= false;
			try {
				notifyParitioner("\r"); //$NON-NLS-1$
			}
			catch (final IOException e) {}
		}
		this.closed= true;
		this.partitioner= null;
	}
	
	/**
	 * Writes a string to the attached console.
	 * 
	 * @param text the string to write to the attached console.
	 * @throws IOException if the stream is closed.
	 */
	public synchronized void write(String text) throws IOException {
		if(this.closed) {
			throw new IOException("Output Stream is closed"); //$NON-NLS-1$
		}
		if (this.prependCR){
			text= "\r" + text; //$NON-NLS-1$
			this.prependCR= false;
		}
		if (text.endsWith("\r")) { //$NON-NLS-1$
			this.prependCR= true;
			text= text.substring(0, text.length() - 1);
		}
		notifyParitioner(text);
	}
	
	private void notifyParitioner(final String encodedString) throws IOException {
		this.partitioner.streamAppended(this, encodedString);
		
		ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(this.console);
	}
	
}
