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

package de.walware.statet.r.internal.ui.intable;

import static de.walware.ecommons.waltable.coordinate.Orientation.HORIZONTAL;

import de.walware.ecommons.waltable.command.AbstractLayerCommandHandler;
import de.walware.ecommons.waltable.resize.DimPositionResizeCommand;


public class DimPositionResizeCommandHandler extends AbstractLayerCommandHandler<DimPositionResizeCommand> {
	
	
	private final RDataLayer dataLayer;
	
	
	public DimPositionResizeCommandHandler(final RDataLayer dataLayer) {
		this.dataLayer= dataLayer;
	}
	
	
	@Override
	public Class<DimPositionResizeCommand> getCommandClass() {
		return DimPositionResizeCommand.class;
	}
	
	
	@Override
	protected boolean doCommand(final DimPositionResizeCommand command) {
		if (command.getOrientation() == HORIZONTAL) {
			this.dataLayer.setColumnWidth(command.getPosition(), command.getNewSize());
		}
		return true;
	}
	
}
