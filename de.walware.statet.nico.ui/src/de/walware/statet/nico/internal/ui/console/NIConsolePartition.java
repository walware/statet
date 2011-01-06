/*******************************************************************************
 * Copyright (c) 2004-2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.console;

import org.eclipse.jface.text.ITypedRegion;

import de.walware.statet.nico.ui.console.NIConsoleOutputStream;


/**
 * Partition of a NIConsole's document.
 */
public class NIConsolePartition implements ITypedRegion {
	
	
	final NIConsoleOutputStream fOutputStream;
	
	/**
	 * The data contained by this partition.
	 */
	private final String fType;
	
	private int fOffset;
	private int fLength;
	
	
	/**
	 * Creates a new partition to contain output to console.
	 */
	public NIConsolePartition(final String type, final int length, final NIConsoleOutputStream stream) {
		fType = type;
		fLength = length;
		fOutputStream = stream;
	}
	
	public String getType() {
		return fType;
	}
	
	public int getOffset() {
		return fOffset;
	}
	
	public int getLength() {
		return fLength;
	}
	
	/**
	 * Sets this partitions offset in the document.
	 * 
	 * @param offset This partitions offset in the document.
	 */
	void setOffset(final int offset) {
		fOffset = offset;
	}
	
	/**
	 * Sets this partition's length.
	 * 
	 * @param length
	 */
	void setLength(final int length) {
		fLength = length;
	}
	
}
