/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.internal.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.rtm.base.ui.RTaskSnippet;
import de.walware.statet.rtm.base.ui.actions.RTaskRunnable;

import de.walware.statet.r.core.tool.RTool;


public class RunRTaskHandler extends AbstractHandler {
	
	
	private IWorkbenchPart fPart;
	
	
	public RunRTaskHandler() {
	}
	
	public RunRTaskHandler(final IWorkbenchPart part) {
		fPart = part;
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = (fPart != null) ? fPart : HandlerUtil.getActivePart(event);
		if (part == null) {
			return null;
		}
		final RTaskSnippet snippet = (RTaskSnippet) part.getAdapter(RTaskSnippet.class);
		if (snippet == null) {
			return null;
		}
		try {
			final ToolProcess tool = NicoUITools.getTool(part);
			NicoUITools.accessController(RTool.TYPE, RTool.R_SERVICE_FEATURE_ID, tool);
			final RTaskRunnable runnable = new RTaskRunnable(snippet, part.getSite().getPage());
			final IStatus status = tool.getQueue().add(runnable);
			if (status.getSeverity() == IStatus.ERROR) {
				throw new CoreException(status);
			}
		}
		catch (final CoreException e) {
			LTKWorkbenchUIUtil.indicateStatus(e.getStatus(), event);
		}
		return null;
	}
	
}
