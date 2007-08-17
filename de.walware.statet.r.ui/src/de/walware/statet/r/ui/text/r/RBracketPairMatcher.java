/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;

import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;


public class RBracketPairMatcher extends PairMatcher {

	
	public static final char[][] BRACKETS = { {'{', '}'}, {'(', ')'}, {'[', ']'} };
	
	
	public RBracketPairMatcher() {
		
		this(new RHeuristicTokenScanner());
	}

	public RBracketPairMatcher(RHeuristicTokenScanner scanner) {
		
		super(BRACKETS, 
				IRDocumentPartitions.R_DOCUMENT_PARTITIONING, 
				IRDocumentPartitions.R_DEFAULT,
				scanner,
				(char) 0);
	}
}
