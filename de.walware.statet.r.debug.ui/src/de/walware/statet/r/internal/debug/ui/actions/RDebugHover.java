/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.debug.core.IRStackFrame;
import de.walware.statet.r.internal.debug.ui.RDebugUIUtils;
import de.walware.statet.r.ui.rtool.RElementInfoHoverCreator;
import de.walware.statet.r.ui.rtool.RElementInfoTask;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext;


public class RDebugHover implements IInfoHover {
	
	
	private final int mode;
	
	private IInformationControlCreator controlCreator;
	
	
	public RDebugHover() {
		this.mode= MODE_TOOLTIP;
	}
	
	
	@Override
	public Object getHoverInfo(final AssistInvocationContext context) {
		final IWorkbenchPart part= context.getEditor().getWorkbenchPart();
		final ToolProcess tool= NicoUITools.getTool(part);
		if (tool == null || !(context instanceof RAssistInvocationContext)) {
			return null;
		}
		final RElementName name= ((RAssistInvocationContext) context).getNameSelection();
		if (name != null) {
			final RElementInfoTask info= new RElementInfoTask(name) {
				@Override
				protected int getFramePosition(final IRDataAdapter r,
						final IProgressMonitor monitor) throws CoreException {
					if (tool.getToolStatus() == ToolStatus.STARTED_SUSPENDED) {
						final IRStackFrame frame= RDebugUIUtils.getFrame(part, tool);
						if (frame != null && frame.getPosition() > 0) {
							return frame.getPosition();
						}
					}
					return 0;
				}
			};
			if (info.preCheck()) {
				return info.load(tool, context.getSourceViewer().getTextWidget(), part);
			}
		}
		return null;
	}
	
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (this.controlCreator == null) {
			this.controlCreator= new RElementInfoHoverCreator(this.mode);
		}
		return this.controlCreator;
	}
	
}
