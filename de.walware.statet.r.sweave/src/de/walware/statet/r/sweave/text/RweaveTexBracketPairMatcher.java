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

package de.walware.statet.r.sweave.text;

import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


/**
 * 
 */
public class RweaveTexBracketPairMatcher extends RBracketPairMatcher {
	
	public RweaveTexBracketPairMatcher() {
		super(new RweaveChunkHeuristicScanner(),
				Rweave.R_TEX_PARTITIONING,
				new String[] {
					Rweave.TEX_DEFAULT_CONTENT_TYPE,
					Rweave.TEX_MATH_CONTENT_TYPE,
					Rweave.TEX_VERBATIM_CONTENT_TYPE,
					Rweave.R_DEFAULT_CONTENT_TYPE,
					Rweave.CHUNK_CONTROL_CONTENT_TYPE,
				}
		);
	}
	
	@Override
	protected char getEscapeChar(final String contentType) {
		return (contentType == Rweave.R_DEFAULT_CONTENT_TYPE || contentType == Rweave.CHUNK_CONTROL_CONTENT_TYPE) ?
				(char) 0 : '\\';
	}
	
}
