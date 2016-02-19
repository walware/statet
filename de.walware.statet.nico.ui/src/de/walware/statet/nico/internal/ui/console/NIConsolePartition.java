/*=============================================================================#
 # Copyright (c) 2004-2016 IBM Corporation and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     IBM Corporation - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.console;

import org.eclipse.jface.text.ITypedRegion;

import de.walware.statet.nico.ui.console.NIConsoleOutputStream;


/**
 * Partition of a NIConsole's document.
 */
public final class NIConsolePartition implements ITypedRegion {
	
	
	private final NIConsoleOutputStream outputStream;
	
	/**
	 * The data contained by this partition.
	 */
	private final String type;
	
	private int offset;
	private int length;
	
	
	/**
	 * Creates a new partition to contain output to console.
	 */
	public NIConsolePartition(final String type, final NIConsoleOutputStream stream) {
		this.type= type;
		this.outputStream= stream;
	}
	
	/**
	 * Creates a new partition to contain output to console.
	 */
	public NIConsolePartition(final String type, final NIConsoleOutputStream stream,
			final int offset, final int length) {
		this(type, stream);
		this.offset= offset;
		this.length= length;
	}
	
	
	@Override
	public String getType() {
		return this.type;
	}
	
	public NIConsoleOutputStream getStream() {
		return this.outputStream;
	}
	
	/**
	 * Sets this partitions offset in the document.
	 * 
	 * @param offset This partitions offset in the document.
	 */
	void setOffset(final int offset) {
		this.offset= offset;
	}
	
	/**
	 * Sets this partition's length.
	 * 
	 * @param length
	 */
	void setLength(final int length) {
		this.length= length;
	}
	
	@Override
	public int getOffset() {
		return this.offset;
	}
	
	@Override
	public int getLength() {
		return this.length;
	}
	
	
	@Override
	public String toString() {
		return getType() + ": offset= " + getOffset() + ", length= " + getLength(); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
