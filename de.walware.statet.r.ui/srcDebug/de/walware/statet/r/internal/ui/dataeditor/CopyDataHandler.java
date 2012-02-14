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

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.copy.command.CopyDataToClipboardCommand;

import de.walware.statet.r.ui.dataeditor.RDataTableComposite;


public class CopyDataHandler extends AbstractHandler {
	
	
	private final RDataTableComposite fTableComposite;
	
	
	public CopyDataHandler(final RDataTableComposite tableProvider) {
		fTableComposite = tableProvider;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
//		final NatTable table = tableProvider.getNatTable();
//		setBaseEnabled(table != null && !table.isDisposed());
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final NatTable table = fTableComposite.getNatTable();
		if (table != null) {
			table.doCommand(new CopyDataToClipboardCommand("\t", "\n"));
		}
		return null;
	}
	
}
