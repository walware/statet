/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.swt.graphics.Image;


public class KeywordCompletionProposal extends SimpleCompletionProposal {
	
	
	/**
	 * @param keyworkd
	 * @param replacementOffset
	 */
	public KeywordCompletionProposal(final String keyword, final int replacementOffset) {
		super(keyword, replacementOffset);
	}
	
	
	/**
	 * {@inheritDoc}
	 * {@value 80}
	 */
	@Override
	public int getRelevance() {
		return 80;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		return null;
	}
	
}
