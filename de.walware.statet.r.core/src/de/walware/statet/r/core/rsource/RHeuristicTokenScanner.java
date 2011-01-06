/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.text.BasicHeuristicTokenScanner;
import de.walware.ecommons.text.PartitioningConfiguration;

import de.walware.statet.r.core.rlang.RTokens;


/**
 * 
 */
public class RHeuristicTokenScanner extends BasicHeuristicTokenScanner {
	
	
	/**
	 * 
	 */
	public RHeuristicTokenScanner() {
		this(IRDocumentPartitions.R_PARTITIONING_CONFIG);
	}
	
	protected RHeuristicTokenScanner(final PartitioningConfiguration partitioning) {
		super(partitioning);
	}
	
	
	public IRegion findRWord(final int position, final boolean isDotSeparator, final boolean allowEnd) {
		return findRegion(position, new StopCondition() {
			@Override
			public boolean stop() {
				return (RTokens.isRobustSeparator(fChar, isDotSeparator));
			}
		}, true);
	}
	
	
	private class BracketBalanceCondition extends PartitionBasedCondition {
		
		private int type;
		private boolean open;
		
		@Override
		protected boolean matchesChar() {
			switch (fChar) {
			case '{':
				type = 0;
				open = true;
				return true;
			case '}':
				type = 0;
				open = false;
				return true;
			case '(':
				type = 1;
				open = true;
				return true;
			case ')':
				type = 1;
				open = false;
				open = false;
				return true;
			case '[':
				type = 2;
				open = true;
				return true;
			case ']':
				type = 2;
				open = false;
				return true;
			}
			return false;
		}
		
	};
	
	/**
	 * 
	 * @param backwardOffset searching backward before this offset
	 * @param forwardOffset searching forward after (including) this offset
	 * @param initial initial balance (e.g. known or not yet inserted between backward and forward offset)
	 * @param searchType
	 *     0 = { }
	 *     1 = ( )
	 *     2 = [ ]
	 * @return
	 */
	public int[] computeBracketBalance(int backwardOffset, int forwardOffset, final int[] initial, final int searchType) {
		final int[] compute = new int[3];
		final BracketBalanceCondition condition = new BracketBalanceCondition();
		int breakType = -1;
		ITER_BACKWARD : while (--backwardOffset >= 0) {
			backwardOffset = scanBackward(backwardOffset, -1, condition);
			if (backwardOffset != NOT_FOUND) {
				if (condition.open) {
					compute[condition.type]++;
					if (condition.type != searchType && compute[condition.type] > 0) {
						breakType = condition.type;
						break ITER_BACKWARD;
					}
				}
				else {
					compute[condition.type]--;
				}
			}
			else {
				break ITER_BACKWARD;
			}
		}
		final int bound = fDocument.getLength();
		for (int i = 0; i < compute.length; i++) {
			if (compute[i] < 0) {
				compute[i] = 0;
			}
			compute[i] = compute[i]+initial[i];
		}
		ITER_FORWARD : while (forwardOffset < bound) {
			forwardOffset = scanForward(forwardOffset, bound, condition);
			if (forwardOffset != NOT_FOUND) {
				if (condition.open) {
					compute[condition.type]++;
				}
				else {
					compute[condition.type]--;
				}
				if (breakType >= 0 && compute[breakType] == 0) {
					break ITER_FORWARD;
				}
				forwardOffset++;
			}
			else {
				break ITER_FORWARD;
			}
		}
		return compute;
	}
	
}
