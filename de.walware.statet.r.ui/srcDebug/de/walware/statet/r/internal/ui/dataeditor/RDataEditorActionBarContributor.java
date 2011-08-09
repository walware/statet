/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.ui.part.EditorActionBarContributor;


public class RDataEditorActionBarContributor extends EditorActionBarContributor {
	
	
	private final StatusLineContributionItem fContributionItem;
	
	
	public RDataEditorActionBarContributor() {
		fContributionItem = new StatusLineContributionItem("data.dimension", 26);
	}
	
	
	@Override
	public void contributeToStatusLine(final IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
		
		statusLineManager.add(fContributionItem);
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		
		fContributionItem.dispose();
	}
	
}
