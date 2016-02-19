/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import de.walware.ecommons.text.PairMatcher;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;


public class RBracketPairMatcher extends PairMatcher {
	
	
	public static final char[][] BRACKETS = { {'{', '}'}, {'(', ')'}, {'[', ']'} };
	
	private static final String[] CONTENT_TYPES= new String[] {
		IRDocumentConstants.R_DEFAULT_CONTENT_TYPE
	};
	
	
	public RBracketPairMatcher(final RHeuristicTokenScanner scanner) {
		this(scanner, CONTENT_TYPES);
	}
	
	public RBracketPairMatcher(final RHeuristicTokenScanner scanner, final String[] partitions) {
		super(BRACKETS,
				scanner.getDocumentPartitioning(),
				partitions,
				scanner,
				(char) 0);
	}
	
}
