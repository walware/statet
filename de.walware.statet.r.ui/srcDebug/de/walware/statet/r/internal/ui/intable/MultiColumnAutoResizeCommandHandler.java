/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.intable;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.resize.command.AutoResizeColumnsCommand;


public class MultiColumnAutoResizeCommandHandler extends AbstractLayerCommandHandler<AutoResizeColumnsCommand> {
	
	
	private final RDataLayer fDataLayer;
	
	
	public MultiColumnAutoResizeCommandHandler(final RDataLayer dataLayer) {
		fDataLayer = dataLayer;
	}
	
	
	@Override
	public Class<AutoResizeColumnsCommand> getCommandClass() {
		return AutoResizeColumnsCommand.class;
	}
	
	@Override
	protected boolean doCommand(final AutoResizeColumnsCommand command) {
		for (final int columnPosition : command.getColumnPositions()) {
			fDataLayer.setColumnWidthToAutoWidth(columnPosition);
		}
		return true;
	}
	
}
