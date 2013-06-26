/*******************************************************************************
 * Copyright (c) 2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.statet.r.ui.dataeditor.RDataTableComposite;


public class GotoCellHandler extends AbstractHandler implements IElementUpdater {
	
	
	private final RDataTableComposite tableComposite;
	
	
	public GotoCellHandler(final RDataTableComposite table) {
		this.tableComposite = table;
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		element.setText("Go to line (cell)...");
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!this.tableComposite.isOK()) {
			return null;
		}
		
		final GotoCellDialog dialog = new GotoCellDialog(this.tableComposite);
		final long[] anchor = this.tableComposite.getAnchor();
		if (anchor != null) {
			dialog.set(HORIZONTAL, anchor[0]);
			dialog.set(VERTICAL, anchor[1]);
		}
		if (dialog.open() == Window.OK) {
			this.tableComposite.setAnchor(dialog.get(HORIZONTAL), dialog.get(VERTICAL));
		}
		return null;
	}
	
}
