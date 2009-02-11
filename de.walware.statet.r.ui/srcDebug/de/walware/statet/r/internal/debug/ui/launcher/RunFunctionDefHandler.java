/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;

import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.ui.RUI;


/**
 * Launch shortcut, which submits the lowest enclosing function (assign of fdef)
 * and does not change the focus.
 * 
 * Supports only text editors with input supporting R AST.
 */
public class RunFunctionDefHandler extends RunEntireCommandHandler {
	
	
	public RunFunctionDefHandler() {
		super(false);
	}
	
	public RunFunctionDefHandler(final boolean gotoConsole) {
		super(gotoConsole);
	}
	
	
	@Override
	protected String getErrorMessage() {
		return RLaunchingMessages.RFunctionLaunch_error_message;
	}
	
	@Override
	protected String getCode(final TextData data) throws CoreException {
		final RAstNode node = RAst.findLowestFDefAssignment(data.astInfo.root, data.selection.getOffset());
		if (node == null) {
			return null;
		}
		try {
			final int start = checkStart(data.document, node.getOffset());
			final int end = node.getOffset()+node.getLength();
			final String code = data.document.get(start, end-start);
			data.modelElements = node;
			return code;
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
		}
	}
	
}
