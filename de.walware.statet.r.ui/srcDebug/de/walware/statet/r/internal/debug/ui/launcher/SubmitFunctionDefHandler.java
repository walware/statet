/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;

import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching.SourceRegion;
import de.walware.statet.r.ui.RUI;


/**
 * Launch shortcut, which submits the lowest enclosing function (assign of fdef)
 * and does not change the focus.
 * 
 * Supports only text editors with input supporting R AST.
 */
public class SubmitFunctionDefHandler extends SubmitEntireCommandHandler {
	
	
	public static class AndGotoConsole extends SubmitFunctionDefHandler {
		
		
		public AndGotoConsole() {
			super(true);
		}
		
	}
	
	
	public SubmitFunctionDefHandler() {
		super(false);
	}
	
	protected SubmitFunctionDefHandler(final boolean gotoConsole) {
		super(gotoConsole);
	}
	
	
	@Override
	protected String getErrorMessage() {
		return RLaunchingMessages.RFunctionLaunch_error_message;
	}
	
	@Override
	protected IStatus getRegions(final Data data) throws CoreException {
		final RAstNode node= RAst.findLowestFDefAssignment(data.ast.root,
				data.selection.getOffset() );
		if (node == null) {
			return LaunchShortcutUtil.createUnsupported();
		}
		try {
			if (RAst.hasErrors(node)) {
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID,
						RLaunchingMessages.SubmitCode_info_SyntaxError_message );
			}
			
			data.nodes= new RAstNode[] { node };
			final List<SourceRegion> list= new ArrayList<>(1);
			{	final SourceRegion region= new SourceRegion(data.su, data.document);
				region.setBegin(checkStart(data.document, node.getOffset()));
				region.setEnd(node.getOffset()+node.getLength());
				region.setCode(data.document.get(region.getOffset(), region.getLength()));
				region.setNode(node);
				list.add(region);
			}
			data.regions= list;
			return Status.OK_STATUS;
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					RLaunchingMessages.SubmitCode_error_WhenAnalyzingAndCollecting_message, e));
		}
	}
	
}
