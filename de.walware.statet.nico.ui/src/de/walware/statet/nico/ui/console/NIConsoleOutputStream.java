/*******************************************************************************
 * Copyright (c) 2000-2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

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
	
	
	public static final String INFO_STREAM_ID = NicoUI.PLUGIN_ID+".InfoStream"; //$NON-NLS-1$
	public static final String INPUT_STREAM_ID = IDebugUIConstants.ID_STANDARD_INPUT_STREAM;
	public static final String OUTPUT_STREAM_ID = IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM;
	public static final String ERROR_STREAM_ID = IDebugUIConstants.ID_STANDARD_ERROR_STREAM;
	
	
	/**
	 * The console this stream is attached to.
	 */
	private final NIConsole fConsole;
	
	/**
	 * The console's document partitioner.
	 */
	private NIConsolePartitioner fPartitioner;
	
	private final String fId;
	
	/**
	 * Flag indicating whether this stream has been closed.
	 */
	private boolean fClosed = false;
	
	/**
	 * The color used to decorate data written to this stream.
	 */
	private Color fColor;
	
	/**
	 * The font style used to decorate data written to this stream.
	 */
	private int fFontStyle;
	
	private boolean fPrependCR;
	
	
	/**
	 * Constructs a new output stream on the given console.
	 * 
	 * @param console the console
	 * @param streamId 
	 */
	NIConsoleOutputStream(final NIConsole console, final String streamId) {
		fConsole = console;
		fPartitioner = console.getPartitioner();
		fId = streamId;
	}
	
	
	public String getId() {
		return fId;
	}
	
	/**
	 * Returns the font style used to decorate data written to this stream.
	 * 
	 * @return the font style used to decorate data written to this stream
	 */
	public int getFontStyle() {
		return fFontStyle;
	}
	
	/**
	 * Sets the font style to be used to decorate data written to this stream.
	 * 
	 * @param newFontStyle the font style to be used to decorate data written to this stream
	 */
	public void setFontStyle(final int newFontStyle) {
		if (newFontStyle != fFontStyle) {
			final int old = fFontStyle;
			fFontStyle = newFontStyle;
			fConsole.firePropertyChange(this, IConsoleConstants.P_FONT_STYLE, new Integer(old), new Integer(fFontStyle));
		}
	}
	
	/**
	 * Sets the color of this stream. Use <code>null</code> to indicate
	 * the default color.
	 * 
	 * @param newColor color of this stream, or <code>null</code>
	 */
	public void setColor(final Color newColor) {
		final Color old = fColor;
		if (old == null || !old.equals(newColor)) {
			fColor = newColor;
			fConsole.firePropertyChange(this, IConsoleConstants.P_STREAM_COLOR, old, newColor);
		}
	}
	
	/**
	 * Returns the color of this stream, or <code>null</code>
	 * if default.
	 * 
	 * @return the color of this stream, or <code>null</code>
	 */
	public Color getColor() {
		return fColor;
	}
	
	/**
	 * Returns true if the stream has been closed
	 * 
	 * @return true is the stream has been closed, false otherwise.
	 */
	public synchronized boolean isClosed() {
		return fClosed;
	}
	
//	public synchronized void close() throws IOException {
//		if(fClosed) {
//			throw new IOException("Output Stream is closed"); //$NON-NLS-1$
//		}
//		if (fPrependCR) { // force writing of last /r
//			fPrependCR = false;
//			notifyParitioner("\r"); //$NON-NLS-1$
//		}
//		fConsole.streamClosed(this);
//		fClosed = true;
//		fPartitioner = null;
//	}
	
	synchronized void close() {
		fClosed = true;
		if (fPrependCR) { // force writing of last /r
			fPrependCR = false;
			try {
				notifyParitioner("\r"); //$NON-NLS-1$
			}
			catch (final IOException e) {}
		}
		fPartitioner = null;
	}
	
	/**
	 * Writes a string to the attached console.
	 * 
	 * @param text the string to write to the attached console.
	 * @throws IOException if the stream is closed.
	 */
	public synchronized void write(String text) throws IOException {
		if(fClosed) {
			throw new IOException("Output Stream is closed"); //$NON-NLS-1$
		}
		if (fPrependCR){
			text = "\r"+text; //$NON-NLS-1$
			fPrependCR = false;
		}
		if (text.endsWith("\r")) { //$NON-NLS-1$
			fPrependCR = true;
			text = text.substring(0, text.length()-1);
		}
		notifyParitioner(text);
	}
	
	private void notifyParitioner(final String encodedString) throws IOException {
		fPartitioner.streamAppended(this, encodedString);
		
		ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(fConsole);
	}
	
}
