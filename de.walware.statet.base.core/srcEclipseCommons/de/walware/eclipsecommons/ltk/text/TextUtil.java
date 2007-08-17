/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.text;

import org.eclipse.jface.text.IRegion;


/**
 *
 */
public class TextUtil {
	
	/**
	 * Return the length of the overlapping length of two regions.
	 * If they don't overlap, it return the negative distance of the regions.
	 */
	public static final int overlaps(final int reg1Start, final int reg1End, final int reg2Start, final int reg2End) {
		if (reg1Start <= reg2Start) {
			if (reg2End < reg1End) {
				return reg2End-reg2Start;
			}
			return reg1End-reg2Start;
		}
		else {
			if (reg1End < reg2End) {
				return reg1End-reg2Start;
			}
			return reg2End-reg1Start;
		}
	}

	/**
	 * Return the distance of two regions
	 */
	private final int distance(final int reg1Start, final int reg1End, final int reg2Start, final int reg2End) {
		if (reg2Start > reg1End) {
			return reg2Start-reg1End;
		}
		if (reg1Start > reg2End) {
			return reg1Start-reg2End;
		}
		return 0;
	}
	
	/**
	 * Return the distance of a point to the region.
	 */
	public static final int distance(final IRegion region, final int pointOffset) {
		int regPointOffset = region.getOffset();
		if (pointOffset < regPointOffset) {
			return regPointOffset-pointOffset;
		}
		regPointOffset += region.getLength();
		if (pointOffset > regPointOffset) {
			return pointOffset-regPointOffset;
		}
		return 0;
	}

}
