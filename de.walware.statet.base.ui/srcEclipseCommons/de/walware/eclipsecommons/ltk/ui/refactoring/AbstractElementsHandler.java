/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.ui.refactoring;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import de.walware.eclipsecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.eclipsecommons.ui.util.DNDUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;


public abstract class AbstractElementsHandler extends AbstractHandler {
	
	
	private RefactoringAdapter fLTK;
	
	
	public AbstractElementsHandler(final RefactoringAdapter ltk) {
		fLTK = ltk;
	}
	
	
	protected RefactoringAdapter getRefactoringAdapter() {
		return fLTK;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		fLTK = null;
	}
	
	protected boolean copyToClipboard(final ExecutionEvent event, final String sourceCode) {
		final Clipboard clipboard = new Clipboard(UIAccess.getDisplay());
		try {
			return DNDUtil.setContent(clipboard, 
					new Object[] { sourceCode }, 
					new Transfer[] { TextTransfer.getInstance() });
		}
		finally {
			if (clipboard != null && !clipboard.isDisposed()) {
				clipboard.dispose();
			}
		}
	}
	
	protected String getCodeFromClipboard(final ExecutionEvent event) {
		final Clipboard clipboard = new Clipboard(UIAccess.getDisplay());
		try {
			return (String) clipboard.getContents(TextTransfer.getInstance());
		}
		finally {
			if (clipboard != null && !clipboard.isDisposed()) {
				clipboard.dispose();
			}
		}
	}
	
}
