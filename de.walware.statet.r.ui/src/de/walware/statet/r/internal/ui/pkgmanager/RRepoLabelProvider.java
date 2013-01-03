/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.pkgmanager;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

import de.walware.statet.r.core.pkgmanager.RRepo;


public class RRepoLabelProvider extends StyledCellLabelProvider {
	
	
	@Override
	public void update(final ViewerCell cell) {
		final RRepo repo = (RRepo) cell.getElement();
		
		update(cell, repo);
		
		super.update(cell);
	}
	
	protected void update(final ViewerCell cell, final RRepo repo) {
		final StyledString sb = new StyledString(repo.getName());
		sb.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
		sb.append(repo.getURL(), StyledString.QUALIFIER_STYLER);
		
		cell.setText(sb.getString());
		cell.setStyleRanges(sb.getStyleRanges());
	}
	
}
