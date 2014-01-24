/*=============================================================================#
 # Copyright (c) 2011-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.nico.AbstractRDbgController;

public class RDebugUIUtils {
	
	
	public static AbstractRDbgController getRDbgController(final ISourceEditor editor) {
		final IWorkbenchPart workbenchPart = editor.getWorkbenchPart();
		if (editor == null || workbenchPart == null) {
			return null;
		}
		final ToolProcess tool = NicoUITools.getTool(workbenchPart);
		if (tool.getMainType() != RConsoleTool.TYPE || (!(tool.getController() instanceof AbstractRDbgController))) {
			return null;
		}
		final AbstractRDbgController controller = (AbstractRDbgController) tool.getController();
		return (controller != null && controller.getStatus() == ToolStatus.STARTED_SUSPENDED) ?
				controller : null;
	}
	
	public static IRStackFrame getFrame(final IWorkbenchPart part, final ToolProcess process) {
		final AtomicReference<IRStackFrame> ref = new AtomicReference<IRStackFrame>();
		UIAccess.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				final IDebugContextService contextService = DebugUITools.getDebugContextManager()
						.getContextService(part.getSite().getWorkbenchWindow());
				final ISelection selection = contextService.getActiveContext();
				if (selection instanceof IStructuredSelection) {
					final Object firstElement = ((IStructuredSelection) selection).getFirstElement();
					if (firstElement instanceof IAdaptable) {
						ref.set((IRStackFrame) ((IAdaptable) firstElement)
								.getAdapter(IRStackFrame.class) );
					}
				}
			}
		});
		return ref.get();
	}
	
}
