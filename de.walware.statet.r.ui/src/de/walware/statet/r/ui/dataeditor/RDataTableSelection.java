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
	
	
	public RDataTableSelection(
			final String anchorRowLabel, final String anchorColumnLabel,
			final String lastSelectedCellRowLabel, final String lastSelectedCellColumnLabel) {
		this.fAnchorRowLabel= anchorRowLabel;
		this.fAnchorColumnLabel= anchorColumnLabel;
		this.fLastSelectedCellRowLabel= lastSelectedCellRowLabel;
		this.fLastSelectedCellColumnLabel= lastSelectedCellColumnLabel;
	}
	
	
	@Override
	public boolean isEmpty() {
		return (this.fAnchorRowLabel == null);
	}
	
	
	public String getAnchorRowLabel() {
		return this.fAnchorRowLabel;
	}
	
	public String getAnchorColumnLabel() {
		return this.fAnchorColumnLabel;
	}
	
	public String getLastSelectedCellRowLabel() {
		return this.fLastSelectedCellRowLabel;
	}
	
	public String getLastSelectedCellColumnLabel() {
		return this.fLastSelectedCellColumnLabel;
	}
	
	
	@Override
	public int hashCode() {
		int h= ((this.fAnchorRowLabel != null) ? this.fAnchorRowLabel.hashCode() : 0);
		h= h * 3 + ((this.fAnchorColumnLabel != null) ? this.fAnchorColumnLabel.hashCode() : 0);
		h= h * 17 + ((this.fLastSelectedCellRowLabel != null) ? this.fLastSelectedCellRowLabel.hashCode() : 0);
		h= h * 99 + ((this.fLastSelectedCellColumnLabel != null) ? this.fLastSelectedCellColumnLabel.hashCode() : 0);
		return h;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RDataTableSelection)) {
			return false;
		}
		final RDataTableSelection other= (RDataTableSelection) obj;
		return (((this.fAnchorRowLabel != null) ?
						this.fAnchorRowLabel.equals(other.fAnchorRowLabel) : null == other.fAnchorRowLabel )
				&& ((this.fAnchorColumnLabel != null) ?
						this.fAnchorColumnLabel.equals(other.fAnchorColumnLabel) : null == other.fAnchorColumnLabel )
				&& ((this.fLastSelectedCellRowLabel != null) ?
						this.fLastSelectedCellRowLabel.equals(other.fLastSelectedCellRowLabel) : null == other.fLastSelectedCellRowLabel )
				&& ((this.fLastSelectedCellColumnLabel != null) ?
						this.fLastSelectedCellColumnLabel.equals(other.fLastSelectedCellColumnLabel) : null == other.fLastSelectedCellColumnLabel )
		);
	}
	
}
