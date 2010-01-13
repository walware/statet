/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui.refactoring;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.ui.util.DNDUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.WorkbenchUIUtil;


/**
 * Handler copying names of selected elements
 */
public class CopyNamesHandler extends AbstractElementsHandler {
	
	
	public CopyNamesHandler(final RefactoringAdapter ltk) {
		super(ltk);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(final Object context) {
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(context);
		if (selection != null) {
			setBaseEnabled(!selection.isEmpty());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		if (selection instanceof IStructuredSelection) {
			final List elements = ((IStructuredSelection) selection).toList();
			final StringBuilder sb = new StringBuilder(elements.size() * 20);
			final String br = System.getProperty("line.separator"); //$NON-NLS-1$
			for (int i = 0; ; ) {
				final String name = getName(elements.get(i++));
				if (name == null) {
					return null;
				}
				sb.append(name);
				if (i < elements.size()) {
					sb.append(br);
				}
				else {
					break;
				}
			}
			
			doCopyToClipboard(event, sb.toString());
		}
		return null;
	}
	
	protected String getName(final Object o) {
		if (o instanceof IModelElement) {
			return ((IModelElement) o).getElementName().getDisplayName();
		}
		return null;
	}
	
	private void doCopyToClipboard(final ExecutionEvent event, final String names) {
		final Clipboard clipboard = new Clipboard(UIAccess.getDisplay());
		try {
			DNDUtil.setContent(clipboard, 
					new Object[] { names }, 
					new Transfer[] { TextTransfer.getInstance() });
		}
		finally {
			if (clipboard != null && !clipboard.isDisposed()) {
				clipboard.dispose();
			}
		}
	}
	
}
