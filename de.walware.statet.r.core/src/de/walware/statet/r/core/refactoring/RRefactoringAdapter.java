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

package de.walware.statet.r.core.refactoring;

import org.eclipse.jface.text.ITypedRegion;

import de.walware.eclipsecommons.ltk.core.refactoring.RefactoringAdapter;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;


/**
 * RefactoringAdapter for R
 */
public class RRefactoringAdapter extends RefactoringAdapter {
	
	
	public RRefactoringAdapter() {
		super(new RHeuristicTokenScanner());
	}
	
	
	@Override
	public String getPluginIdentifier() {
		return RCore.PLUGIN_ID;
	}
	
	@Override
	public boolean isCommentContent(final ITypedRegion partition) {
		return (partition != null) && partition.getType().equals(IRDocumentPartitions.R_COMMENT);
	}
	
}
