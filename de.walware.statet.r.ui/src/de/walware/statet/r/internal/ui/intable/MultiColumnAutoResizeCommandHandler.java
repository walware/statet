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

import de.walware.ecommons.waltable.command.AbstractLayerCommandHandler;
import de.walware.ecommons.waltable.coordinate.ILValueIterator;
import de.walware.ecommons.waltable.coordinate.LRangeList.ValueIterator;
import de.walware.ecommons.waltable.resize.AutoResizePositionsCommand;


public class MultiColumnAutoResizeCommandHandler extends AbstractLayerCommandHandler<AutoResizePositionsCommand> {
	
	
	private final RDataLayer fDataLayer;
	
	
	public MultiColumnAutoResizeCommandHandler(final RDataLayer dataLayer) {
		this.fDataLayer= dataLayer;
	}
	
	
	@Override
	public Class<AutoResizePositionsCommand> getCommandClass() {
		return AutoResizePositionsCommand.class;
	}
	
	@Override
	protected boolean doCommand(final AutoResizePositionsCommand command) {
		for (final ILValueIterator posIter= new ValueIterator(command.getPositions()); posIter.hasNext(); ) {
			this.fDataLayer.setColumnWidthToAutoWidth(posIter.nextValue());
		}
		return true;
	}
	
}
