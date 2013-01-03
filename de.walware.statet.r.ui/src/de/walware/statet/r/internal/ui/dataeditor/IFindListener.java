/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.runtime.IStatus;


public interface IFindListener {
	
	
	final class FindEvent {
		
		public final IStatus status;
		
		public final int total;
		
		public final int rowIdx;
		
		public final int colIdx;
		
		
		public FindEvent(final IStatus status, final int total, final int rowIdx, final int colIdx) {
			this.status = status;
			this.total = total;
			this.rowIdx = rowIdx;
			this.colIdx = colIdx;
		}
		
	}
	
	
	void handleFindEvent(FindEvent event);
	
}
