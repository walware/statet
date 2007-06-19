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

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.text.IRegion;

import de.walware.statet.ext.ui.text.BasicHeuristicTokenScanner;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.ui.IRDocumentPartitions;


/**
 *
 */
public class RHeuristicTokenScanner extends BasicHeuristicTokenScanner {

	
	/**
	 * 
	 */
	public RHeuristicTokenScanner() {
		
		super(IRDocumentPartitions.R_DOCUMENT_PARTITIONING);
	}
	
	
	public IRegion findRWord(int position, final boolean isDotSeparator, boolean allowEnd) {
		
		return findRegion(position, new StopCondition() {
			@Override
			public boolean stop(boolean forward) {
				return (RTokens.isRobustSeparator(fChar, isDotSeparator));
			}
		}, true);
	}
	
}
