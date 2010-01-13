/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.text;


public class LineInformation implements ILineInformation {
	
	
	private final int[] fOffsets;
	
	
	public LineInformation(final int[] offsets) {
		fOffsets = offsets;
	}
	
	
	public int getLineOfOffset(final int offset) {
		if (fOffsets.length == 0) {
			return -1;
		}
		int low = 0;
		int high = fOffsets.length-1;
		
		while (low <= high) {
			final int mid = (low + high) >> 1;
			final int lineOffset = fOffsets[mid];
			
			if (lineOffset < offset)
				low = mid + 1;
			else if (lineOffset > offset)
				high = mid - 1;
			else
				return mid;
		}
		return low-1;
	}
	
}
