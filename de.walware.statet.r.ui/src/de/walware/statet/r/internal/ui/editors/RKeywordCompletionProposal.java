/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ui.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.SimpleCompletionProposal;

import de.walware.statet.r.ui.RUI;


public class RKeywordCompletionProposal extends SimpleCompletionProposal {
	
	
	/**
	 * @param keyworkd
	 * @param replacementOffset
	 */
	public RKeywordCompletionProposal(final AssistInvocationContext context, final String keyword, final int replacementOffset) {
		super(context, keyword, replacementOffset);
	}
	
	
	@Override
	protected String getPluginId() {
		return RUI.PLUGIN_ID;
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
	
	@Override
	public boolean isAutoInsertable() {
		return true;
	}
	
}
