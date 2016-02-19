/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.dataeditor;

import org.eclipse.jface.viewers.ISelection;


public class RDataTableSelection implements ISelection {
	
	
	private final String fAnchorRowLabel;
	private final String fAnchorColumnLabel;
	
	private final String fLastSelectedCellRowLabel;
	private final String fLastSelectedCellColumnLabel;
	
	
	public RDataTableSelection(final String anchorRowLabel, final String anchorColumnLabel,
			final String lastSelectedCellRowLabel, final String lastSelectedCellColumnLabel) {
		fAnchorRowLabel = anchorRowLabel;
		fAnchorColumnLabel = anchorColumnLabel;
		fLastSelectedCellRowLabel = lastSelectedCellRowLabel;
		fLastSelectedCellColumnLabel = lastSelectedCellColumnLabel;
	}
	
	
	@Override
	public boolean isEmpty() {
		return (fAnchorRowLabel == null);
	}
	
	public String getAnchorRowLabel() {
		return fAnchorRowLabel;
	}
	
	public String getAnchorColumnLabel() {
		return fAnchorColumnLabel;
	}
	
	public String getLastSelectedCellRowLabel() {
		return fLastSelectedCellRowLabel;
	}
	
	public String getLastSelectedCellColumnLabel() {
		return fLastSelectedCellColumnLabel;
	}
	
	
	@Override
	public int hashCode() {
		int h = ((fAnchorRowLabel != null) ? fAnchorRowLabel.hashCode() : 0);
		h = h * 3 + ((fAnchorColumnLabel != null) ? fAnchorColumnLabel.hashCode() : 0);
		h = h * 17 + ((fLastSelectedCellRowLabel != null) ? fLastSelectedCellRowLabel.hashCode() : 0);
		h = h * 99 + ((fLastSelectedCellColumnLabel != null) ? fLastSelectedCellColumnLabel.hashCode() : 0);
		return h;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RDataTableSelection)) {
			return false;
		}
		final RDataTableSelection other = (RDataTableSelection) obj;
		return (((fAnchorRowLabel != null) ?
						fAnchorRowLabel.equals(other.fAnchorRowLabel) : null == other.fAnchorRowLabel )
				&& ((fAnchorColumnLabel != null) ?
						fAnchorColumnLabel.equals(other.fAnchorColumnLabel) : null == other.fAnchorColumnLabel )
				&& ((fLastSelectedCellRowLabel != null) ?
						fLastSelectedCellRowLabel.equals(other.fLastSelectedCellRowLabel) : null == other.fLastSelectedCellRowLabel )
				&& ((fLastSelectedCellColumnLabel != null) ?
						fLastSelectedCellColumnLabel.equals(other.fLastSelectedCellColumnLabel) : null == other.fLastSelectedCellColumnLabel )
		);
	}
	
}
