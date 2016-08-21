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
import de.walware.ecommons.waltable.resize.MultiColumnResizeCommand;


public class MultiColumnResizeCommandHandler extends AbstractLayerCommandHandler<MultiColumnResizeCommand> {
	
	
	private final RDataLayer fDataLayer;
	
	
	public MultiColumnResizeCommandHandler(final RDataLayer dataLayer) {
		this.fDataLayer= dataLayer;
	}
	
	
	@Override
	public Class<MultiColumnResizeCommand> getCommandClass() {
		return MultiColumnResizeCommand.class;
	}
	
	@Override
	protected boolean doCommand(final MultiColumnResizeCommand command) {
		for (final ILValueIterator posIter= new ValueIterator(command.getPositions()); posIter.hasNext(); ) {
			final long position= posIter.nextValue();
			this.fDataLayer.setColumnWidth(position, command.getColumnWidth(position));
		}
		return true;
	}
	
}
