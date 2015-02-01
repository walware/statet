/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import de.walware.ecommons.text.ITokenScanner;
import de.walware.ecommons.text.PairMatcher;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;


public class RBracketPairMatcher extends PairMatcher {
	
	
	public static final char[][] BRACKETS = { {'{', '}'}, {'(', ')'}, {'[', ']'} };
	
	private static final String[] DEFAULT_SET = new String[] {
		IRDocumentPartitions.R_DEFAULT,
		IRDocumentPartitions.R_DEFAULT_EXPL
	};
	
	private static final String[] EXPL_SET = new String[] {
		IRDocumentPartitions.R_DEFAULT_EXPL
	};
	
	
	public RBracketPairMatcher() {
		this(new RHeuristicTokenScanner());
	}
	
	public RBracketPairMatcher(final RHeuristicTokenScanner scanner) {
		this(scanner, scanner.getPartitioningConfig().getPartitioning(),
				scanner.getPartitioningConfig().getDefaultPartitionConstraint().matches(IRDocumentPartitions.R_DEFAULT) ?
						DEFAULT_SET : EXPL_SET );
	}
	
	public RBracketPairMatcher(final ITokenScanner scanner, final String partitioning,
			final String[] partitions) {
		super(BRACKETS,
				partitioning,
				partitions,
				scanner,
				(char) 0);
	}
	
}
