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

package de.walware.statet.r.internal.ui.intable;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;


public class MultiColumnResizeCommandHandler extends AbstractLayerCommandHandler<MultiColumnResizeCommand> {
	
	
	private final RDataLayer fDataLayer;
	
	
	public MultiColumnResizeCommandHandler(final RDataLayer dataLayer) {
		fDataLayer = dataLayer;
	}
	
	
	@Override
	public Class<MultiColumnResizeCommand> getCommandClass() {
		return MultiColumnResizeCommand.class;
	}
	
	@Override
	protected boolean doCommand(final MultiColumnResizeCommand command) {
		for (final int columnPosition : command.getColumnPositions()) {
			fDataLayer.setColumnWidth(columnPosition, command.getColumnWidth(columnPosition));
		}
		return true;
	}
	
}
