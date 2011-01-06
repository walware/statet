/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.ui.RUI;


/**
 * Launch shortcut, which submits the commands (touched by selection)
 * and goes to next commands.
 * 
 * Supports only text editors and R doc (with AST).
 */
public class RunEntireCommandAndGotoNextCommandHandler extends RunEntireCommandHandler {
	
	
	public RunEntireCommandAndGotoNextCommandHandler() {
		super(false);
	}
	
	
	@Override
	protected void postLaunch(final TextData data) {
		try {
			final RAstNode[] nodes = (RAstNode[]) data.modelElements;
			final int offset = getNextOffset(nodes[nodes.length-1], data.document);
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					data.editor.selectAndReveal(offset, 0);
				}
			});
		}
		catch (final BadLocationException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1, "Error occurred when updating selection", null)); //$NON-NLS-1$
		}
	}
	
	protected int getNextOffset(RAstNode node, final IDocument doc) throws BadLocationException {
		RAstNode parent;
		while ((parent = node.getRParent()) != null) {
			final int lastIdx = parent.getChildIndex(node);
			if (lastIdx+1 < parent.getChildCount()) {
				return parent.getChild(lastIdx+1).getOffset();
			}
			node = parent;
		}
		final int line = doc.getLineOfOffset(node.getStopOffset());
		if (line+1 < doc.getNumberOfLines()) {
			return doc.getLineOffset(line+1);
		}
		return doc.getLength();
	}
	
}
